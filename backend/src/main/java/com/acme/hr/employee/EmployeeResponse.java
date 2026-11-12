package com.acme.hr.employee;

import com.acme.hr.common.Country;
import java.math.BigDecimal;
import java.time.Instant;

public record EmployeeResponse(
        long id,
        String code,
        String fullName,
        String jobTitle,
        Department department,
        EmploymentType employmentType,
        Country country,
        String currency,
        boolean active,
        Long managerId,
        BigDecimal monthlyGross,
        Instant createdAt,
        Instant updatedAt) {

    static EmployeeResponse of(Employee e, BigDecimal monthlyGross) {
        return new EmployeeResponse(
                e.getId(), e.code(), e.getFullName(), e.getJobTitle(), e.getDepartment(),
                e.getEmploymentType(), e.getCountry(), e.getCountry().currency(), e.isActive(), e.getManagerId(),
                monthlyGross, e.getCreatedAt(), e.getUpdatedAt());
    }
}
