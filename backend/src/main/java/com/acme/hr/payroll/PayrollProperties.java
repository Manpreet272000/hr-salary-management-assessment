package com.acme.hr.payroll;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/** @param taxRate flat income-tax rate applied to taxable monthly pay, e.g. 0.05 for 5% */
@Validated
@ConfigurationProperties("app.payroll")
public record PayrollProperties(@NotNull @DecimalMin("0") @DecimalMax("1") BigDecimal taxRate) {
}
