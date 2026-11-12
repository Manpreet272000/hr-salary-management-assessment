package com.acme.hr.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.acme.hr.support.IntegrationTest;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

/** Attendance recording and the monthly pay estimate it feeds (November 2026: 21 working days). */
class PayEstimateApiTest extends IntegrationTest {

    @Test
    void estimateWithNoRecordedLeaveIsGrossLessTax() throws Exception {
        long id = employeeWithSalary("42000");

        mvc.perform(get("/api/employees/{id}/pay-estimate", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.month").value("2026-11"))
                .andExpect(jsonPath("$.currency").value("INR"))
                .andExpect(jsonPath("$.payrollCutoff").value("2026-11-27"))
                .andExpect(jsonPath("$.breakdown.workingDays").value(21))
                .andExpect(jsonPath("$.breakdown.tax").value(2100.0))
                .andExpect(jsonPath("$.breakdown.netPay").value(39900.0));
    }

    @Test
    void appliedSickAndCasualLeaveDoNotChangeTheEstimate() throws Exception {
        long id = employeeWithSalary("42000");

        recordLeave(id, "2026-11", 2, 3, 0).andExpect(status().isOk());

        estimate(id, "2026-11")
                .andExpect(jsonPath("$.leaves.SICK").value(2))
                .andExpect(jsonPath("$.breakdown.lossOfPayDeduction").value(0.0))
                .andExpect(jsonPath("$.breakdown.netPay").value(39900.0));
    }

    @Test
    void unplannedLeaveIsDeductedPerWorkingDayBeforeTax() throws Exception {
        long id = employeeWithSalary("42000");

        recordLeave(id, "2026-11", 1, 0, 3).andExpect(status().isOk());

        // 42000 × 3 / 21 = 6000 deducted; (42000 − 6000) × 0.95 = 34200
        estimate(id, "2026-11")
                .andExpect(jsonPath("$.breakdown.lossOfPayDays").value(3))
                .andExpect(jsonPath("$.breakdown.lossOfPayDeduction").value(6000.0))
                .andExpect(jsonPath("$.breakdown.netPay").value(34200.0));
    }

    @Test
    void recordingAgainReplacesTheMonthsLeave() throws Exception {
        long id = employeeWithSalary("42000");

        recordLeave(id, "2026-11", 0, 0, 3);
        recordLeave(id, "2026-11", 0, 0, 1);

        estimate(id, "2026-11").andExpect(jsonPath("$.breakdown.lossOfPayDays").value(1));
        mvc.perform(get("/api/employees/{id}/attendance/2026-11", id))
                .andExpect(jsonPath("$.leaves.UNPLANNED").value(1))
                .andExpect(jsonPath("$.workingDays").value(21));
    }

    @Test
    void leaveIsTrackedPerMonth() throws Exception {
        long id = employeeWithSalary("42000");

        recordLeave(id, "2026-10", 0, 0, 5);

        estimate(id, "2026-11").andExpect(jsonPath("$.breakdown.lossOfPayDays").value(0));
        estimate(id, "2026-10")
                .andExpect(jsonPath("$.breakdown.workingDays").value(22))
                .andExpect(jsonPath("$.breakdown.lossOfPayDays").value(5));
    }

    @Test
    void salesIncentiveIsNotReducedByUnplannedLeave() throws Exception {
        long id = createEmployee("SALES", "FTE", "IN");
        setPay(id, "REGULAR", "42000");
        setPay(id, "INCENTIVE", "10000");

        recordLeave(id, "2026-11", 0, 0, 21);

        estimate(id, "2026-11")
                .andExpect(jsonPath("$.breakdown.grossPay").value(52000.0))
                .andExpect(jsonPath("$.breakdown.lossOfPayDeduction").value(42000.0))
                .andExpect(jsonPath("$.breakdown.netPay").value(9500.0));
    }

    @Test
    void inactiveEmployeesHaveNoPayoutAndNoAttendance() throws Exception {
        long id = employeeWithSalary("42000");
        deactivate(id);

        estimate(id, "2026-11")
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("inactive")));
        recordLeave(id, "2026-11", 0, 0, 1).andExpect(status().isUnprocessableEntity());
    }

    @Test
    void reactivatedEmployeesArePaidAgain() throws Exception {
        long id = employeeWithSalary("42000");
        deactivate(id);
        send(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .patch("/api/employees/{id}/status", id), Map.of("active", true));

        estimate(id, "2026-11").andExpect(status().isOk()).andExpect(jsonPath("$.breakdown.netPay").value(39900.0));
    }

    @Test
    void rejectsMoreLeaveThanWorkingDays() throws Exception {
        long id = employeeWithSalary("42000");

        recordLeave(id, "2026-11", 10, 10, 2)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value("22 leave days exceed the 21 working days in 2026-11"));
    }

    @Test
    void rejectsNegativeLeaveAndFutureMonths() throws Exception {
        long id = employeeWithSalary("42000");

        recordLeave(id, "2026-11", -1, 0, 0).andExpect(status().isBadRequest());
        recordLeave(id, "2026-12", 0, 0, 1).andExpect(status().isUnprocessableEntity());
    }

    private long employeeWithSalary(String amount) throws Exception {
        long id = createEmployee("ENGINEERING", "FTE", "IN");
        setPay(id, "REGULAR", amount).andExpect(status().isOk());
        return id;
    }

    private ResultActions recordLeave(long id, String month, int sick, int casual, int unplanned) throws Exception {
        return send(put("/api/employees/{id}/attendance/{month}", id, month),
                Map.of("SICK", sick, "CASUAL", casual, "UNPLANNED", unplanned));
    }

    private ResultActions estimate(long id, String month) throws Exception {
        return mvc.perform(get("/api/employees/{id}/pay-estimate", id).param("month", month));
    }
}
