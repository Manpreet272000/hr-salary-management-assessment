package com.acme.hr.employee;

import com.acme.hr.common.Country;

/** Optional filters for the employee directory; null means "any". */
public record EmployeeSearchCriteria(
        String query,
        Department department,
        EmploymentType employmentType,
        Country country,
        Boolean active) {
}
