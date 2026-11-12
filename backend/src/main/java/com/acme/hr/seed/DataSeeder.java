package com.acme.hr.seed;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seeds an empty database with {@code app.seed.employee-count} employees. Uses JDBC batch inserts in
 * a single transaction (about a second for 10k employees on SQLite) instead of row-by-row JPA saves.
 */
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final JdbcTemplate jdbc;
    private final SeedProperties properties;
    private final Clock clock;

    DataSeeder(JdbcTemplate jdbc, SeedProperties properties, Clock clock) {
        this.jdbc = jdbc;
        this.properties = properties;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Long existing = jdbc.queryForObject("SELECT COUNT(*) FROM employee", Long.class);
        if (existing != null && existing > 0) {
            log.info("Skipping seed: {} employees already present", existing);
            return;
        }
        long started = System.currentTimeMillis();
        List<SeedEmployee> employees = new SeedDataGenerator(properties.randomSeed(), LocalDate.now(clock))
                .generate(properties.employeeCount());
        insert(employees);
        log.info("Seeded {} employees in {} ms", employees.size(), System.currentTimeMillis() - started);
    }

    private void insert(List<SeedEmployee> employees) {
        ZoneId zone = clock.getZone();
        List<Object[]> employeeRows = new ArrayList<>(employees.size());
        List<Object[]> payRows = new ArrayList<>();
        // The table is empty, so ids are assigned explicitly (1..n) and reused as salary foreign keys.
        for (int i = 0; i < employees.size(); i++) {
            long id = i + 1L;
            SeedEmployee e = employees.get(i);
            Timestamp joined = Timestamp.from(e.joinedOn().atStartOfDay(zone).toInstant());
            employeeRows.add(new Object[] {
                id, e.profile().fullName(), e.profile().jobTitle(), e.profile().department().name(),
                e.profile().employmentType().name(), e.profile().country().name(), e.active(), joined, joined});
            e.pay().forEach((payType, amount) -> payRows.add(new Object[] {
                id, payType.name(), amount, e.profile().country().currency(), Date.valueOf(e.joinedOn()), joined, joined}));
        }
        jdbc.batchUpdate("""
                INSERT INTO employee (id, full_name, job_title, department, employment_type, country, active, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)""", employeeRows);
        jdbc.batchUpdate("""
                INSERT INTO salary_component (employee_id, pay_type, amount, currency, effective_from, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?)""", payRows);
    }
}
