package com.acme.hr.service;

import com.acme.hr.model.dto.LeaveSummary;
import com.acme.hr.model.dto.PayBreakdown;
import com.acme.hr.model.dto.PayEstimate;
import com.acme.hr.model.dto.PayLine;
import com.acme.hr.model.entity.Employee;
import com.acme.hr.service.calendar.WorkingDayCalendar;
import com.acme.hr.service.payroll.PayrollCalculator;
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
}
