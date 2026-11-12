package com.acme.hr.employee;

import com.acme.hr.common.BusinessRuleException;
import com.acme.hr.common.ResourceNotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EmployeeService {

    private final EmployeeRepository employees;
    private final ApplicationEventPublisher events;

    EmployeeService(EmployeeRepository employees, ApplicationEventPublisher events) {
        this.employees = employees;
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
        events.publishEvent(new EmployeeProfileChanged(employee, previous));
        return employee;
    }

    public Employee setActive(long id, boolean active) {
        Employee employee = get(id);
        if (active) {
            employee.activate();
        } else {
            employee.deactivate();
        }
        return employee;
    }
}
