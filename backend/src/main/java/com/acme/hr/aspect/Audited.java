package com.acme.hr.aspect;

import com.acme.hr.model.enums.AuditSubject;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a use-case method whose effects must be audited. {@link AuditAspect} snapshots the listed
 * subjects before and after the call and records each subject that changed, in the same transaction.
 *
 * <p>Put it on the outermost operation HR invokes (not on helpers it calls), so each change is
 * recorded once. Side effects inside the operation, such as pay ended by a department change, are
 * captured by listing their subject here.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Audited {

    /** Human-readable name of the operation, stored with every record, e.g. "Revise pay". */
    String operation();

    AuditSubject[] subjects();

    /** SpEL for the affected employee's id. Use {@code #result?.id} when the method creates the employee. */
    String employeeId() default "#employeeId";

    /** Optional SpEL passed to the snapshots, e.g. {@code #month}. */
    String scope() default "";
}
