package com.acme.hr.service;

import com.acme.hr.model.dto.EmployeeProfile;
import com.acme.hr.model.dto.EmployeeResponse;
import com.acme.hr.model.dto.EmployeeSearchCriteria;
import com.acme.hr.model.dto.PageResponse;
import com.acme.hr.model.entity.Employee;
import com.acme.hr.util.Money;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * The employee directory as the UI sees it: profile plus current monthly gross. Combines
 * {@link EmployeeService} (profile rules) and {@link CompensationService} (pay) so controllers deal
 * only in response DTOs. Writes delegate to EmployeeService, so validation and auditing still apply.
 */
@Service
public class EmployeeDirectoryService {

    private final EmployeeService employees;
    private final CompensationService compensation;

    EmployeeDirectoryService(EmployeeService employees, CompensationService compensation) {
        this.employees = employees;
        this.compensation = compensation;
    }

    public PageResponse<EmployeeResponse> search(EmployeeSearchCriteria criteria, Pageable pageable) {
        Page<Employee> page = employees.search(criteria, pageable);
        // One grouped query for the whole page's pay, not one per row.
        Map<Long, BigDecimal> gross = compensation.monthlyGross(page.map(Employee::getId).getContent());
        return PageResponse.of(page, e -> EmployeeResponse.of(e, gross.getOrDefault(e.getId(), Money.zero())));
    }

    public EmployeeResponse get(long id) {
        return toResponse(employees.get(id));
    }

    public EmployeeResponse create(EmployeeProfile profile) {
        return toResponse(employees.create(profile));
    }

    public EmployeeResponse updateProfile(long id, EmployeeProfile profile) {
        return toResponse(employees.updateProfile(id, profile));
    }

    public EmployeeResponse setActive(long id, boolean active) {
        return toResponse(employees.setActive(id, active));
    }

    private EmployeeResponse toResponse(Employee employee) {
        BigDecimal gross = compensation.monthlyGross(List.of(employee.getId())).getOrDefault(employee.getId(), Money.zero());
        return EmployeeResponse.of(employee, gross);
    }
}
