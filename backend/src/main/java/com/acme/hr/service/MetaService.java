package com.acme.hr.service;

import com.acme.hr.model.dto.CountryInfo;
import com.acme.hr.model.dto.LeaveTypeInfo;
import com.acme.hr.model.dto.Meta;
import com.acme.hr.model.enums.Country;
import com.acme.hr.model.enums.Department;
import com.acme.hr.model.enums.Dimension;
import com.acme.hr.model.enums.EmploymentType;
import com.acme.hr.model.enums.LeaveType;
import com.acme.hr.model.enums.PayType;
import com.acme.hr.service.payroll.TaxPolicy;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Service;

/** Reference data for the UI, so enum values are defined once (in Java) instead of duplicated in TypeScript. */
@Service
public class MetaService {

    private final TaxPolicy taxPolicy;

    MetaService(TaxPolicy taxPolicy) {
        this.taxPolicy = taxPolicy;
    }

    public Meta meta() {
        return new Meta(
                List.of(Department.values()),
                List.of(EmploymentType.values()),
                Arrays.stream(Country.values()).map(c -> new CountryInfo(c, c.displayName(), c.currency())).toList(),
                List.of(PayType.values()),
                Arrays.stream(LeaveType.values()).map(t -> new LeaveTypeInfo(t, t.isPaid())).toList(),
                List.of(Dimension.values()),
                taxPolicy.rate());
    }
}
