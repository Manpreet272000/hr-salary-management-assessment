package com.acme.hr.seed;

import com.acme.hr.model.dto.EmployeeProfile;
import com.acme.hr.model.enums.EmploymentType;
import com.acme.hr.model.enums.PayType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * One generated employee with their current monthly pay components.
 *
 * @param level        seniority 0 (associate) to 4 (principal); drives pay and who manages whom
 * @param managerIndex position of their manager in the generated list, or null for a department head
 */
public record SeedEmployee(EmployeeProfile profile, boolean active, LocalDate joinedOn, Map<PayType, BigDecimal> pay,
                    int level, Integer managerIndex) {

    SeedEmployee withManager(Integer index) {
        return new SeedEmployee(profile, active, joinedOn, pay, level, index);
    }

    boolean canManage() {
        return active && profile.employmentType() == EmploymentType.FTE;
    }
}
