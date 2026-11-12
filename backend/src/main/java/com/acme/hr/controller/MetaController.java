package com.acme.hr.controller;

import com.acme.hr.model.dto.Meta;
import com.acme.hr.service.MetaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MetaController {

    private final MetaService service;

    MetaController(MetaService service) {
        this.service = service;
    }

    /** Departments, countries, pay and leave types, etc. for the UI's dropdowns and forms. */
    @GetMapping("/api/meta")
    Meta meta() {
        return service.meta();
    }
}
