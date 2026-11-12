package com.acme.hr.payroll;

import com.acme.hr.compensation.PayType;
import java.math.BigDecimal;

/** A monthly pay amount of a given type, as input to the calculator. */
public record PayLine(PayType payType, BigDecimal amount) {
}
