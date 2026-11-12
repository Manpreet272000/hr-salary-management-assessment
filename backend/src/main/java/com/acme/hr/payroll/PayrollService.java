package com.acme.hr.payroll;

import com.acme.hr.attendance.AttendanceService;
import com.acme.hr.attendance.LeaveSummary;
import com.acme.hr.attendance.WorkingDayCalendar;
import com.acme.hr.compensation.CompensationService;
import com.acme.hr.employee.Employee;
import com.acme.hr.employee.EmployeeService;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Estimates an employee's payout for a month from their current pay and that month's recorded leave. */
@Service
@Transactional(readOnly = true)
public class PayrollService {

    private final EmployeeService employees;
    private final CompensationService compensation;
    private final AttendanceService attendance;
    private final WorkingDayCalendar calendar;
    private final PayrollCalculator calculator;

    PayrollService(EmployeeService employees, CompensationService compensation, AttendanceService attendance,
                   WorkingDayCalendar calendar, PayrollCalculator calculator) {
        this.employees = employees;
        this.compensation = compensation;
        this.attendance = attendance;
        this.calendar = calendar;
        this.calculator = calculator;
    }

    public PayEstimate estimate(long employeeId, YearMonth month) {
        Employee employee = employees.getActive(employeeId);
        List<PayLine> lines = compensation.current(employeeId).stream()
                .map(c -> new PayLine(c.getPayType(), c.getAmount()))
                .toList();
        LeaveSummary leaves = attendance.leavesFor(employeeId, month);
        PayBreakdown breakdown = calculator.calculate(lines, calendar.workingDays(month), leaves);
        return new PayEstimate(employeeId, employee.code(), month, employee.getCountry().currency(),
                calendar.payrollCutoff(month), leaves, breakdown);
    }

    public record PayEstimate(long employeeId, String employeeCode, YearMonth month, String currency,
                              LocalDate payrollCutoff, LeaveSummary leaves, PayBreakdown breakdown) {
    }
}
