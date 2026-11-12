package com.acme.hr.model.entity;

import com.acme.hr.model.enums.PayType;
import com.acme.hr.util.Money;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * One monthly pay component of an employee, valid from {@code effectiveFrom} (inclusive) until
 * {@code effectiveTo} (exclusive). A null {@code effectiveTo} marks the current row; revisions close
 * the current row and open a new one so the full pay history is preserved.
 */
@Entity
@Table(name = "salary_component")
public class SalaryComponent extends AuditedEntity {

    @Column(name = "employee_id", nullable = false, updatable = false)
    private Long employeeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "pay_type", nullable = false, updatable = false)
    private PayType payType;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false, updatable = false)
    private String currency;

    @Column(name = "effective_from", nullable = false, updatable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    protected SalaryComponent() {
    }

    public SalaryComponent(long employeeId, PayType payType, BigDecimal amount, String currency, LocalDate effectiveFrom) {
        this.employeeId = employeeId;
        this.payType = payType;
        this.amount = Money.round(amount);
        this.currency = currency;
        this.effectiveFrom = effectiveFrom;
    }

    /** Same-day corrections overwrite the amount instead of creating a zero-length history row. */
    public void correctAmount(BigDecimal newAmount) {
        this.amount = Money.round(newAmount);
    }

    public void endOn(LocalDate date) {
        this.effectiveTo = date;
    }

    public boolean startedOn(LocalDate date) {
        return effectiveFrom.equals(date);
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public PayType getPayType() {
        return payType;
    }

    public BigDecimal getAmount() {
        return Money.round(amount);
    }

    public String getCurrency() {
        return currency;
    }

    public LocalDate getEffectiveFrom() {
        return effectiveFrom;
    }

    public LocalDate getEffectiveTo() {
        return effectiveTo;
    }
}
