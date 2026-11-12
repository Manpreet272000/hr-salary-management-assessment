package com.acme.hr.payroll;

import com.acme.hr.common.Money;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
class FlatRateTaxPolicy implements TaxPolicy {

    private final BigDecimal rate;

    FlatRateTaxPolicy(PayrollProperties properties) {
        this.rate = properties.taxRate();
    }

    @Override
    public BigDecimal rate() {
        return rate;
    }

    @Override
    public BigDecimal taxOn(BigDecimal taxablePay) {
        return Money.round(taxablePay.multiply(rate));
    }
}
