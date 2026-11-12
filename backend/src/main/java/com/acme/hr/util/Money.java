package com.acme.hr.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Monetary rounding rules, kept in one place. */
public final class Money {

    public static final int SCALE = 2;
    public static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    private Money() {
    }

    public static BigDecimal round(BigDecimal value) {
        return value.setScale(SCALE, ROUNDING);
    }

    /** "INR 80,000.00" — for human-readable text such as audit summaries. */
    public static String format(BigDecimal amount, String currency) {
        return "%s %,.2f".formatted(currency, round(amount));
    }

    public static BigDecimal zero() {
        return BigDecimal.ZERO.setScale(SCALE);
    }
}
