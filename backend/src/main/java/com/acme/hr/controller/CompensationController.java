package com.acme.hr.controller;

import com.acme.hr.model.dto.CompensationResponse;
import com.acme.hr.model.dto.ComponentResponse;
import com.acme.hr.model.dto.RevisionRequest;
import com.acme.hr.model.enums.PayType;
import com.acme.hr.service.CompensationService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/employees/{employeeId}/compensation")
public class CompensationController {

    private final CompensationService service;

    CompensationController(CompensationService service) {
        this.service = service;
    }

    @GetMapping
    CompensationResponse current(@PathVariable long employeeId) {
        return service.compensation(employeeId);
    }

    @PutMapping("/{payType}")
    CompensationResponse revise(@PathVariable long employeeId, @PathVariable PayType payType,
                                @Valid @RequestBody RevisionRequest request) {
        service.revise(employeeId, payType, request.amount());
        return service.compensation(employeeId);
    }

    @DeleteMapping("/{payType}")
    CompensationResponse end(@PathVariable long employeeId, @PathVariable PayType payType) {
        service.end(employeeId, payType);
        return service.compensation(employeeId);
    }

    @GetMapping("/history")
    List<ComponentResponse> history(@PathVariable long employeeId) {
        return service.history(employeeId);
    }
}
