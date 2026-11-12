package com.acme.hr.model.dto;

import java.time.LocalDate;
import java.time.YearMonth;

public record PayEstimate(long employeeId, String employeeCode, YearMonth month, String currency,
        LocalDate payrollCutoff, LeaveSummary leaves, PayBreakdown breakdown) {
}
