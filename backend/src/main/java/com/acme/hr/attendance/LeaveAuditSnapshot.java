package com.acme.hr.attendance;

import com.acme.hr.audit.AuditSnapshot;
import com.acme.hr.audit.AuditSubject;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/** Leave days per type for the month given as scope, e.g. {"2026-11 UNPLANNED": "2"}. */
@Component
class LeaveAuditSnapshot implements AuditSnapshot {

    private final MonthlyLeaveRepository leaves;

    LeaveAuditSnapshot(MonthlyLeaveRepository leaves) {
        this.leaves = leaves;
    }

    @Override
    public AuditSubject subject() {
        return AuditSubject.LEAVE;
    }

    @Override
    public Map<String, String> capture(long employeeId, Object scope) {
        YearMonth month = (YearMonth) scope;
        Map<String, Integer> recorded = new LinkedHashMap<>();
        leaves.findByEmployeeIdAndMonth(employeeId, month).forEach(l -> recorded.put(l.getLeaveType().name(), l.getDays()));
        Map<String, String> state = new LinkedHashMap<>();
        for (LeaveType type : LeaveType.values()) {
            state.put(month + " " + type.name(), String.valueOf(recorded.getOrDefault(type.name(), 0)));
        }
        return state;
    }
}
