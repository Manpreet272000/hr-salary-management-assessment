package com.acme.hr.controller;

import com.acme.hr.model.dto.EmployeeProfile;
import com.acme.hr.model.dto.EmployeeResponse;
import com.acme.hr.model.dto.EmployeeSearchCriteria;
import com.acme.hr.model.dto.PageResponse;
import com.acme.hr.model.dto.StatusRequest;
import com.acme.hr.service.EmployeeDirectoryService;
import jakarta.validation.Valid;
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
public class EmployeeController {

    private final EmployeeDirectoryService directory;

    EmployeeController(EmployeeDirectoryService directory) {
        this.directory = directory;
    }

    /** Search by name/title/code with optional filters, e.g. {@code ?department=SALES&active=true&sort=fullName}. */
    @GetMapping
    PageResponse<EmployeeResponse> search(
            EmployeeSearchCriteria criteria,
            @PageableDefault(size = 25, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return directory.search(criteria, pageable);
    }

    @GetMapping("/{id}")
    EmployeeResponse get(@PathVariable long id) {
        return directory.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    EmployeeResponse create(@Valid @RequestBody EmployeeProfile profile) {
        return directory.create(profile);
    }

    @PutMapping("/{id}")
    EmployeeResponse update(@PathVariable long id, @Valid @RequestBody EmployeeProfile profile) {
        return directory.updateProfile(id, profile);
    }

    @PatchMapping("/{id}/status")
    EmployeeResponse setStatus(@PathVariable long id, @Valid @RequestBody StatusRequest request) {
        return directory.setActive(id, request.active());
    }
}
