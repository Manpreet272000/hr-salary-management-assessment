package com.acme.hr.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.acme.hr.support.IntegrationTest;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CompensationApiTest extends IntegrationTest {

    @Test
    void setsPayInTheEmployeesCountryCurrency() throws Exception {
        long id = createEmployee("SALES", "FTE", "DE");

        setPay(id, "REGULAR", "5000.50").andExpect(status().isOk());
        setPay(id, "INCENTIVE", "1000")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.length()").value(2))
                .andExpect(jsonPath("$.components[0].currency").value("EUR"))
                .andExpect(jsonPath("$.monthlyGross").value(6000.50))
                .andExpect(jsonPath("$.allowedPayTypes.length()").value(2));
    }

    @Test
    void revisionKeepsPreviousAmountAsHistory() throws Exception {
        long id = createEmployee("ENGINEERING", "FTE", "IN");
        insertCurrentPay(id, "REGULAR", "80000", LocalDate.of(2025, 4, 1));

        setPay(id, "REGULAR", "90000")
                .andExpect(jsonPath("$.components.length()").value(1))
                .andExpect(jsonPath("$.components[0].amount").value(90000.0))
                .andExpect(jsonPath("$.components[0].effectiveFrom").value(TODAY.toString()));

        mvc.perform(get("/api/employees/{id}/compensation/history", id))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].amount").value(90000.0))
                .andExpect(jsonPath("$[0].effectiveTo").doesNotExist())
                .andExpect(jsonPath("$[1].amount").value(80000.0))
                .andExpect(jsonPath("$[1].effectiveTo").value(TODAY.toString()));
    }

    @Test
    void sameDayRevisionCorrectsTheAmountInsteadOfAddingHistory() throws Exception {
        long id = createEmployee("ENGINEERING", "FTE", "IN");

        setPay(id, "REGULAR", "90000");
        setPay(id, "REGULAR", "95000");

        mvc.perform(get("/api/employees/{id}/compensation/history", id))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].amount").value(95000.0));
    }

    @Test
    void rejectsPayTypesNotAllowedForTheEmployee() throws Exception {
        long intern = createEmployee("ENGINEERING", "INTERN", "IN");
        long engineer = createEmployee("ENGINEERING", "FTE", "IN");

        setPay(intern, "REGULAR", "1000").andExpect(status().isUnprocessableEntity());
        setPay(intern, "STIPEND", "1000").andExpect(status().isOk());
        setPay(engineer, "INCENTIVE", "1000").andExpect(status().isUnprocessableEntity());
    }

    @Test
    void rejectsNonPositiveAmounts() throws Exception {
        long id = createEmployee("ENGINEERING", "FTE", "IN");

        setPay(id, "REGULAR", "0").andExpect(status().isBadRequest());
        setPay(id, "REGULAR", "-5").andExpect(status().isBadRequest());
    }

    @Test
    void inactiveEmployeesPayCannotBeChanged() throws Exception {
        long id = createEmployee("SALES", "FTE", "IN");
        setPay(id, "INCENTIVE", "1000");
        deactivate(id);

        setPay(id, "REGULAR", "50000")
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("inactive")));
        mvc.perform(delete("/api/employees/{id}/compensation/INCENTIVE", id))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void endingAComponentStopsItButKeepsItInHistory() throws Exception {
        long id = createEmployee("SALES", "FTE", "IN");
        insertCurrentPay(id, "INCENTIVE", "10000", LocalDate.of(2026, 1, 1));

        mvc.perform(delete("/api/employees/{id}/compensation/INCENTIVE", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.length()").value(0));
        mvc.perform(get("/api/employees/{id}/compensation/history", id))
                .andExpect(jsonPath("$[0].effectiveTo").value(TODAY.toString()));
    }

    @Test
    void leavingSalesEndsTheIncentiveButKeepsSalary() throws Exception {
        long id = createEmployee("SALES", "FTE", "IN");
        insertCurrentPay(id, "REGULAR", "50000", LocalDate.of(2026, 1, 1));
        insertCurrentPay(id, "INCENTIVE", "10000", LocalDate.of(2026, 1, 1));

        updateProfile(id, "MARKETING", "FTE", "IN");

        mvc.perform(get("/api/employees/{id}/compensation", id))
                .andExpect(jsonPath("$.components.length()").value(1))
                .andExpect(jsonPath("$.components[0].payType").value("REGULAR"));
    }

    @Test
    void relocatingEndsPayInTheOldCurrency() throws Exception {
        long id = createEmployee("ENGINEERING", "FTE", "IN");
        insertCurrentPay(id, "REGULAR", "50000", LocalDate.of(2026, 1, 1));

        updateProfile(id, "ENGINEERING", "FTE", "SG");

        mvc.perform(get("/api/employees/{id}/compensation", id))
                .andExpect(jsonPath("$.components.length()").value(0));
        setPay(id, "REGULAR", "7000").andExpect(jsonPath("$.components[0].currency").value("SGD"));
    }

    private void updateProfile(long id, String department, String type, String country) throws Exception {
        send(put("/api/employees/{id}", id), Map.of("fullName", "Test Person", "jobTitle", "Tester",
                "department", department, "employmentType", type, "country", country))
                .andExpect(status().isOk());
    }

    /** Pay that started before today, so a revision creates history rather than a same-day correction. */
    private void insertCurrentPay(long employeeId, String payType, String amount, LocalDate from) {
        jdbc.update("""
                INSERT INTO salary_component (employee_id, pay_type, amount, currency, effective_from, created_at, updated_at)
                SELECT id, ?, ?, CASE country WHEN 'IN' THEN 'INR' WHEN 'SG' THEN 'SGD' ELSE 'USD' END, ?, 0, 0
                FROM employee WHERE id = ?""",
                payType, new java.math.BigDecimal(amount), java.sql.Date.valueOf(from), employeeId);
    }
}
