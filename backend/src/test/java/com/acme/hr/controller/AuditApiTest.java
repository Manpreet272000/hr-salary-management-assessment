package com.acme.hr.controller;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.acme.hr.support.IntegrationTest;
import com.acme.hr.util.EmployeeCode;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

/** Every HR change is recorded once, with before → after, time, actor and operation, and nothing else is. */
class AuditApiTest extends IntegrationTest {

    @Test
    void addingAnEmployeeRecordsTheirProfileWithWhenAndWho() throws Exception {
        long id = createEmployee("SALES", "FTE", "IN");

        auditOf(id)
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].subject").value("PROFILE"))
                .andExpect(jsonPath("$.content[0].operation").value("Add employee"))
                .andExpect(jsonPath("$.content[0].summary").value(
                        "Profile set: name Test Person, job title Tester, department SALES, employment type FTE, country IN"))
                .andExpect(jsonPath("$.content[0].actor").value("HR Manager"))
                .andExpect(jsonPath("$.content[0].occurredAt").value("2026-11-11T10:00:00Z"));
    }

    @Test
    void recordsTheActorFromTheRequest() throws Exception {
        long id = createEmployee("SALES", "FTE", "IN");

        mvc.perform(put("/api/employees/{id}/compensation/REGULAR", id)
                        .header("X-Actor", "priya.hr@acme.example")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"amount\": 50000}"))
                .andExpect(status().isOk());

        auditOf(id).andExpect(jsonPath("$.content[0].actor").value("priya.hr@acme.example"));
    }

    @Test
    void payChangesShowTheAmountBeforeAndAfter() throws Exception {
        long id = createEmployee("ENGINEERING", "FTE", "IN");

        setPay(id, "REGULAR", "50000");
        setPay(id, "REGULAR", "60000");

        auditOf(id)
                .andExpect(jsonPath("$.content[0].summary").value("Pay changed: REGULAR INR 50,000.00 → INR 60,000.00"))
                .andExpect(jsonPath("$.content[0].operation").value("Revise pay"))
                .andExpect(jsonPath("$.content[0].details.before.REGULAR").value("INR 50,000.00"))
                .andExpect(jsonPath("$.content[0].details.after.REGULAR").value("INR 60,000.00"))
                .andExpect(jsonPath("$.content[1].summary").value("Pay set: REGULAR INR 50,000.00"));
    }

    @Test
    void leaveRecordsShowWhichLeaveChanged() throws Exception {
        long id = createEmployee("ENGINEERING", "FTE", "IN");

        recordLeave(id, Map.of("SICK", 1, "UNPLANNED", 2));
        recordLeave(id, Map.of("SICK", 1, "UNPLANNED", 3));

        auditOf(id)
                .andExpect(jsonPath("$.content[0].subject").value("LEAVE"))
                .andExpect(jsonPath("$.content[0].summary").value("Leave changed: 2026-11 UNPLANNED 2 → 3"))
                .andExpect(jsonPath("$.content[1].summary")
                        .value("Leave changed: 2026-11 SICK 0 → 1; 2026-11 UNPLANNED 0 → 2"));
    }

    @Test
    void statusAndManagerChangesAreRecorded() throws Exception {
        long manager = createEmployee("SALES", "FTE", "IN");
        long rep = createEmployee("SALES", "FTE", "IN");

        send(put("/api/employees/{id}/manager", rep), Map.of("managerId", manager)).andExpect(status().isOk());
        auditOf(rep).andExpect(jsonPath("$.content[0].summary")
                .value("Manager set: reports to Test Person (%s)".formatted(EmployeeCode.format(manager))));

        send(put("/api/employees/{id}/manager", rep), java.util.Collections.singletonMap("managerId", null));
        deactivate(rep);

        auditOf(rep)
                .andExpect(jsonPath("$.content[0].summary").value("Status changed: status Active → Inactive"))
                .andExpect(jsonPath("$.content[0].operation").value("Change status"))
                .andExpect(jsonPath("$.content[1].summary").value(org.hamcrest.Matchers.startsWith("Manager cleared")));
    }

    @Test
    void sideEffectsOfAnOperationAreRecordedUnderThatOperation() throws Exception {
        long id = createEmployee("SALES", "FTE", "IN");
        setPay(id, "INCENTIVE", "10000");

        send(put("/api/employees/{id}", id), Map.of("fullName", "Test Person", "jobTitle", "Tester",
                "department", "MARKETING", "employmentType", "FTE", "country", "IN"))
                .andExpect(status().isOk());

        mvc.perform(get("/api/audit").param("employeeId", String.valueOf(id)).param("subject", "PAY"))
                .andExpect(jsonPath("$.content[0].summary").value("Pay cleared: INCENTIVE INR 10,000.00"))
                .andExpect(jsonPath("$.content[0].operation").value("Update profile"));
        mvc.perform(get("/api/audit").param("employeeId", String.valueOf(id)).param("subject", "PROFILE"))
                .andExpect(jsonPath("$.content[0].summary").value("Profile changed: department SALES → MARKETING"));
    }

    @Test
    void operationsThatChangeNothingAreNotRecorded() throws Exception {
        long id = createEmployee("ENGINEERING", "FTE", "IN");
        recordLeave(id, Map.of());

        send(put("/api/employees/{id}", id), Map.of("fullName", "Test Person", "jobTitle", "Tester",
                "department", "ENGINEERING", "employmentType", "FTE", "country", "IN"));

        auditOf(id).andExpect(jsonPath("$.totalElements").value(1)); // only "Add employee"
    }

    @Test
    void rejectedChangesLeaveNoAuditRecord() throws Exception {
        long intern = createEmployee("ENGINEERING", "INTERN", "IN");

        setPay(intern, "REGULAR", "1000").andExpect(status().isUnprocessableEntity());
        send(put("/api/employees/{id}/attendance/2026-11", intern), Map.of("UNPLANNED", 40))
                .andExpect(status().isBadRequest());

        auditOf(intern).andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void auditHistoryCannotBeRewritten() throws Exception {
        createEmployee("SALES", "FTE", "IN");

        assertThatThrownBy(() -> jdbc.update("UPDATE audit_event SET actor = 'someone else'"))
                .hasMessageContaining("audit_event is append-only");
    }

    @Test
    void auditCanBeFilteredAcrossEmployees() throws Exception {
        long a = createEmployee("SALES", "FTE", "IN");
        createEmployee("SALES", "FTE", "IN");
        setPay(a, "REGULAR", "1000");

        mvc.perform(get("/api/audit").param("subject", "PAY"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].employeeId").value(a));
        mvc.perform(get("/api/audit")).andExpect(jsonPath("$.totalElements").value(3));
    }

    private ResultActions auditOf(long employeeId) throws Exception {
        return mvc.perform(get("/api/audit").param("employeeId", String.valueOf(employeeId)));
    }

    private void recordLeave(long id, Map<String, Integer> leave) throws Exception {
        send(put("/api/employees/{id}/attendance/2026-11", id), leave).andExpect(status().isOk());
    }
}
