package com.acme.hr.aspect;

import com.acme.hr.dao.AuditEntryRepository;
import com.acme.hr.model.entity.AuditEntry;
import com.acme.hr.model.enums.AuditSubject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Method;
import java.time.Clock;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Cross-cutting audit for {@link Audited} methods: snapshot → proceed → snapshot → record the diff.
 *
 * <p>The aspect opens (or joins) the transaction itself, so the change and its audit records commit
 * together, and an operation that fails leaves no audit record. Nothing is recorded when the
 * operation changed nothing.
 */
@Aspect
@Component
public class AuditAspect {

    private static final String NONE = "(none)";

    private final Map<AuditSubject, AuditSnapshot> snapshots;
    private final AuditEntryRepository entries;
    private final CurrentActor actor;
    private final ObjectMapper json;
    private final Clock clock;
    private final TransactionTemplate transaction;
    private final ExpressionParser parser = new SpelExpressionParser();
    private final ParameterNameDiscoverer parameterNames = new DefaultParameterNameDiscoverer();

    AuditAspect(List<AuditSnapshot> snapshots, AuditEntryRepository entries, CurrentActor actor, ObjectMapper json,
                Clock clock, TransactionTemplate transaction) {
        this.snapshots = new EnumMap<>(snapshots.stream()
                .collect(Collectors.toMap(AuditSnapshot::subject, Function.identity())));
        this.entries = entries;
        this.actor = actor;
        this.json = json;
        this.clock = clock;
        this.transaction = transaction;
    }

    @Around("@annotation(audited)")
    Object audit(ProceedingJoinPoint call, Audited audited) throws Throwable {
        try {
            return transaction.execute(status -> auditInTransaction(call, audited));
        } catch (InvocationFailure failure) {
            throw failure.getCause();
        }
    }

    private Object auditInTransaction(ProceedingJoinPoint call, Audited audited) {
        Method method = ((MethodSignature) call.getSignature()).getMethod();
        var context = new MethodBasedEvaluationContext(call.getTarget(), method, call.getArgs(), parameterNames);
        Object scope = audited.scope().isEmpty() ? null : parser.parseExpression(audited.scope()).getValue(context);

        Long employeeBefore = parser.parseExpression(audited.employeeId()).getValue(context, Long.class);
        Map<AuditSubject, Map<String, String>> before = capture(audited, employeeBefore, scope);

        Object result;
        try {
            result = call.proceed();
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Throwable t) {
            throw new InvocationFailure(t);
        }

        context.setVariable("result", result);
        Long employeeId = parser.parseExpression(audited.employeeId()).getValue(context, Long.class);
        Map<AuditSubject, Map<String, String>> after = capture(audited, employeeId, scope);
        for (AuditSubject subject : audited.subjects()) {
            record(employeeId, subject, audited.operation(), before.get(subject), after.get(subject));
        }
        return result;
    }

    private Map<AuditSubject, Map<String, String>> capture(Audited audited, Long employeeId, Object scope) {
        Map<AuditSubject, Map<String, String>> state = new EnumMap<>(AuditSubject.class);
        for (AuditSubject subject : audited.subjects()) {
            state.put(subject, employeeId == null ? Map.of() : snapshotFor(subject).capture(employeeId, scope));
        }
        return state;
    }

    private void record(long employeeId, AuditSubject subject, String operation,
                        Map<String, String> before, Map<String, String> after) {
        Set<String> fields = new LinkedHashSet<>(before.keySet());
        fields.addAll(after.keySet());
        Map<String, String> changedFrom = new LinkedHashMap<>();
        Map<String, String> changedTo = new LinkedHashMap<>();
        for (String field : fields) {
            if (!Objects.equals(before.get(field), after.get(field))) {
                changedFrom.put(field, before.get(field));
                changedTo.put(field, after.get(field));
            }
        }
        if (changedTo.isEmpty()) {
            return;
        }
        String summary = summarize(subject, before, after, changedFrom, changedTo);
        entries.save(new AuditEntry(employeeId, subject, operation, summary,
                toJson(Map.of("before", changedFrom, "after", changedTo)), actor.name(), clock.instant()));
    }

    /** e.g. "Pay changed: REGULAR INR 80,000.00 → INR 90,000.00". */
    static String summarize(AuditSubject subject, Map<String, String> before, Map<String, String> after,
                            Map<String, String> changedFrom, Map<String, String> changedTo) {
        if (before.isEmpty()) {
            return "%s set: %s".formatted(subject.label(), changedTo.entrySet().stream()
                    .map(e -> e.getKey() + " " + e.getValue()).collect(Collectors.joining(", ")));
        }
        if (after.isEmpty()) {
            return "%s cleared: %s".formatted(subject.label(), changedFrom.entrySet().stream()
                    .map(e -> e.getKey() + " " + e.getValue()).collect(Collectors.joining(", ")));
        }
        return "%s changed: %s".formatted(subject.label(), changedTo.keySet().stream()
                .map(field -> "%s %s → %s".formatted(field, orNone(changedFrom.get(field)), orNone(changedTo.get(field))))
                .collect(Collectors.joining("; ")));
    }

    private static String orNone(String value) {
        return value == null ? NONE : value;
    }

    private AuditSnapshot snapshotFor(AuditSubject subject) {
        AuditSnapshot snapshot = snapshots.get(subject);
        if (snapshot == null) {
            throw new IllegalStateException("No AuditSnapshot bean for " + subject);
        }
        return snapshot;
    }

    private String toJson(Object details) {
        try {
            return json.writeValueAsString(details);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Audit details are not serializable", e);
        }
    }

    /** Carries a checked exception out of the transaction callback unchanged. */
    private static final class InvocationFailure extends RuntimeException {
        InvocationFailure(Throwable cause) {
            super(cause);
        }
    }
}
