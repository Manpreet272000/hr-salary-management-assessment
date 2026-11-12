package com.acme.hr.service;

import com.acme.hr.aspect.Audited;
import com.acme.hr.dao.MonthlyLeaveRepository;
import com.acme.hr.exception.BusinessRuleException;
import com.acme.hr.model.dto.AttendanceResponse;
import com.acme.hr.model.dto.LeaveSummary;
import com.acme.hr.model.entity.MonthlyLeave;
import com.acme.hr.model.enums.AuditSubject;
import com.acme.hr.model.enums.LeaveType;
import com.acme.hr.service.calendar.WorkingDayCalendar;
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

    /** The month's working days, payroll cutoff and recorded leave. */
    @Transactional(readOnly = true)
    public AttendanceResponse attendance(long employeeId, YearMonth month) {
        return new AttendanceResponse(month, calendar.workingDays(month), calendar.payrollCutoff(month),
                leavesFor(employeeId, month));
    }

    /** Leave recorded for the month, or no leave if nothing has been recorded yet. */
    @Transactional(readOnly = true)
    public LeaveSummary leavesFor(long employeeId, YearMonth month) {
        employees.get(employeeId);
        return new LeaveSummary(leaves.findByEmployeeIdAndMonth(employeeId, month).stream()
                .collect(Collectors.toMap(MonthlyLeave::getLeaveType, MonthlyLeave::getDays)));
    }

    /** Creates or replaces the month's leave record. */
    @Audited(operation = "Record leave", subjects = AuditSubject.LEAVE, scope = "#month")
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
