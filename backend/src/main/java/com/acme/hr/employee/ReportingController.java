package com.acme.hr.employee;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/employees/{employeeId}")
class ReportingController {

    private final ReportingService service;

    ReportingController(ReportingService service) {
        this.service = service;
    }

    @GetMapping("/reporting")
    ReportingView reporting(@PathVariable long employeeId) {
        List<EmployeeRef> chain = refs(service.managementChain(employeeId));
        return new ReportingView(chain.isEmpty() ? null : chain.getLast(), chain, refs(service.directReports(employeeId)));
    }

    /** {@code {"managerId": 42}} to assign, {@code {"managerId": null}} to remove. */
    @PutMapping("/manager")
    ReportingView assignManager(@PathVariable long employeeId, @RequestBody ManagerRequest request) {
        service.assignManager(employeeId, request.managerId());
        return reporting(employeeId);
    }

    @GetMapping("/manager-candidates")
    List<EmployeeRef> candidates(@PathVariable long employeeId, @RequestParam(required = false) String query) {
        return refs(service.managerCandidates(employeeId, query));
    }

    private static List<EmployeeRef> refs(List<Employee> employees) {
        return employees.stream().map(EmployeeRef::of).toList();
    }

    record ManagerRequest(Long managerId) {
    }

    record ReportingView(EmployeeRef manager, List<EmployeeRef> chain, List<EmployeeRef> directReports) {
    }

    record EmployeeRef(long id, String code, String fullName, String jobTitle, Department department,
                       EmploymentType employmentType, boolean active) {

        static EmployeeRef of(Employee e) {
            return new EmployeeRef(e.getId(), e.code(), e.getFullName(), e.getJobTitle(), e.getDepartment(),
                    e.getEmploymentType(), e.isActive());
        }
    }
}
