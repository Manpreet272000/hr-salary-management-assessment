package com.acme.hr.dao;

import com.acme.hr.model.entity.AuditEntry;
import com.acme.hr.model.enums.AuditSubject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AuditEntryRepository extends JpaRepository<AuditEntry, Long> {

    @Query("""
            select a from AuditEntry a
            where (:employeeId is null or a.employeeId = :employeeId)
              and (:subject is null or a.subject = :subject)
            order by a.occurredAt desc, a.id desc
            """)
    Page<AuditEntry> search(Long employeeId, AuditSubject subject, Pageable pageable);
}
