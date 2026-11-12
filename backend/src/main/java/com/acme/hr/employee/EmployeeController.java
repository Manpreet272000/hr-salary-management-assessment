package com.acme.hr.employee;

import com.acme.hr.common.Money;
import com.acme.hr.common.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/employees")
class EmployeeController {

    private final EmployeeService service;
    private final EmployeePayLookup payLookup;

    EmployeeController(EmployeeService service, EmployeePayLookup payLookup) {
        this.service = service;
        this.payLookup = payLookup;
    }

    @GetMapping
    PageResponse<EmployeeResponse> search(
            EmployeeSearchCriteria criteria,
            @PageableDefault(size = 25, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<Employee> page = service.search(criteria, pageable);
        Map<Long, BigDecimal> gross = payLookup.monthlyGross(page.map(Employee::getId).getContent());
        return PageResponse.of(page, e -> EmployeeResponse.of(e, gross.getOrDefault(e.getId(), Money.zero())));
    }

    @GetMapping("/{id}")
    EmployeeResponse get(@PathVariable long id) {
        return toResponse(service.get(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    EmployeeResponse create(@Valid @RequestBody EmployeeProfile profile) {
        return toResponse(service.create(profile));
    }

    @PutMapping("/{id}")
    EmployeeResponse update(@PathVariable long id, @Valid @RequestBody EmployeeProfile profile) {
        return toResponse(service.updateProfile(id, profile));
    }

    @PatchMapping("/{id}/status")
    EmployeeResponse setStatus(@PathVariable long id, @Valid @RequestBody StatusRequest request) {
        return toResponse(service.setActive(id, request.active()));
    }

    private EmployeeResponse toResponse(Employee employee) {
        BigDecimal gross = payLookup.monthlyGross(List.of(employee.getId()))
                .getOrDefault(employee.getId(), Money.zero());
        return EmployeeResponse.of(employee, gross);
    }

    record StatusRequest(@NotNull Boolean active) {
    }
}
