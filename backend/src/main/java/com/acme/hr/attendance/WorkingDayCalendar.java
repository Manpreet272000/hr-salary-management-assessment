package com.acme.hr.attendance;

import java.time.LocalDate;
import java.time.YearMonth;

/** The org's notion of working days; swap the implementation to add public holidays per country. */
public interface WorkingDayCalendar {

    int workingDays(YearMonth month);

    /** Attendance for the month is checked and payroll is run on the second-last working day. */
    LocalDate payrollCutoff(YearMonth month);
}
