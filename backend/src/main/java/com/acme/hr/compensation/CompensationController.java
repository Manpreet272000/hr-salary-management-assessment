package com.acme.hr.compensation;

import com.acme.hr.common.Money;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/employees/{employeeId}/compensation")
class CompensationController {

    private final CompensationService service;

    CompensationController(CompensationService service) {
        this.service = service;
    }

    @GetMapping
    CompensationResponse current(@PathVariable long employeeId) {
        return snapshot(employeeId);
    }

    @PutMapping("/{payType}")
    CompensationResponse revise(@PathVariable long employeeId, @PathVariable PayType payType,
                                @Valid @RequestBody RevisionRequest request) {
        service.revise(employeeId, payType, request.amount());
        return snapshot(employeeId);
    }

    @DeleteMapping("/{payType}")
    CompensationResponse end(@PathVariable long employeeId, @PathVariable PayType payType) {
        service.end(employeeId, payType);
        return snapshot(employeeId);
    }

    @GetMapping("/history")
    List<ComponentResponse> history(@PathVariable long employeeId) {
        return service.history(employeeId).stream().map(ComponentResponse::of).toList();
    }

    private CompensationResponse snapshot(long employeeId) {
        List<ComponentResponse> current = service.current(employeeId).stream().map(ComponentResponse::of).toList();
        BigDecimal gross = Money.round(current.stream().map(ComponentResponse::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        return new CompensationResponse(employeeId, service.allowedPayTypes(employeeId), current, gross);
    }

    record RevisionRequest(@NotNull @Positive @Digits(integer = 13, fraction = 2) BigDecimal amount) {
    }

    record CompensationResponse(long employeeId, Set<PayType> allowedPayTypes,
                                List<ComponentResponse> components, BigDecimal monthlyGross) {
    }

    record ComponentResponse(long id, PayType payType, BigDecimal amount, String currency,
                             LocalDate effectiveFrom, LocalDate effectiveTo) {

        static ComponentResponse of(SalaryComponent c) {
            return new ComponentResponse(c.getId(), c.getPayType(), c.getAmount(), c.getCurrency(),
                    c.getEffectiveFrom(), c.getEffectiveTo());
        }
    }
}
