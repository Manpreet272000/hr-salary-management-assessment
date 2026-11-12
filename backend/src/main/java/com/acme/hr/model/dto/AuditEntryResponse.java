package com.acme.hr.model.dto;

import com.acme.hr.model.enums.AuditSubject;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;

public record AuditEntryResponse(long id, long employeeId, AuditSubject subject, String operation, String summary,
        JsonNode details,
        String actor, Instant occurredAt) {
}
