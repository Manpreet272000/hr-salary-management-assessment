package com.acme.hr.aspect;

import com.acme.hr.aspect.Audited;
import com.acme.hr.model.enums.AuditSubject;
import java.util.Map;

/**
 * Reads the current state of one {@link AuditSubject} for an employee, as display-ready
 * field → value pairs. The aspect diffs the snapshot taken before and after an operation, so features
 * expose their state here and never write audit records themselves.
 */
public interface AuditSnapshot {

    AuditSubject subject();

    /**
     * @param scope optional narrowing from {@link Audited#scope()}, e.g. the month for leave
     * @return the subject's state, or an empty map if there is none (e.g. no manager, employee not created yet)
     */
    Map<String, String> capture(long employeeId, Object scope);
}
