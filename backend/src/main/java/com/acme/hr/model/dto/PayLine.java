package com.acme.hr.model.dto;

import com.acme.hr.model.enums.PayType;
import java.math.BigDecimal;

/** A monthly pay amount of a given type, as input to the calculator. */
public record PayLine(PayType payType, BigDecimal amount) {
}
