package com.acme.hr.employee;

/**
 * Published inside the update transaction so other modules (e.g. compensation) can react without
 * the employee module depending on them.
 */
public record EmployeeProfileChanged(Employee employee, EmployeeProfile previous) {
}
