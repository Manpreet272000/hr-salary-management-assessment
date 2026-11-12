package com.acme.hr.support;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * Full-stack tests against a throwaway SQLite file (real schema via Flyway, no mocks) with the clock
 * fixed at {@link #TODAY}. Tables are emptied before each test so tests are independent.
 */
@SpringBootTest(properties = "app.seed.enabled=false")
@AutoConfigureMockMvc
@Import(IntegrationTest.FixedClock.class)
public abstract class IntegrationTest {

    /** Wednesday 11 Nov 2026: November has 21 working days and a payroll cutoff of Fri 27 Nov. */
    public static final LocalDate TODAY = LocalDate.of(2026, 11, 11);

    private static final Path DB = createTempDb();

    @Autowired
    protected MockMvc mvc;
    @Autowired
    protected ObjectMapper json;
    @Autowired
    protected JdbcTemplate jdbc;

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:sqlite:" + DB + "?foreign_keys=true&journal_mode=WAL");
    }

    @BeforeEach
    void emptyTables() {
        jdbc.update("DELETE FROM monthly_attendance");
        jdbc.update("DELETE FROM salary_component");
        jdbc.update("DELETE FROM employee");
    }

    protected long createEmployee(String department, String employmentType, String country) throws Exception {
        Map<String, String> body = Map.of(
                "fullName", "Test Person", "jobTitle", "Tester",
                "department", department, "employmentType", employmentType, "country", country);
        return read(send(post("/api/employees"), body).andExpect(status().isCreated())).get("id").asLong();
    }

    protected ResultActions setPay(long employeeId, String payType, String amount) throws Exception {
        return send(put("/api/employees/{id}/compensation/{type}", employeeId, payType), Map.of("amount", amount));
    }

    protected ResultActions deactivate(long employeeId) throws Exception {
        return send(patch("/api/employees/{id}/status", employeeId), Map.of("active", false));
    }

    protected ResultActions send(MockHttpServletRequestBuilder request,
                                 Object body) throws Exception {
        return mvc.perform(request.contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body)));
    }

    protected JsonNode read(ResultActions result) throws Exception {
        return json.readTree(result.andReturn().getResponse().getContentAsString());
    }

    private static Path createTempDb() {
        try {
            Path file = Files.createTempFile("hr-test-", ".db");
            file.toFile().deleteOnExit();
            return file;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @TestConfiguration
    static class FixedClock {

        @Bean
        @Primary
        Clock fixedClock() {
            return Clock.fixed(Instant.parse("2026-11-11T10:00:00Z"), ZoneOffset.UTC);
        }
    }
}
