package com.acme.hr.model.enums;

import com.acme.hr.aspect.AuditSnapshot;

/** The parts of an employee's record whose changes are audited. Each has one {@link AuditSnapshot}. */
public enum AuditSubject {
    PROFILE("Profile"),
    STATUS("Status"),
    MANAGER("Manager"),
    PAY("Pay"),
    LEAVE("Leave");

    private final String label;

    AuditSubject(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
