package com.acme.hr.service.policy;

import com.acme.hr.exception.BusinessRuleException;
import com.acme.hr.model.entity.Employee;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

/** Applies every {@link ReportingRule}; the single place that decides whether a reporting line is valid. */
@Component
public class ReportingPolicy {

    private final List<ReportingRule> rules;

    ReportingPolicy(List<ReportingRule> rules) {
        this.rules = rules;
    }

    public List<String> violations(Employee report, Employee manager) {
        return rules.stream().map(rule -> rule.violation(report, manager)).flatMap(Optional::stream).toList();
    }

    public void requireValid(Employee report, Employee manager) {
        List<String> violations = violations(report, manager);
        if (!violations.isEmpty()) {
            throw new BusinessRuleException(String.join("; ", violations));
        }
    }

    /** Everyone the rules would accept as {@code report}'s manager, as far as rules can express it as a query. */
    public Specification<Employee> candidates(Employee report) {
        return Specification.allOf(rules.stream().map(rule -> rule.candidateFilter(report)).filter(Objects::nonNull).toList());
    }
}
