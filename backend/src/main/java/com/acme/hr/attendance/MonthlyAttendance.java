package com.acme.hr.attendance;

import com.acme.hr.common.AuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.YearMonth;

/** Leave taken by one employee in one month (one row per employee per month). */
@Entity
@Table(name = "monthly_attendance")
class MonthlyAttendance extends AuditedEntity {

    @Column(name = "employee_id", nullable = false, updatable = false)
    private Long employeeId;

    @Column(nullable = false, updatable = false)
    private YearMonth month;

    @Column(name = "sick_leaves", nullable = false)
    private int sickLeaves;

    @Column(name = "casual_leaves", nullable = false)
    private int casualLeaves;

    @Column(name = "unplanned_leaves", nullable = false)
    private int unplannedLeaves;

    protected MonthlyAttendance() {
    }

    MonthlyAttendance(long employeeId, YearMonth month) {
        this.employeeId = employeeId;
        this.month = month;
    }

    void record(LeaveSummary leaves) {
        this.sickLeaves = leaves.sickLeaves();
        this.casualLeaves = leaves.casualLeaves();
        this.unplannedLeaves = leaves.unplannedLeaves();
    }

    LeaveSummary leaves() {
        return new LeaveSummary(sickLeaves, casualLeaves, unplannedLeaves);
    }
}
