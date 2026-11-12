package com.acme.hr.controller;

import com.acme.hr.model.dto.EmployeeRef;
import com.acme.hr.model.dto.ManagerRequest;
import com.acme.hr.model.dto.ReportingView;
import com.acme.hr.service.ReportingService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/employees/{employeeId}")
public class ReportingController {

    private final ReportingService service;

    ReportingController(ReportingService service) {
        this.service = service;
    }

    @GetMapping("/reporting")
    ReportingView reporting(@PathVariable long employeeId) {
        return service.reporting(employeeId);
    }

    /** {@code {"managerId": 42}} to assign, {@code {"managerId": null}} to remove. */
    @PutMapping("/manager")
    ReportingView assignManager(@PathVariable long employeeId, @RequestBody ManagerRequest request) {
        service.assignManager(employeeId, request.managerId());
        return service.reporting(employeeId);
    }

    @GetMapping("/manager-candidates")
    List<EmployeeRef> candidates(@PathVariable long employeeId, @RequestParam(required = false) String query) {
        return service.managerCandidates(employeeId, query);
    }
}
