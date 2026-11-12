package com.acme.hr.seed;

import com.acme.hr.compensation.PayType;
import com.acme.hr.employee.EmployeeProfile;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/** One generated employee with their current monthly pay components. */
record SeedEmployee(EmployeeProfile profile, boolean active, LocalDate joinedOn, Map<PayType, BigDecimal> pay) {
}
