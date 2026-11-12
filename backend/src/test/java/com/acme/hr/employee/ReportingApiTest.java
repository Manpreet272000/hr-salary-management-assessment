package com.acme.hr.employee;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.acme.hr.support.IntegrationTest;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

class ReportingApiTest extends IntegrationTest {

    @Test
    void salesRepReportsToSalesManager() throws Exception {
        long manager = createEmployee("SALES", "FTE", "IN");
        long rep = createEmployee("SALES", "FTE", "US");

        assignManager(rep, manager)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.manager.id").value(manager));
        mvc.perform(get("/api/employees/{id}", rep)).andExpect(jsonPath("$.managerId").value(manager));
        mvc.perform(get("/api/employees/{id}/reporting", manager))
                .andExpect(jsonPath("$.manager").doesNotExist())
                .andExpect(jsonPath("$.directReports[0].id").value(rep));
    }

    @Test
    void salesRepCannotReportToEngineeringManager() throws Exception {
        long engineeringManager = createEmployee("ENGINEERING", "FTE", "IN");
        long rep = createEmployee("SALES", "FTE", "IN");

        assignManager(rep, engineeringManager)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value("A SALES employee must report to a manager in SALES, not ENGINEERING"));
    }

    @Test
    void reportsAllBrokenRulesTogether() throws Exception {
        long contractor = createEmployee("ENGINEERING", "CONTRACTUAL", "IN");
        long rep = createEmployee("SALES", "FTE", "IN");

        assignManager(rep, contractor)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value(containsString("Only full-time employees can manage people")))
                .andExpect(jsonPath("$.detail").value(containsString("must report to a manager in SALES")));
    }

    @Test
    void rejectsInactiveManagersAndSelfReporting() throws Exception {
        long leaver = createEmployee("SALES", "FTE", "IN");
        deactivate(leaver);
        long rep = createEmployee("SALES", "FTE", "IN");

        assignManager(rep, leaver).andExpect(status().isUnprocessableEntity());
        assignManager(rep, rep).andExpect(status().isUnprocessableEntity());
    }

    @Test
    void rejectsLoopsInTheHierarchy() throws Exception {
        long head = createEmployee("SALES", "FTE", "IN");
        long lead = createEmployee("SALES", "FTE", "IN");
        long rep = createEmployee("SALES", "FTE", "IN");
        assignManager(lead, head);
        assignManager(rep, lead);

        assignManager(head, rep)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value(containsString("would create a loop")));
    }

    @Test
    void showsTheManagementChainFromTheTop() throws Exception {
        long head = createEmployee("SALES", "FTE", "IN");
        long lead = createEmployee("SALES", "FTE", "IN");
        long rep = createEmployee("SALES", "FTE", "IN");
        assignManager(lead, head);
        assignManager(rep, lead);

        mvc.perform(get("/api/employees/{id}/reporting", rep))
                .andExpect(jsonPath("$.chain.length()").value(2))
                .andExpect(jsonPath("$.chain[0].id").value(head))
                .andExpect(jsonPath("$.chain[1].id").value(lead))
                .andExpect(jsonPath("$.manager.id").value(lead));
    }

    @Test
    void removesAManager() throws Exception {
        long manager = createEmployee("SALES", "FTE", "IN");
        long rep = createEmployee("SALES", "FTE", "IN");
        assignManager(rep, manager);

        assignManager(rep, null).andExpect(jsonPath("$.manager").doesNotExist());
    }

    @Test
    void managerWithAReportCannotBeMarkedInactive() throws Exception {
        long manager = createEmployee("SALES", "FTE", "IN");
        long rep = createEmployee("SALES", "FTE", "IN");
        assignManager(rep, manager);

        deactivate(manager)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value(containsString("manages 1 people")));

        assignManager(rep, null);
        deactivate(manager).andExpect(status().isOk());
    }

    @Test
    void managerCannotMoveDepartmentWhileTheirTeamStays() throws Exception {
        long manager = createEmployee("SALES", "FTE", "IN");
        long rep = createEmployee("SALES", "FTE", "IN");
        assignManager(rep, manager);

        updateProfile(manager, "ENGINEERING")
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value(containsString("reassign them first")));
        mvc.perform(get("/api/employees/{id}", manager)).andExpect(jsonPath("$.department").value("SALES"));
    }

    @Test
    void employeeMovingDepartmentLosesTheirOldManager() throws Exception {
        long manager = createEmployee("SALES", "FTE", "IN");
        long rep = createEmployee("SALES", "FTE", "IN");
        assignManager(rep, manager);

        updateProfile(rep, "MARKETING")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.managerId").doesNotExist());
    }

    @Test
    void inactiveEmployeesCannotBeGivenAManager() throws Exception {
        long manager = createEmployee("SALES", "FTE", "IN");
        long leaver = createEmployee("SALES", "FTE", "IN");
        deactivate(leaver);

        assignManager(leaver, manager).andExpect(status().isUnprocessableEntity());
    }

    @Test
    void candidatesAreOnlyValidManagers() throws Exception {
        long rep = createEmployee("SALES", "FTE", "IN");
        long salesManager = createEmployee("SALES", "FTE", "GB");
        createEmployee("ENGINEERING", "FTE", "IN");
        createEmployee("SALES", "INTERN", "IN");
        createEmployee("SALES", "CONTRACTUAL", "IN");
        deactivate(createEmployee("SALES", "FTE", "IN"));

        mvc.perform(get("/api/employees/{id}/manager-candidates", rep))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(salesManager));
        mvc.perform(get("/api/employees/{id}/manager-candidates", rep).param("query", EmployeeCode.format(salesManager)))
                .andExpect(jsonPath("$.length()").value(1));
    }

    private ResultActions assignManager(long employeeId, Long managerId) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("managerId", managerId);
        return send(put("/api/employees/{id}/manager", employeeId), body);
    }

    private ResultActions updateProfile(long id, String department) throws Exception {
        return send(put("/api/employees/{id}", id), Map.of("fullName", "Test Person", "jobTitle", "Tester",
                "department", department, "employmentType", "FTE", "country", "IN"));
    }
}
