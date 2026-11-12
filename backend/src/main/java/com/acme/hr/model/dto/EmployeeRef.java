package com.acme.hr.model.dto;

import com.acme.hr.model.entity.Employee;
import com.acme.hr.model.enums.Department;
import com.acme.hr.model.enums.EmploymentType;

public record EmployeeRef(long id, String code, String fullName, String jobTitle, Department department,
        EmploymentType employmentType, boolean active) {

    public static EmployeeRef of(Employee e) {
        return new EmployeeRef(e.getId(), e.code(), e.getFullName(), e.getJobTitle(), e.getDepartment(),
                e.getEmploymentType(), e.isActive());
    }
}
