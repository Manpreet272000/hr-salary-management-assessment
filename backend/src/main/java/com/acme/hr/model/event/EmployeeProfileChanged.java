package com.acme.hr.model.event;

import com.acme.hr.model.dto.EmployeeProfile;
import com.acme.hr.model.entity.Employee;

/**
 * Published by {@code EmployeeService} inside the update transaction so other services (e.g.
 * {@code CompensationService}) can react to a profile change without EmployeeService depending on them.
 */
public record EmployeeProfileChanged(Employee employee, EmployeeProfile previous) {
}
