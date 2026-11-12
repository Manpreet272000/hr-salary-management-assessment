package com.acme.hr.model.dto;

import com.acme.hr.model.entity.Employee;
import com.acme.hr.model.enums.Country;
import com.acme.hr.model.enums.Department;
import com.acme.hr.model.enums.EmploymentType;
import com.acme.hr.model.event.EmployeeProfileChanged;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * The HR-editable facts about an employee. Used as the create/update request body, as the state
 * held by {@link Employee}, and as the "before" snapshot in {@link EmployeeProfileChanged}.
 */
public record EmployeeProfile(
        @NotBlank @Size(max = 120) String fullName,
        @NotBlank @Size(max = 120) String jobTitle,
        @NotNull Department department,
        @NotNull EmploymentType employmentType,
        @NotNull Country country) {

    public EmployeeProfile {
        fullName = fullName == null ? null : fullName.strip();
        jobTitle = jobTitle == null ? null : jobTitle.strip();
    }

    /** True when a change could invalidate the employee's current pay structure. */
    public boolean payStructureDiffersFrom(EmployeeProfile other) {
        return department != other.department
                || employmentType != other.employmentType
                || country != other.country;
    }
}
