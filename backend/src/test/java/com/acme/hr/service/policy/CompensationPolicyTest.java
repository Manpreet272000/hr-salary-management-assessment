package com.acme.hr.service.policy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.acme.hr.exception.BusinessRuleException;
import com.acme.hr.model.dto.EmployeeProfile;
import com.acme.hr.model.enums.Country;
import com.acme.hr.model.enums.Department;
import com.acme.hr.model.enums.EmploymentType;
import com.acme.hr.model.enums.PayType;
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
