package com.acme.hr.controller;

import static org.hamcrest.Matchers.hasKey;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.acme.hr.model.entity.Employee;
import com.acme.hr.support.IntegrationTest;
import com.acme.hr.util.EmployeeCode;
import java.util.Map;
import org.junit.jupiter.api.Test;

class EmployeeApiTest extends IntegrationTest {

    @Test
    void createsAnActiveEmployeeWithDerivedCodeAndCurrency() throws Exception {
        send(post("/api/employees"), Map.of(
                "fullName", "  Asha Rao ", "jobTitle", "Engineer",
                "department", "ENGINEERING", "employmentType", "FTE", "country", "IN"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(org.hamcrest.Matchers.startsWith("EMP")))
                .andExpect(jsonPath("$.fullName").value("Asha Rao"))
                .andExpect(jsonPath("$.currency").value("INR"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.monthlyGross").value(0));
    }

    @Test
    void rejectsInvalidProfileWithFieldErrors() throws Exception {
        send(post("/api/employees"), Map.of("fullName", "", "jobTitle", "Engineer"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasKey("fullName")))
                .andExpect(jsonPath("$.errors", hasKey("department")));
    }

    @Test
    void rejectsUnknownEnumValues() throws Exception {
        send(post("/api/employees"), Map.of(
                "fullName", "A", "jobTitle", "B", "department", "SPACE", "employmentType", "FTE", "country", "IN"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returns404ForUnknownEmployee() throws Exception {
        mvc.perform(get("/api/employees/{id}", 999_999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Employee EMP999999 was not found"));
    }

    @Test
    void deactivatesAndReactivatesAnEmployee() throws Exception {
        long id = createEmployee("ENGINEERING", "FTE", "IN");

        deactivate(id).andExpect(status().isOk()).andExpect(jsonPath("$.active").value(false));
        send(patch("/api/employees/{id}/status", id), Map.of("active", true))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void searchFiltersByStatusDepartmentCountryAndText() throws Exception {
        long salesIn = createEmployee("SALES", "FTE", "IN");
        createEmployee("SALES", "FTE", "US");
        long leaver = createEmployee("ENGINEERING", "FTE", "IN");
        deactivate(leaver);
        setPay(salesIn, "REGULAR", "50000");

        mvc.perform(get("/api/employees").param("department", "SALES").param("country", "IN"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(salesIn))
                .andExpect(jsonPath("$.content[0].monthlyGross").value(50000.0));
        mvc.perform(get("/api/employees").param("active", "false"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(leaver));
        mvc.perform(get("/api/employees").param("query", EmployeeCode.format(leaver)))
                .andExpect(jsonPath("$.totalElements").value(1));
        mvc.perform(get("/api/employees").param("query", "test per"))
                .andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    void pagesAndSortsResults() throws Exception {
        for (int i = 0; i < 3; i++) {
            createEmployee("FINANCE", "FTE", "GB");
        }

        mvc.perform(get("/api/employees").param("size", "2").param("sort", "id,desc"))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void rejectsSortingByUnknownField() throws Exception {
        mvc.perform(get("/api/employees").param("sort", "password"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updatesProfile() throws Exception {
        long id = createEmployee("ENGINEERING", "FTE", "IN");

        send(put("/api/employees/{id}", id), Map.of(
                "fullName", "Renamed", "jobTitle", "Senior Engineer",
                "department", "ENGINEERING", "employmentType", "FTE", "country", "IN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Renamed"))
                .andExpect(jsonPath("$.jobTitle").value("Senior Engineer"));
    }
}
