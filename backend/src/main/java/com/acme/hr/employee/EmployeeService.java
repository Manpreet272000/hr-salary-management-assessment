package com.acme.hr.employee;

import com.acme.hr.common.BusinessRuleException;
import com.acme.hr.common.ResourceNotFoundException;
import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EmployeeService {

    private final EmployeeRepository employees;
    private final ReportingPolicy reporting;
    private final ApplicationEventPublisher events;

    EmployeeService(EmployeeRepository employees, ReportingPolicy reporting, ApplicationEventPublisher events) {
        this.employees = employees;
        this.reporting = reporting;
        this.events = events;
    }

    @Transactional(readOnly = true)
    public Page<Employee> search(EmployeeSearchCriteria criteria, Pageable pageable) {
        return employees.findAll(EmployeeSpecifications.matching(criteria), pageable);
    }

    @Transactional(readOnly = true)
    public Employee get(long id) {
        return employees.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", EmployeeCode.format(id)));
    }

    /** For operations that only make sense for people currently employed (pay changes, attendance, payouts). */
    @Transactional(readOnly = true)
    public Employee getActive(long id) {
        Employee employee = get(id);
        if (!employee.isActive()) {
            throw new BusinessRuleException(
                    "Employee %s is inactive; reactivate them first".formatted(employee.code()));
        }
        return employee;
    }

    public Employee create(EmployeeProfile profile) {
        return employees.save(new Employee(profile));
    }

    public Employee updateProfile(long id, EmployeeProfile profile) {
        Employee employee = get(id);
        EmployeeProfile previous = employee.profile();
        employee.applyProfile(profile);
        requireStillValidManagerOfReports(employee);
        dropManagerIfNoLongerValid(employee);
        events.publishEvent(new EmployeeProfileChanged(employee, previous));
        return employee;
    }

    public Employee setActive(long id, boolean active) {
        Employee employee = get(id);
        if (active) {
            employee.activate();
            dropManagerIfNoLongerValid(employee);
        } else {
            long reports = employees.countByManagerId(id);
            if (reports > 0) {
                throw new BusinessRuleException("%s manages %d people; assign them a new manager before marking %s inactive"
                        .formatted(employee.code(), reports, employee.code()));
            }
            employee.deactivate();
        }
        return employee;
    }

    /**
     * A change to a manager (e.g. moving them out of Sales) must not leave their team with an
     * invalid manager. HR reassigns the team first, so nobody's reporting line changes silently.
     */
    private void requireStillValidManagerOfReports(Employee manager) {
        List<Employee> reports = employees.findByManagerIdOrderByFullName(manager.getId());
        List<String> problems = reports.stream()
                .flatMap(report -> reporting.violations(report, manager).stream())
                .distinct()
                .toList();
        if (!problems.isEmpty()) {
            throw new BusinessRuleException("%s has %d direct reports who could no longer report to them (%s); reassign them first"
                    .formatted(manager.code(), reports.size(), String.join("; ", problems)));
        }
    }

    /**
     * The employee's own reporting line ends if it is no longer valid (e.g. they moved department, or
     * were reactivated after their old manager left); HR then picks a new manager.
     */
    private void dropManagerIfNoLongerValid(Employee employee) {
        if (employee.getManagerId() == null) {
            return;
        }
        employees.findById(employee.getManagerId())
                .filter(manager -> !reporting.violations(employee, manager).isEmpty())
                .ifPresent(manager -> employee.reportTo(null));
    }
}
