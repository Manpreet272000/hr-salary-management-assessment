package com.acme.hr.service.policy;

import static org.assertj.core.api.Assertions.assertThat;

import com.acme.hr.model.dto.EmployeeProfile;
import com.acme.hr.model.entity.Employee;
import com.acme.hr.model.enums.Country;
import com.acme.hr.model.enums.Department;
import com.acme.hr.model.enums.EmploymentType;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/** The rules that need no database, checked directly. Cycle detection needs the database and is covered in ReportingApiTest. */
class ReportingRulesTest {

    private final Employee salesRep = employee(1, Department.SALES, EmploymentType.FTE, true);

    @Test
    void salesStaffMayReportToAnActiveFullTimeSalesManager() {
        Employee salesManager = employee(2, Department.SALES, EmploymentType.FTE, true);

        assertThat(new ReportingRules.SameDepartment().violation(salesRep, salesManager)).isEmpty();
        assertThat(new ReportingRules.ManagerIsFullTime().violation(salesRep, salesManager)).isEmpty();
        assertThat(new ReportingRules.ManagerIsActive().violation(salesRep, salesManager)).isEmpty();
        assertThat(new ReportingRules.NotSelf().violation(salesRep, salesManager)).isEmpty();
    }

    @Test
    void salesStaffMayNotReportToAnEngineeringManager() {
        Employee engineeringManager = employee(2, Department.ENGINEERING, EmploymentType.FTE, true);

        assertThat(new ReportingRules.SameDepartment().violation(salesRep, engineeringManager))
                .contains("A SALES employee must report to a manager in SALES, not ENGINEERING");
    }

    @Test
    void internsAndContractorsDoNotManagePeople() {
        assertThat(new ReportingRules.ManagerIsFullTime()
                .violation(salesRep, employee(2, Department.SALES, EmploymentType.INTERN, true))).isPresent();
        assertThat(new ReportingRules.ManagerIsFullTime()
                .violation(salesRep, employee(3, Department.SALES, EmploymentType.CONTRACTUAL, true))).isPresent();
    }

    @Test
    void inactiveEmployeesDoNotManagePeople() {
        assertThat(new ReportingRules.ManagerIsActive()
                .violation(salesRep, employee(2, Department.SALES, EmploymentType.FTE, false)))
                .contains("EMP000002 is inactive and cannot manage anyone");
    }

    @Test
    void nobodyReportsToThemselves() {
        assertThat(new ReportingRules.NotSelf().violation(salesRep, salesRep)).isPresent();
    }

    private static Employee employee(long id, Department department, EmploymentType type, boolean active) {
        Employee employee = new Employee(new EmployeeProfile("Person " + id, "Role", department, type, Country.IN));
        ReflectionTestUtils.setField(employee, "id", id);
        if (!active) {
            employee.deactivate();
        }
        return employee;
    }
}
