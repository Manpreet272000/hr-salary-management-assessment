package com.acme.hr.employee;

import com.acme.hr.common.AuditedEntity;
import com.acme.hr.common.Country;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "employee")
public class Employee extends AuditedEntity {

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "job_title", nullable = false)
    private String jobTitle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Department department;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type", nullable = false)
    private EmploymentType employmentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Country country;

    /** True while the person is employed by ACME. Inactive employees keep their history but are not paid. */
    @Column(nullable = false)
    private boolean active = true;

    protected Employee() {
    }

    public Employee(EmployeeProfile profile) {
        applyProfile(profile);
    }

    public final void applyProfile(EmployeeProfile profile) {
        this.fullName = profile.fullName();
        this.jobTitle = profile.jobTitle();
        this.department = profile.department();
        this.employmentType = profile.employmentType();
        this.country = profile.country();
    }

    public EmployeeProfile profile() {
        return new EmployeeProfile(fullName, jobTitle, department, employmentType, country);
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    public String code() {
        return EmployeeCode.format(getId());
    }

    public String getFullName() {
        return fullName;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public Department getDepartment() {
        return department;
    }

    public EmploymentType getEmploymentType() {
        return employmentType;
    }

    public Country getCountry() {
        return country;
    }

    public boolean isActive() {
        return active;
    }
}
