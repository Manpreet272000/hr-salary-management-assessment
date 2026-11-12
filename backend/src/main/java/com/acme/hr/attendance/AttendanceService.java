package com.acme.hr.attendance;

import com.acme.hr.common.BusinessRuleException;
import com.acme.hr.employee.EmployeeService;
import java.time.Clock;
import java.time.YearMonth;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AttendanceService {

    private final MonthlyAttendanceRepository attendance;
    private final EmployeeService employees;
    private final WorkingDayCalendar calendar;
    private final Clock clock;

    AttendanceService(MonthlyAttendanceRepository attendance, EmployeeService employees,
                      WorkingDayCalendar calendar, Clock clock) {
        this.attendance = attendance;
        this.employees = employees;
        this.calendar = calendar;
        this.clock = clock;
    }

    /** Leave recorded for the month, or no leave if nothing has been recorded yet. */
    @Transactional(readOnly = true)
    public LeaveSummary leavesFor(long employeeId, YearMonth month) {
        employees.get(employeeId);
        return attendance.findByEmployeeIdAndMonth(employeeId, month)
                .map(MonthlyAttendance::leaves)
                .orElse(LeaveSummary.NONE);
    }

    /** Creates or replaces the month's leave record. */
    public LeaveSummary record(long employeeId, YearMonth month, LeaveSummary leaves) {
        employees.getActive(employeeId);
        if (month.isAfter(YearMonth.now(clock))) {
            throw new BusinessRuleException("Attendance cannot be recorded for a future month (%s)".formatted(month));
        }
        int workingDays = calendar.workingDays(month);
        if (leaves.totalDays() > workingDays) {
            throw new BusinessRuleException("%d leave days exceed the %d working days in %s"
                    .formatted(leaves.totalDays(), workingDays, month));
        }
        MonthlyAttendance record = attendance.findByEmployeeIdAndMonth(employeeId, month)
                .orElseGet(() -> new MonthlyAttendance(employeeId, month));
        record.record(leaves);
        attendance.save(record);
        return leaves;
    }
}
