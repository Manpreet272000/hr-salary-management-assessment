package com.acme.hr.model.dto;

import java.util.List;

public record ReportingView(EmployeeRef manager, List<EmployeeRef> chain, List<EmployeeRef> directReports) {
}
