package com.acme.hr.compensation;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

interface SalaryComponentRepository extends JpaRepository<SalaryComponent, Long> {

    List<SalaryComponent> findByEmployeeIdAndEffectiveToIsNullOrderByPayType(long employeeId);

    Optional<SalaryComponent> findByEmployeeIdAndPayTypeAndEffectiveToIsNull(long employeeId, PayType payType);

    List<SalaryComponent> findByEmployeeIdOrderByEffectiveFromDescIdDesc(long employeeId);

    @Query("""
            select s.employeeId as employeeId, sum(s.amount) as total
            from SalaryComponent s
            where s.effectiveTo is null and s.employeeId in :employeeIds
            group by s.employeeId
            """)
    List<EmployeeTotal> currentTotals(Collection<Long> employeeIds);

    interface EmployeeTotal {
        Long getEmployeeId();

        BigDecimal getTotal();
    }
}
