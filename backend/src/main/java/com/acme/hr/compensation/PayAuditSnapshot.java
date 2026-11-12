package com.acme.hr.compensation;

import com.acme.hr.audit.AuditSnapshot;
import com.acme.hr.audit.AuditSubject;
import com.acme.hr.common.Money;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/** Current monthly pay per pay type, e.g. {"REGULAR": "INR 90,000.00"}. */
@Component
class PayAuditSnapshot implements AuditSnapshot {

    private final SalaryComponentRepository components;

    PayAuditSnapshot(SalaryComponentRepository components) {
        this.components = components;
    }

    @Override
    public AuditSubject subject() {
        return AuditSubject.PAY;
    }

    @Override
    public Map<String, String> capture(long employeeId, Object scope) {
        Map<String, String> pay = new LinkedHashMap<>();
        for (SalaryComponent c : components.findByEmployeeIdAndEffectiveToIsNullOrderByPayType(employeeId)) {
            pay.put(c.getPayType().name(), Money.format(c.getAmount(), c.getCurrency()));
        }
        return pay;
    }
}
