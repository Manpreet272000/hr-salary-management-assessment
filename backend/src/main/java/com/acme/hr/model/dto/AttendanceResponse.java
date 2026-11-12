package com.acme.hr.model.dto;

import java.time.LocalDate;
import java.time.YearMonth;

public record AttendanceResponse(YearMonth month, int workingDays, LocalDate payrollCutoff, LeaveSummary leaves) {
}
