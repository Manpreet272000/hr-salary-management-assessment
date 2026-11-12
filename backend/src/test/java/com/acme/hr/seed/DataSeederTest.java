package com.acme.hr.seed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.acme.hr.support.IntegrationTest;
import java.time.Clock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class DataSeederTest extends IntegrationTest {

    private static final int COUNT = 500;

    @Autowired
    private Clock clock;

    private DataSeeder seeder;

    @BeforeEach
    void setUp() {
        seeder = new DataSeeder(jdbc, new SeedProperties(true, COUNT, 42), clock);
    }

    @Test
    void seedsEmployeesWithPayThatTheApiCanRead() throws Exception {
        seeder.run(null);

        assertThat(count("employee")).isEqualTo(COUNT);
        assertThat(count("salary_component")).isGreaterThanOrEqualTo(COUNT);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM employee WHERE manager_id IS NULL", Long.class))
                .isEqualTo(9); // one head per department
        mvc.perform(get("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.monthlyGross").isNumber())
                .andExpect(jsonPath("$.createdAt").isString());
        mvc.perform(get("/api/employees/1/compensation/history"))
                .andExpect(jsonPath("$[0].effectiveFrom").isString());
    }

    @Test
    void doesNothingWhenEmployeesAlreadyExist() throws Exception {
        createEmployee("SALES", "FTE", "IN");

        seeder.run(null);

        assertThat(count("employee")).isEqualTo(1);
    }

    private long count(String table) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM " + table, Long.class);
    }
}
