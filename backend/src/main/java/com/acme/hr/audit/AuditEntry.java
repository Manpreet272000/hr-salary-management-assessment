package com.acme.hr.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** One immutable audit record. */
@Entity
@Table(name = "audit_event")
class AuditEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false, updatable = false)
    private Long employeeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private AuditSubject subject;

    @Column(nullable = false, updatable = false)
    private String operation;

    @Column(nullable = false, updatable = false)
    private String summary;

    @Column(nullable = false, updatable = false)
    private String details;

    @Column(nullable = false, updatable = false)
    private String actor;

    @Column(name = "occurred_at", nullable = false, updatable = false)
    private Instant occurredAt;

    protected AuditEntry() {
    }

    AuditEntry(long employeeId, AuditSubject subject, String operation, String summary, String details, String actor, Instant occurredAt) {
        this.employeeId = employeeId;
        this.subject = subject;
        this.operation = operation;
        this.summary = summary;
        this.details = details;
        this.actor = actor;
        this.occurredAt = occurredAt;
    }

    Long getId() {
        return id;
    }

    Long getEmployeeId() {
        return employeeId;
    }

    AuditSubject getSubject() {
        return subject;
    }

    String getOperation() {
        return operation;
    }

    String getSummary() {
        return summary;
    }

    String getDetails() {
        return details;
    }

    String getActor() {
        return actor;
    }

    Instant getOccurredAt() {
        return occurredAt;
    }
}
