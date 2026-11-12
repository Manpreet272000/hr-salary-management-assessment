package com.acme.hr.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.acme.hr.support.IntegrationTest;
import org.junit.jupiter.api.Test;

class InsightsApiTest extends IntegrationTest {

    @Test
    void payStatsPerGroupAndCurrencyCountOnlyActiveEmployees() throws Exception {
        employee("ENGINEERING", "IN", "100000");
        employee("ENGINEERING", "IN", "200000");
        employee("ENGINEERING", "IN", "600000");
        employee("ENGINEERING", "US", "9000");
        long leaver = employee("ENGINEERING", "IN", "999999");
        deactivate(leaver);

        mvc.perform(get("/api/insights/pay").param("groupBy", "DEPARTMENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].currency").value("INR"))
                .andExpect(jsonPath("$[0].headcount").value(3))
                .andExpect(jsonPath("$[0].min").value(100000.0))
                .andExpect(jsonPath("$[0].median").value(200000.0))
                .andExpect(jsonPath("$[0].average").value(300000.0))
                .andExpect(jsonPath("$[0].max").value(600000.0))
                .andExpect(jsonPath("$[0].totalMonthlyPayroll").value(900000.0))
                .andExpect(jsonPath("$[1].currency").value("USD"));
    }

    @Test
    void medianOfAnEvenGroupIsTheMeanOfTheMiddleTwo() throws Exception {
        employee("FINANCE", "GB", "1000");
        employee("FINANCE", "GB", "2000");
        employee("FINANCE", "GB", "4000");
        employee("FINANCE", "GB", "9000");

        mvc.perform(get("/api/insights/pay").param("groupBy", "COUNTRY"))
                .andExpect(jsonPath("$[0].group").value("GB"))
                .andExpect(jsonPath("$[0].median").value(3000.0));
    }

    @Test
    void employeePayIncludesAllCurrentComponentsAndCanBeFilteredByCountry() throws Exception {
        long rep = createEmployee("SALES", "FTE", "IN");
        setPay(rep, "REGULAR", "50000");
        setPay(rep, "INCENTIVE", "10000");
        employee("SALES", "US", "5000");

        mvc.perform(get("/api/insights/pay").param("groupBy", "DEPARTMENT").param("country", "IN"))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].headcount").value(1))
                .andExpect(jsonPath("$[0].totalMonthlyPayroll").value(60000.0));
    }

    @Test
    void headcountSplitsActiveAndInactive() throws Exception {
        employee("SALES", "IN", "1000");
        deactivate(employee("SALES", "IN", "1000"));
        employee("DESIGN", "IN", "1000");

        mvc.perform(get("/api/insights/headcount").param("groupBy", "DEPARTMENT"))
                .andExpect(jsonPath("$[0].group").value("DESIGN"))
                .andExpect(jsonPath("$[0].active").value(1))
                .andExpect(jsonPath("$[1].group").value("SALES"))
                .andExpect(jsonPath("$[1].active").value(1))
                .andExpect(jsonPath("$[1].inactive").value(1));
    }

    @Test
    void rejectsUnknownGrouping() throws Exception {
        mvc.perform(get("/api/insights/pay").param("groupBy", "salary; DROP TABLE employee"))
                .andExpect(status().isBadRequest());
    }

    private long employee(String department, String country, String regularPay) throws Exception {
        long id = createEmployee(department, "FTE", country);
        setPay(id, "REGULAR", regularPay);
        return id;
    }
}
