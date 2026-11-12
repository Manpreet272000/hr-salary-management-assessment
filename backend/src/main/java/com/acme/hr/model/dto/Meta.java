package com.acme.hr.model.dto;

import com.acme.hr.model.enums.Department;
import com.acme.hr.model.enums.Dimension;
import com.acme.hr.model.enums.EmploymentType;
import com.acme.hr.model.enums.PayType;
import java.math.BigDecimal;
import java.util.List;

public record Meta(List<Department> departments, List<EmploymentType> employmentTypes, List<CountryInfo> countries,
        List<PayType> payTypes, List<LeaveTypeInfo> leaveTypes, List<Dimension> dimensions,
        BigDecimal taxRate) {
}
