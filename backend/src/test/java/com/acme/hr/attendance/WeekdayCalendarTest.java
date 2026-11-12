package com.acme.hr.attendance;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.YearMonth;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class WeekdayCalendarTest {

    private final WeekdayCalendar calendar = new WeekdayCalendar();

    @ParameterizedTest(name = "{0} has {1} working days, payroll cutoff {2}")
    @CsvSource({
        "2026-11, 21, 2026-11-27", // ends on Monday 30th → cutoff is the Friday before
        "2026-10, 22, 2026-10-29", // ends on Saturday 31st → last working day Fri 30th
        "2026-02, 20, 2026-02-26", // four full weeks
    })
    void countsWeekdaysAndFindsSecondLastWorkingDay(String month, int workingDays, LocalDate cutoff) {
        YearMonth yearMonth = YearMonth.parse(month);

        assertThat(calendar.workingDays(yearMonth)).isEqualTo(workingDays);
        assertThat(calendar.payrollCutoff(yearMonth)).isEqualTo(cutoff);
    }
}
