package com.acme.hr.model.dto;

import com.acme.hr.model.enums.PayType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public record CompensationResponse(long employeeId, Set<PayType> allowedPayTypes,
        List<ComponentResponse> components, BigDecimal monthlyGross) {
}
