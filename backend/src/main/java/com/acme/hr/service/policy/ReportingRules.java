package com.acme.hr.service.policy;

import com.acme.hr.dao.EmployeeRepository;
import com.acme.hr.model.entity.Employee;
import com.acme.hr.model.enums.EmploymentType;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.springframework.core.annotation.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

/** The reporting rules ACME applies today. Order controls the order of messages when several fail. */
public final class ReportingRules {

    private ReportingRules() {
    }

    @Component
    @Order(1)
    static class NotSelf implements ReportingRule {

        @Override
        public Optional<String> violation(Employee report, Employee manager) {
            return report.getId().equals(manager.getId())
                    ? Optional.of("An employee cannot report to themselves")
                    : Optional.empty();
        }

        @Override
        public Specification<Employee> candidateFilter(Employee report) {
            return (root, query, cb) -> cb.notEqual(root.get("id"), report.getId());
        }
    }

    @Component
    @Order(2)
    static class ManagerIsActive implements ReportingRule {

        @Override
        public Optional<String> violation(Employee report, Employee manager) {
            return manager.isActive()
                    ? Optional.empty()
                    : Optional.of("%s is inactive and cannot manage anyone".formatted(manager.code()));
        }

        @Override
        public Specification<Employee> candidateFilter(Employee report) {
            return (root, query, cb) -> cb.isTrue(root.get("active"));
        }
    }

    /** Interns and contractors don't manage people. */
    @Component
    @Order(3)
    static class ManagerIsFullTime implements ReportingRule {

        @Override
        public Optional<String> violation(Employee report, Employee manager) {
            return manager.getEmploymentType() == EmploymentType.FTE
                    ? Optional.empty()
                    : Optional.of("Only full-time employees can manage people; %s is %s"
                            .formatted(manager.code(), manager.getEmploymentType()));
        }

        @Override
        public Specification<Employee> candidateFilter(Employee report) {
            return (root, query, cb) -> cb.equal(root.get("employmentType"), EmploymentType.FTE);
        }
    }

    /** A sales person reports to a sales manager, not an engineering manager. */
    @Component
    @Order(4)
    static class SameDepartment implements ReportingRule {

        @Override
        public Optional<String> violation(Employee report, Employee manager) {
            return report.getDepartment() == manager.getDepartment()
                    ? Optional.empty()
                    : Optional.of("A %s employee must report to a manager in %s, not %s"
                            .formatted(report.getDepartment(), report.getDepartment(), manager.getDepartment()));
        }

        @Override
        public Specification<Employee> candidateFilter(Employee report) {
            return (root, query, cb) -> cb.equal(root.get("department"), report.getDepartment());
        }
    }

    /** Walks up the manager's chain; meeting the report there means the assignment would create a loop. */
    @Component
    @Order(5)
    static class NoCircularReporting implements ReportingRule {

        private final EmployeeRepository employees;

        NoCircularReporting(EmployeeRepository employees) {
            this.employees = employees;
        }

        @Override
        public Optional<String> violation(Employee report, Employee manager) {
            Set<Long> seen = new HashSet<>();
            Long current = manager.getManagerId();
            while (current != null && seen.add(current)) {
                if (current.equals(report.getId())) {
                    return Optional.of("%s already reports (directly or indirectly) to %s; this would create a loop"
                            .formatted(manager.code(), report.code()));
                }
                current = employees.findById(current).map(Employee::getManagerId).orElse(null);
            }
            return Optional.empty();
        }
    }
}
