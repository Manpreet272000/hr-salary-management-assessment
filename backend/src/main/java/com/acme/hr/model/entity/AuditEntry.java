package com.acme.hr.model.entity;

import com.acme.hr.model.enums.AuditSubject;
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
public class AuditEntry {

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

    public AuditEntry(long employeeId, AuditSubject subject, String operation, String summary, String details, String actor, Instant occurredAt) {
        this.employeeId = employeeId;
        this.subject = subject;
        this.operation = operation;
        this.summary = summary;
        this.details = details;
        this.actor = actor;
        this.occurredAt = occurredAt;
    }

    public Long getId() {
        return id;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public AuditSubject getSubject() {
        return subject;
    }

    public String getOperation() {
        return operation;
    }

    public String getSummary() {
        return summary;
    }

    public String getDetails() {
        return details;
    }

    public String getActor() {
        return actor;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
