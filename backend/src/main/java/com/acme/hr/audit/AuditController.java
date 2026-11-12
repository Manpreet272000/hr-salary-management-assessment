package com.acme.hr.audit;

import com.acme.hr.common.PageResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Read-only audit trail. There is deliberately no endpoint to change or delete entries. */
@RestController
class AuditController {

    private final AuditEntryRepository entries;
    private final ObjectMapper json;

    AuditController(AuditEntryRepository entries, ObjectMapper json) {
        this.entries = entries;
        this.json = json;
    }

    /** Newest first; filter by employee and/or subject, e.g. {@code ?employeeId=42&subject=PAY}. */
    @GetMapping("/api/audit")
    @Transactional(readOnly = true)
    PageResponse<AuditEntryResponse> search(@RequestParam(required = false) Long employeeId,
                                            @RequestParam(required = false) AuditSubject subject,
                                            @PageableDefault(size = 20) Pageable pageable) {
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

    record AuditEntryResponse(long id, long employeeId, AuditSubject subject, String operation, String summary,
                              JsonNode details,
                              String actor, Instant occurredAt) {
    }
}
