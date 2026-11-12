package com.acme.hr.service;

import com.acme.hr.dao.AuditEntryRepository;
import com.acme.hr.model.dto.AuditEntryResponse;
import com.acme.hr.model.dto.PageResponse;
import com.acme.hr.model.entity.AuditEntry;
import com.acme.hr.model.enums.AuditSubject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Reads the audit trail. Writing is done only by {@code AuditAspect}. */
@Service
@Transactional(readOnly = true)
public class AuditService {

    private final AuditEntryRepository entries;
    private final ObjectMapper json;

    AuditService(AuditEntryRepository entries, ObjectMapper json) {
        this.entries = entries;
        this.json = json;
    }

    /** Newest first; null filters mean "any". */
    public PageResponse<AuditEntryResponse> search(Long employeeId, AuditSubject subject, Pageable pageable) {
        return PageResponse.of(entries.search(employeeId, subject, pageable), this::toResponse);
    }

    private AuditEntryResponse toResponse(AuditEntry e) {
        return new AuditEntryResponse(e.getId(), e.getEmployeeId(), e.getSubject(), e.getOperation(), e.getSummary(),
                parse(e.getDetails()), e.getActor(), e.getOccurredAt());
    }

    private JsonNode parse(String details) {
        try {
            return json.readTree(details);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Stored audit details are not valid JSON", e);
        }
    }
}
