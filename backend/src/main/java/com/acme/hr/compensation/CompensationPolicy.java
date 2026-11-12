package com.acme.hr.compensation;

import com.acme.hr.common.BusinessRuleException;
import com.acme.hr.employee.Department;
import com.acme.hr.employee.EmployeeProfile;
import com.acme.hr.employee.EmploymentType;
import java.util.EnumSet;
import java.util.Set;
import org.springframework.stereotype.Component;

/** Which pay types an employee may receive, based on employment type and department. */
@Component
public class CompensationPolicy {

    public Set<PayType> allowedPayTypes(EmployeeProfile profile) {
        if (profile.employmentType() == EmploymentType.INTERN) {
            return EnumSet.of(PayType.STIPEND);
        }
        Set<PayType> allowed = EnumSet.of(PayType.REGULAR);
        if (profile.department() == Department.SALES) {
            allowed.add(PayType.INCENTIVE);
        }
        return allowed;
    }

    public void requireAllowed(EmployeeProfile profile, PayType payType) {
        if (!allowedPayTypes(profile).contains(payType)) {
            throw new BusinessRuleException("%s pay is not allowed for a %s employee in %s"
                    .formatted(payType, profile.employmentType(), profile.department()));
        }
    }
}
