package com.acme.hr.model.dto;

import com.acme.hr.model.enums.Country;
import com.acme.hr.model.enums.Department;
import com.acme.hr.model.enums.EmploymentType;

/** Optional filters for the employee directory; null means "any". */
public record EmployeeSearchCriteria(
        String query,
        Department department,
        EmploymentType employmentType,
        Country country,
        Boolean active) {
}
