package com.acme.hr.attendance;

import com.acme.hr.common.BusinessRuleException;
import com.acme.hr.employee.EmployeeService;
import java.time.Clock;
import java.time.YearMonth;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AttendanceService {

    private final MonthlyLeaveRepository leaves;
    private final EmployeeService employees;
    private final WorkingDayCalendar calendar;
    private final Clock clock;

    AttendanceService(MonthlyLeaveRepository leaves, EmployeeService employees,
                      WorkingDayCalendar calendar, Clock clock) {
        this.leaves = leaves;
        this.employees = employees;
        this.calendar = calendar;
        this.clock = clock;
    }

    /** Leave recorded for the month, or no leave if nothing has been recorded yet. */
    @Transactional(readOnly = true)
    public LeaveSummary leavesFor(long employeeId, YearMonth month) {
        employees.get(employeeId);
        return new LeaveSummary(leaves.findByEmployeeIdAndMonth(employeeId, month).stream()
                .collect(Collectors.toMap(MonthlyLeave::getLeaveType, MonthlyLeave::getDays)));
    }

    /** Creates or replaces the month's leave record. */
    public LeaveSummary record(long employeeId, YearMonth month, LeaveSummary summary) {
        employees.getActive(employeeId);
        if (month.isAfter(YearMonth.now(clock))) {
            throw new BusinessRuleException("Attendance cannot be recorded for a future month (%s)".formatted(month));
        }
        int workingDays = calendar.workingDays(month);
        if (summary.totalDays() > workingDays) {
            throw new BusinessRuleException("%d leave days exceed the %d working days in %s"
                    .formatted(summary.totalDays(), workingDays, month));
        }
        Map<LeaveType, MonthlyLeave> existing = leaves.findByEmployeeIdAndMonth(employeeId, month).stream()
                .collect(Collectors.toMap(MonthlyLeave::getLeaveType, Function.identity()));
        for (LeaveType type : LeaveType.values()) {
            int days = summary.daysOf(type);
            MonthlyLeave row = existing.get(type);
            if (days == 0) {
                if (row != null) {
                    leaves.delete(row);
                }
            } else {
                MonthlyLeave target = row != null ? row : new MonthlyLeave(employeeId, month, type);
                target.setDays(days);
                leaves.save(target);
            }
        }
        return summary;
    }
}
