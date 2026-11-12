package com.acme.hr.dao;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Bulk inserts for seeding an empty database. Uses JDBC batches rather than JPA because IDENTITY ids
 * stop Hibernate from batching, which would make 10k employees ~20x slower.
 */
@Repository
public class SeedDao {

    private final JdbcTemplate jdbc;

    SeedDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public long countEmployees() {
        Long count = jdbc.queryForObject("SELECT COUNT(*) FROM employee", Long.class);
        return count == null ? 0 : count;
    }

    /** Rows: id, full_name, job_title, department, employment_type, country, active, created_at, updated_at. */
    public void insertEmployees(List<Object[]> rows) {
        jdbc.batchUpdate("""
                INSERT INTO employee (id, full_name, job_title, department, employment_type, country, active, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)""", rows);
    }

    /** Rows: employee_id, pay_type, amount, currency, effective_from, created_at, updated_at. */
    public void insertSalaryComponents(List<Object[]> rows) {
        jdbc.batchUpdate("""
                INSERT INTO salary_component (employee_id, pay_type, amount, currency, effective_from, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?)""", rows);
    }

    /** Rows: manager_id, employee id. Run after all employees exist, as a manager may have a higher id. */
    public void assignManagers(List<Object[]> rows) {
        jdbc.batchUpdate("UPDATE employee SET manager_id = ? WHERE id = ?", rows);
    }
}
