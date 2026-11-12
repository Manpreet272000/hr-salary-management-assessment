package com.acme.hr.payroll;

import java.math.BigDecimal;

/** Tax withheld from monthly pay. Replace with per-country slabs when real tax rules are in scope. */
public interface TaxPolicy {

    BigDecimal rate();

    BigDecimal taxOn(BigDecimal taxablePay);
}
