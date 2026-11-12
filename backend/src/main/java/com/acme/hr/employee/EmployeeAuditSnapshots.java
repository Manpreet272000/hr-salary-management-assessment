package com.acme.hr.employee;

import com.acme.hr.audit.AuditSnapshot;
import com.acme.hr.audit.AuditSubject;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/** How the employee module's audited state looks to {@code AuditAspect}. */
final class EmployeeAuditSnapshots {

    private EmployeeAuditSnapshots() {
    }

    @Component
    static class Profile implements AuditSnapshot {

        private final EmployeeRepository employees;

        Profile(EmployeeRepository employees) {
            this.employees = employees;
        }

        @Override
        public AuditSubject subject() {
            return AuditSubject.PROFILE;
        }

        @Override
        public Map<String, String> capture(long employeeId, Object scope) {
            return employees.findById(employeeId).map(e -> {
                Map<String, String> state = new LinkedHashMap<>();
                state.put("name", e.getFullName());
                state.put("job title", e.getJobTitle());
                state.put("department", e.getDepartment().name());
                state.put("employment type", e.getEmploymentType().name());
                state.put("country", e.getCountry().name());
                return state;
            }).orElse(Map.of());
        }
    }

    @Component
    static class Status implements AuditSnapshot {

        private final EmployeeRepository employees;

        Status(EmployeeRepository employees) {
            this.employees = employees;
        }

        @Override
        public AuditSubject subject() {
            return AuditSubject.STATUS;
        }

        @Override
        public Map<String, String> capture(long employeeId, Object scope) {
            return employees.findById(employeeId)
                    .map(e -> Map.of("status", e.isActive() ? "Active" : "Inactive"))
                    .orElse(Map.of());
        }
    }

    @Component
    static class Manager implements AuditSnapshot {

        private final EmployeeRepository employees;

        Manager(EmployeeRepository employees) {
            this.employees = employees;
        }

        @Override
        public AuditSubject subject() {
            return AuditSubject.MANAGER;
        }

        @Override
        public Map<String, String> capture(long employeeId, Object scope) {
            return employees.findById(employeeId)
                    .map(Employee::getManagerId)
                    .flatMap(employees::findById)
                    .map(m -> Map.of("reports to", "%s (%s)".formatted(m.getFullName(), m.code())))
                    .orElse(Map.of());
        }
    }
}
