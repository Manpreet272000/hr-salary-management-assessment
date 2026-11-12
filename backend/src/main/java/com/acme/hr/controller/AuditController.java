package com.acme.hr.controller;

import com.acme.hr.model.dto.AuditEntryResponse;
import com.acme.hr.model.dto.PageResponse;
import com.acme.hr.model.enums.AuditSubject;
import com.acme.hr.service.AuditService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Read-only audit trail. There is deliberately no endpoint to change or delete entries. */
@RestController
public class AuditController {

    private final AuditService audit;

    AuditController(AuditService audit) {
        this.audit = audit;
    }

    /** Newest first; filter by employee and/or subject, e.g. {@code ?employeeId=42&subject=PAY}. */
    @GetMapping("/api/audit")
    PageResponse<AuditEntryResponse> search(@RequestParam(required = false) Long employeeId,
                                            @RequestParam(required = false) AuditSubject subject,
                                            @PageableDefault(size = 20) Pageable pageable) {
        return audit.search(employeeId, subject, pageable);
    }
}
