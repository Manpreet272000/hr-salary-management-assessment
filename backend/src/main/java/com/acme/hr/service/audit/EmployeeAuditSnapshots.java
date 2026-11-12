package com.acme.hr.service.audit;

import com.acme.hr.aspect.AuditSnapshot;
import com.acme.hr.dao.EmployeeRepository;
import com.acme.hr.model.entity.Employee;
import com.acme.hr.model.enums.AuditSubject;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/** How an employee's profile, status and manager look to {@code AuditAspect}. */
public final class EmployeeAuditSnapshots {

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
