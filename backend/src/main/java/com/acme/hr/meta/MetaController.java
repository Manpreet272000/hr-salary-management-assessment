package com.acme.hr.meta;

import com.acme.hr.common.Country;
import com.acme.hr.compensation.PayType;
import com.acme.hr.employee.Department;
import com.acme.hr.employee.EmploymentType;
import com.acme.hr.insights.Dimension;
import com.acme.hr.payroll.TaxPolicy;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Reference data for the UI, so enum values are defined once (here) instead of duplicated in TypeScript. */
@RestController
class MetaController {

    private final TaxPolicy taxPolicy;

    MetaController(TaxPolicy taxPolicy) {
        this.taxPolicy = taxPolicy;
    }

    @GetMapping("/api/meta")
    Meta meta() {
        return new Meta(
                List.of(Department.values()),
                List.of(EmploymentType.values()),
                Arrays.stream(Country.values()).map(c -> new CountryInfo(c, c.displayName(), c.currency())).toList(),
                List.of(PayType.values()),
                List.of(Dimension.values()),
                taxPolicy.rate());
    }

    record Meta(List<Department> departments, List<EmploymentType> employmentTypes, List<CountryInfo> countries,
                List<PayType> payTypes, List<Dimension> dimensions, BigDecimal taxRate) {
    }

    record CountryInfo(Country code, String name, String currency) {
    }
}
