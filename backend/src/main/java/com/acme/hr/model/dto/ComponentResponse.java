package com.acme.hr.model.dto;

import com.acme.hr.model.entity.SalaryComponent;
import com.acme.hr.model.enums.PayType;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ComponentResponse(long id, PayType payType, BigDecimal amount, String currency,
        LocalDate effectiveFrom, LocalDate effectiveTo) {

    public static ComponentResponse of(SalaryComponent c) {
        return new ComponentResponse(c.getId(), c.getPayType(), c.getAmount(), c.getCurrency(),
                c.getEffectiveFrom(), c.getEffectiveTo());
    }
}
