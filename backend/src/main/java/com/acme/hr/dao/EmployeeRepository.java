package com.acme.hr.dao;

import com.acme.hr.model.entity.Employee;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {

    List<Employee> findByManagerIdOrderByFullName(long managerId);

    long countByManagerId(long managerId);
}
