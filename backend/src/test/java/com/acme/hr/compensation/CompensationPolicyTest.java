package com.acme.hr.compensation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.acme.hr.common.BusinessRuleException;
import com.acme.hr.common.Country;
import com.acme.hr.employee.Department;
import com.acme.hr.employee.EmployeeProfile;
import com.acme.hr.employee.EmploymentType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CompensationPolicyTest {

    private final CompensationPolicy policy = new CompensationPolicy();

    @ParameterizedTest(name = "{0} in {1} may receive {2}")
    @CsvSource({
        "FTE,         ENGINEERING, REGULAR",
        "CONTRACTUAL, ENGINEERING, REGULAR",
        "FTE,         SALES,       REGULAR|INCENTIVE",
        "CONTRACTUAL, SALES,       REGULAR|INCENTIVE",
        "INTERN,      ENGINEERING, STIPEND",
        "INTERN,      SALES,       STIPEND",
    })
    void allowedPayTypesDependOnEmploymentTypeAndDepartment(EmploymentType type, Department department, String expected) {
        assertThat(policy.allowedPayTypes(profile(type, department)))
                .extracting(Enum::name)
                .containsExactlyInAnyOrder(expected.split("\\|"));
    }

    @ParameterizedTest(name = "{0} in {1} may not receive {2}")
    @CsvSource({
        "FTE,    ENGINEERING, INCENTIVE",
        "FTE,    SALES,       STIPEND",
        "INTERN, SALES,       REGULAR",
        "INTERN, SALES,       INCENTIVE",
    })
    void rejectsDisallowedPayTypes(EmploymentType type, Department department, PayType payType) {
        assertThatThrownBy(() -> policy.requireAllowed(profile(type, department), payType))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining(payType.name());
    }

    private static EmployeeProfile profile(EmploymentType type, Department department) {
        return new EmployeeProfile("A Person", "Role", department, type, Country.IN);
    }
}
