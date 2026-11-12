package com.acme.hr.dao;

import com.acme.hr.model.entity.MonthlyLeave;
import java.time.YearMonth;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MonthlyLeaveRepository extends JpaRepository<MonthlyLeave, Long> {

    List<MonthlyLeave> findByEmployeeIdAndMonth(long employeeId, YearMonth month);
}
