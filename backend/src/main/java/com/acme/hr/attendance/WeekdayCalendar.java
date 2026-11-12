package com.acme.hr.attendance;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import org.springframework.stereotype.Component;

/** Monday–Friday are working days; no holiday calendar yet. */
@Component
class WeekdayCalendar implements WorkingDayCalendar {

    @Override
    public int workingDays(YearMonth month) {
        return workingDaysOf(month).size();
    }

    @Override
    public LocalDate payrollCutoff(YearMonth month) {
        List<LocalDate> days = workingDaysOf(month);
        return days.get(days.size() - 2);
    }

    private static List<LocalDate> workingDaysOf(YearMonth month) {
        return month.atDay(1).datesUntil(month.plusMonths(1).atDay(1))
                .filter(day -> day.getDayOfWeek() != DayOfWeek.SATURDAY && day.getDayOfWeek() != DayOfWeek.SUNDAY)
                .toList();
    }
}
