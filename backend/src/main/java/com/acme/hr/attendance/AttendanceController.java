package com.acme.hr.attendance;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.YearMonth;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/employees/{employeeId}/attendance/{month}")
class AttendanceController {

    private final AttendanceService service;
    private final WorkingDayCalendar calendar;

    AttendanceController(AttendanceService service, WorkingDayCalendar calendar) {
        this.service = service;
        this.calendar = calendar;
    }

    @GetMapping
    AttendanceResponse get(@PathVariable long employeeId, @PathVariable YearMonth month) {
        return response(month, service.leavesFor(employeeId, month));
    }

    @PutMapping
    AttendanceResponse record(@PathVariable long employeeId, @PathVariable YearMonth month,
                              @Valid @RequestBody LeaveSummary leaves) {
        return response(month, service.record(employeeId, month, leaves));
    }

    private AttendanceResponse response(YearMonth month, LeaveSummary leaves) {
        return new AttendanceResponse(month, calendar.workingDays(month), calendar.payrollCutoff(month), leaves);
    }

    record AttendanceResponse(YearMonth month, int workingDays, LocalDate payrollCutoff, LeaveSummary leaves) {
    }
}
