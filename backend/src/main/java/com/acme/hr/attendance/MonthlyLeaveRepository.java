package com.acme.hr.attendance;

import java.time.YearMonth;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

interface MonthlyLeaveRepository extends JpaRepository<MonthlyLeave, Long> {

    List<MonthlyLeave> findByEmployeeIdAndMonth(long employeeId, YearMonth month);
}
