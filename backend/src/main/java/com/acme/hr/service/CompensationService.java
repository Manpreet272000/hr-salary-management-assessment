package com.acme.hr.service;

import com.acme.hr.aspect.Audited;
import com.acme.hr.dao.SalaryComponentRepository;
import com.acme.hr.exception.BusinessRuleException;
import com.acme.hr.model.dto.CompensationResponse;
import com.acme.hr.model.dto.ComponentResponse;
import com.acme.hr.model.dto.EmployeeProfile;
import com.acme.hr.model.entity.Employee;
import com.acme.hr.model.entity.SalaryComponent;
import com.acme.hr.model.enums.AuditSubject;
import com.acme.hr.model.enums.PayType;
import com.acme.hr.model.event.EmployeeProfileChanged;
import com.acme.hr.service.policy.CompensationPolicy;
import com.acme.hr.util.Money;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CompensationService {

    private final SalaryComponentRepository components;
    private final EmployeeService employees;
    private final CompensationPolicy policy;
    private final Clock clock;

    CompensationService(SalaryComponentRepository components, EmployeeService employees,
                        CompensationPolicy policy, Clock clock) {
        this.components = components;
        this.employees = employees;
        this.policy = policy;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<SalaryComponent> current(long employeeId) {
        employees.get(employeeId);
        return components.findByEmployeeIdAndEffectiveToIsNullOrderByPayType(employeeId);
    }

    /** Current pay, the pay types the employee may receive, and the monthly gross. */
    @Transactional(readOnly = true)
    public CompensationResponse compensation(long employeeId) {
        List<ComponentResponse> current = current(employeeId).stream().map(ComponentResponse::of).toList();
        BigDecimal gross = Money.round(current.stream().map(ComponentResponse::amount).reduce(BigDecimal.ZERO, BigDecimal::add));
        return new CompensationResponse(employeeId, allowedPayTypes(employeeId), current, gross);
    }

    /** Every pay amount the employee has had, newest first. */
    @Transactional(readOnly = true)
    public List<ComponentResponse> history(long employeeId) {
        employees.get(employeeId);
        return components.findByEmployeeIdOrderByEffectiveFromDescIdDesc(employeeId).stream()
                .map(ComponentResponse::of)
                .toList();
    }

    @Transactional(readOnly = true)
    public Set<PayType> allowedPayTypes(long employeeId) {
        return policy.allowedPayTypes(employees.get(employeeId).profile());
    }

    /** Sets the monthly amount of one pay type from today onwards, keeping the previous amount as history. */
    @Audited(operation = "Revise pay", subjects = AuditSubject.PAY)
    public SalaryComponent revise(long employeeId, PayType payType, BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new BusinessRuleException("Amount must be positive");
        }
        Employee employee = employees.getActive(employeeId);
        policy.requireAllowed(employee.profile(), payType);
        LocalDate today = LocalDate.now(clock);

        var existing = components.findByEmployeeIdAndPayTypeAndEffectiveToIsNull(employeeId, payType);
        if (existing.isPresent() && existing.get().startedOn(today)) {
            existing.get().correctAmount(amount);
            return existing.get();
        }
        existing.ifPresent(current -> {
            current.endOn(today);
            // Flush before inserting: only one open row per (employee, pay type) is allowed by a unique index.
            components.saveAndFlush(current);
        });
        return components.save(new SalaryComponent(
                employeeId, payType, amount, employee.getCountry().currency(), today));
    }

    /** Stops paying a component from today (e.g. an incentive plan ends). */
    @Audited(operation = "End pay", subjects = AuditSubject.PAY)
    public void end(long employeeId, PayType payType) {
        employees.getActive(employeeId);
        SalaryComponent current = components.findByEmployeeIdAndPayTypeAndEffectiveToIsNull(employeeId, payType)
                .orElseThrow(() -> new BusinessRuleException("No current %s pay to end".formatted(payType)));
        endOrDiscard(current, LocalDate.now(clock));
    }

    /**
     * A department, employment-type or country change can make current components invalid (e.g. an
     * incentive after leaving Sales, or a salary in the old country's currency). Those are ended so
     * HR can set the new structure; still-valid components are left untouched.
     */
    @EventListener
    void onProfileChanged(EmployeeProfileChanged event) {
        EmployeeProfile updated = event.employee().profile();
        if (!updated.payStructureDiffersFrom(event.previous())) {
            return;
        }
        Set<PayType> allowed = policy.allowedPayTypes(updated);
        String currency = updated.country().currency();
        LocalDate today = LocalDate.now(clock);
        components.findByEmployeeIdAndEffectiveToIsNullOrderByPayType(event.employee().getId()).stream()
                .filter(c -> !allowed.contains(c.getPayType()) || !c.getCurrency().equals(currency))
                .forEach(c -> endOrDiscard(c, today));
    }

    /** Current monthly gross per employee id, in one query; employees with no pay components are absent. */
    @Transactional(readOnly = true)
    public Map<Long, BigDecimal> monthlyGross(Collection<Long> employeeIds) {
        if (employeeIds.isEmpty()) {
            return Map.of();
        }
        return components.currentTotals(employeeIds).stream().collect(Collectors.toMap(
                SalaryComponentRepository.EmployeeTotal::getEmployeeId,
                total -> Money.round(total.getTotal())));
    }

    private void endOrDiscard(SalaryComponent component, LocalDate today) {
        if (component.startedOn(today)) {
            components.delete(component);
        } else {
            component.endOn(today);
        }
    }
}
