package com.acme.hr.payroll;

import com.acme.hr.attendance.LeaveSummary;
import com.acme.hr.common.Money;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.function.Predicate;
import org.springframework.stereotype.Component;

/** Pure monthly pay arithmetic: no I/O, so every rule is unit-tested directly. */
@Component
public class PayrollCalculator {

    private final TaxPolicy taxPolicy;

    public PayrollCalculator(TaxPolicy taxPolicy) {
        this.taxPolicy = taxPolicy;
    }

    public PayBreakdown calculate(Collection<PayLine> lines, int workingDays, LeaveSummary leaves) {
        if (workingDays <= 0) {
            throw new IllegalArgumentException("workingDays must be positive");
        }
        if (leaves.lossOfPayDays() > workingDays) {
            throw new IllegalArgumentException("loss-of-pay days cannot exceed working days");
        }
        BigDecimal fixed = sum(lines, line -> line.payType().isReducedByLossOfPay());
        BigDecimal variable = sum(lines, line -> !line.payType().isReducedByLossOfPay());
        BigDecimal gross = fixed.add(variable);

        // Multiply before dividing so the deduction is rounded once, not via a rounded per-day rate.
        BigDecimal deduction = fixed.multiply(BigDecimal.valueOf(leaves.lossOfPayDays()))
                .divide(BigDecimal.valueOf(workingDays), Money.SCALE, Money.ROUNDING);
        BigDecimal taxable = gross.subtract(deduction);
        BigDecimal tax = taxPolicy.taxOn(taxable);

        return new PayBreakdown(fixed, variable, gross, workingDays, leaves.lossOfPayDays(), deduction,
                taxable, taxPolicy.rate(), tax, Money.round(taxable.subtract(tax)));
    }

    private static BigDecimal sum(Collection<PayLine> lines, Predicate<PayLine> filter) {
        return Money.round(lines.stream().filter(filter).map(PayLine::amount).reduce(BigDecimal.ZERO, BigDecimal::add));
    }
}
