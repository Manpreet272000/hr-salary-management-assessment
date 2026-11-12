package com.acme.hr.service.payroll;

import com.acme.hr.config.PayrollProperties;
import com.acme.hr.util.Money;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class FlatRateTaxPolicy implements TaxPolicy {

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
