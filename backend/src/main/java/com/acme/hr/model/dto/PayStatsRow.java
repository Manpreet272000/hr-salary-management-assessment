package com.acme.hr.model.dto;

import java.math.BigDecimal;

public record PayStatsRow(String group, String currency, long headcount, BigDecimal min, BigDecimal median,
        BigDecimal average, BigDecimal max, BigDecimal totalMonthlyPayroll) {
}
