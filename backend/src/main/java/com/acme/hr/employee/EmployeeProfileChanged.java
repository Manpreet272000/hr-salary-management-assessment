package com.acme.hr.employee;

/**
 * Published inside the update transaction (only when something changed) so other modules, such as
 * compensation, can react without the employee module depending on them.
 */
public record EmployeeProfileChanged(Employee employee, EmployeeProfile previous) {
}
