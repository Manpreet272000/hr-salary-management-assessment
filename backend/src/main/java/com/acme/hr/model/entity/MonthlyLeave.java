package com.acme.hr.model.entity;

import com.acme.hr.model.enums.LeaveType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.YearMonth;

/** Days of one leave type taken by one employee in one month. Zero-day rows are not stored. */
@Entity
@Table(name = "monthly_leave")
public class MonthlyLeave extends AuditedEntity {

    @Column(name = "employee_id", nullable = false, updatable = false)
    private Long employeeId;

    @Column(nullable = false, updatable = false)
    private YearMonth month;

    @Enumerated(EnumType.STRING)
    @Column(name = "leave_type", nullable = false, updatable = false)
    private LeaveType leaveType;

    @Column(nullable = false)
    private int days;

    protected MonthlyLeave() {
    }

    public MonthlyLeave(long employeeId, YearMonth month, LeaveType leaveType) {
        this.employeeId = employeeId;
        this.month = month;
        this.leaveType = leaveType;
    }

    public void setDays(int days) {
        this.days = days;
    }

    public LeaveType getLeaveType() {
        return leaveType;
    }

    public int getDays() {
        return days;
    }
}
