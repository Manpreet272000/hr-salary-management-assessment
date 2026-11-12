package com.acme.hr.service.policy;

import com.acme.hr.model.entity.Employee;
import java.util.Optional;
import org.springframework.data.jpa.domain.Specification;

/**
 * One rule about who may manage whom. Rules are Spring beans collected by {@link ReportingPolicy},
 * so adding a rule (e.g. "managers must be in the same country") is one new class and nothing else.
 *
 * <p>A rule both validates an assignment and, where it can be expressed as a query, narrows the
 * manager picker, so the UI only offers valid managers without repeating the rules.
 */
public interface ReportingRule {

    /** Why {@code manager} may not manage {@code report}, or empty if this rule allows it. */
    Optional<String> violation(Employee report, Employee manager);

    /** Filter for valid manager candidates of {@code report}; null if the rule can't be expressed as a query. */
    default Specification<Employee> candidateFilter(Employee report) {
        return null;
    }
}
