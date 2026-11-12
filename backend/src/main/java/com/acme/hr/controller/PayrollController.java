package com.acme.hr.controller;

import com.acme.hr.model.dto.PayEstimate;
import com.acme.hr.service.PayrollService;
import java.time.Clock;
import java.time.YearMonth;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PayrollController {

    private final PayrollService service;
    private final Clock clock;

    PayrollController(PayrollService service, Clock clock) {
        this.service = service;
        this.clock = clock;
    }

    /** Defaults to the current month. */
    @GetMapping("/api/employees/{employeeId}/pay-estimate")
    PayEstimate estimate(@PathVariable long employeeId,
                                        @RequestParam(required = false) YearMonth month) {
        return service.estimate(employeeId, month == null ? YearMonth.now(clock) : month);
    }
}
