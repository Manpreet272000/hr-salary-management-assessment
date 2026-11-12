package com.acme.hr.employee;

import com.acme.hr.audit.AuditSubject;
import com.acme.hr.audit.Audited;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Reporting lines: who someone reports to, who reports to them, and changing their manager. */
@Service
@Transactional
public class ReportingService {

    static final int MAX_CANDIDATES = 20;

    private final EmployeeService employees;
    private final EmployeeRepository repository;
    private final ReportingPolicy policy;

    ReportingService(EmployeeService employees, EmployeeRepository repository, ReportingPolicy policy) {
        this.employees = employees;
        this.repository = repository;
        this.policy = policy;
    }

    /** Sets (or with a null id, removes) the employee's manager after checking every reporting rule. */
    @Audited(operation = "Change manager", subjects = AuditSubject.MANAGER)
    public Employee assignManager(long employeeId, Long managerId) {
        Employee employee = employees.getActive(employeeId);
        if (managerId == null) {
            employee.reportTo(null);
            return employee;
        }
        Employee manager = employees.get(managerId);
        policy.requireValid(employee, manager);
        employee.reportTo(manager.getId());
        return employee;
    }

    /** Managers above the employee, from the top of the hierarchy down to their direct manager. */
    @Transactional(readOnly = true)
    public List<Employee> managementChain(long employeeId) {
        List<Employee> chain = new ArrayList<>();
        Set<Long> seen = new HashSet<>();
        Long next = employees.get(employeeId).getManagerId();
        while (next != null && seen.add(next)) {
            Employee manager = employees.get(next);
            chain.add(manager);
            next = manager.getManagerId();
        }
        Collections.reverse(chain);
        return chain;
    }

    @Transactional(readOnly = true)
    public List<Employee> directReports(long employeeId) {
        employees.get(employeeId);
        return repository.findByManagerIdOrderByFullName(employeeId);
    }

    /** Valid managers for the employee, optionally matching a name/title/code search. */
    @Transactional(readOnly = true)
    public List<Employee> managerCandidates(long employeeId, String query) {
        Employee employee = employees.get(employeeId);
        var filter = policy.candidates(employee).and(EmployeeSpecifications.textQuery(query));
        return repository.findAll(filter, PageRequest.of(0, MAX_CANDIDATES, Sort.by("fullName"))).getContent();
    }
}
