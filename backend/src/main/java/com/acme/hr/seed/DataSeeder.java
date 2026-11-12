package com.acme.hr.seed;

import com.acme.hr.config.SeedProperties;
import com.acme.hr.dao.SeedDao;
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
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seeds an empty database with {@code app.seed.employee-count} employees: generates them, then writes
 * them through {@link SeedDao} batch inserts in one transaction (about 1.5 s for 10k on SQLite).
 */
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final SeedDao seedDao;
    private final SeedProperties properties;
    private final Clock clock;

    DataSeeder(SeedDao seedDao, SeedProperties properties, Clock clock) {
        this.seedDao = seedDao;
        this.properties = properties;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        long existing = seedDao.countEmployees();
        if (existing > 0) {
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
        List<Object[]> managerRows = new ArrayList<>();
        // The table is empty, so ids are assigned explicitly (1..n) and reused as salary foreign keys.
        for (int i = 0; i < employees.size(); i++) {
            long id = i + 1L;
            SeedEmployee e = employees.get(i);
            Timestamp joined = Timestamp.from(e.joinedOn().atStartOfDay(zone).toInstant());
            employeeRows.add(new Object[] {
                id, e.profile().fullName(), e.profile().jobTitle(), e.profile().department().name(),
                e.profile().employmentType().name(), e.profile().country().name(), e.active(), joined, joined});
            if (e.managerIndex() != null) {
                managerRows.add(new Object[] {e.managerIndex() + 1L, id});
            }
            e.pay().forEach((payType, amount) -> payRows.add(new Object[] {
                id, payType.name(), amount, e.profile().country().currency(), Date.valueOf(e.joinedOn()), joined, joined}));
        }
        seedDao.insertEmployees(employeeRows);
        seedDao.insertSalaryComponents(payRows);
        seedDao.assignManagers(managerRows);
    }
}
