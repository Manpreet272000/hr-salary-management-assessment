package com.acme.hr.controller;

import com.acme.hr.model.dto.AttendanceResponse;
import com.acme.hr.model.dto.LeaveSummary;
import com.acme.hr.service.AttendanceService;
import jakarta.validation.Valid;
import java.time.YearMonth;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/employees/{employeeId}/attendance/{month}")
public class AttendanceController {

    private final AttendanceService service;

    AttendanceController(AttendanceService service) {
        this.service = service;
    }

    @GetMapping
    AttendanceResponse get(@PathVariable long employeeId, @PathVariable YearMonth month) {
        return service.attendance(employeeId, month);
    }

    /** Body: leave days per type, e.g. {@code {"SICK": 1, "CASUAL": 0, "UNPLANNED": 2}}. */
    @PutMapping
    AttendanceResponse record(@PathVariable long employeeId, @PathVariable YearMonth month,
                              @Valid @RequestBody LeaveSummary leaves) {
        service.record(employeeId, month, leaves);
        return service.attendance(employeeId, month);
    }
}
