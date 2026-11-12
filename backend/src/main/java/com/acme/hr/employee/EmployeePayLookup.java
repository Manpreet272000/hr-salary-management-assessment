package com.acme.hr.employee;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;

/**
 * Lets the directory show each employee's current monthly pay without the employee module
 * depending on the compensation module (implemented there).
 */
public interface EmployeePayLookup {

    /** Current monthly gross per employee id; employees with no pay components are absent. */
    Map<Long, BigDecimal> monthlyGross(Collection<Long> employeeIds);
}
