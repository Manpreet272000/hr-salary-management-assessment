package com.acme.hr.model.dto;

import java.math.BigDecimal;

/**
 * How a month's net pay is reached.
 *
 * @param fixedPay        components reduced by loss-of-pay days (regular salary, stipend)
 * @param variablePay     components paid in full regardless of attendance (incentive)
 * @param grossPay        fixed + variable
 * @param lossOfPayDays   unplanned leave days
 * @param lossOfPayDeduction fixedPay × lossOfPayDays / workingDays
 * @param taxablePay      gross − deduction
 * @param taxRate         flat rate applied
 * @param tax             taxable × rate
 * @param netPay          taxable − tax
 */
public record PayBreakdown(
        BigDecimal fixedPay,
        BigDecimal variablePay,
        BigDecimal grossPay,
        int workingDays,
        int lossOfPayDays,
        BigDecimal lossOfPayDeduction,
        BigDecimal taxablePay,
        BigDecimal taxRate,
        BigDecimal tax,
        BigDecimal netPay) {
}
