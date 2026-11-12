package com.acme.hr.seed;

import static org.assertj.core.api.Assertions.assertThat;

import com.acme.hr.compensation.PayType;
import com.acme.hr.employee.Department;
import com.acme.hr.employee.EmploymentType;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class SeedDataGeneratorTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 11);

    private final List<SeedEmployee> employees = new SeedDataGenerator(42, TODAY).generate(2_000);

    @Test
    void sameSeedProducesSameData() {
        assertThat(new SeedDataGenerator(42, TODAY).generate(2_000)).isEqualTo(employees);
    }

    @Test
    void internsReceiveOnlyAStipend() {
        assertThat(employees)
                .filteredOn(e -> e.profile().employmentType() == EmploymentType.INTERN)
                .isNotEmpty()
                .allSatisfy(e -> assertThat(e.pay()).containsOnlyKeys(PayType.STIPEND));
    }

    @Test
    void onlySalesStaffReceiveIncentives() {
        assertThat(employees)
                .filteredOn(e -> e.pay().containsKey(PayType.INCENTIVE))
                .isNotEmpty()
                .allSatisfy(e -> assertThat(e.profile().department()).isEqualTo(Department.SALES));
        assertThat(employees)
                .filteredOn(e -> e.profile().department() == Department.SALES
                        && e.profile().employmentType() != EmploymentType.INTERN)
                .allSatisfy(e -> assertThat(e.pay()).containsKeys(PayType.REGULAR, PayType.INCENTIVE));
    }

    @Test
    void everyEmployeeHasPositivePayAndJoinedInThePast() {
        assertThat(employees).allSatisfy(e -> {
            assertThat(e.pay()).isNotEmpty();
            assertThat(e.pay().values()).allSatisfy(amount -> assertThat(amount).isPositive());
            assertThat(e.joinedOn()).isBefore(TODAY);
        });
    }

    @Test
    void mostButNotAllEmployeesAreActive() {
        long active = employees.stream().filter(SeedEmployee::active).count();

        assertThat(active).isBetween(1_800L, 1_960L);
    }
}
