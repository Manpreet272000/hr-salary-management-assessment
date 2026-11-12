package com.acme.hr.attendance;

import java.time.YearMonth;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface MonthlyAttendanceRepository extends JpaRepository<MonthlyAttendance, Long> {

    Optional<MonthlyAttendance> findByEmployeeIdAndMonth(long employeeId, YearMonth month);
}
