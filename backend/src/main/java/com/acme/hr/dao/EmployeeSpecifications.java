package com.acme.hr.dao;

import com.acme.hr.model.dto.EmployeeSearchCriteria;
import com.acme.hr.model.entity.Employee;
import com.acme.hr.util.EmployeeCode;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Stream;
import org.springframework.data.jpa.domain.Specification;

public final class EmployeeSpecifications {

    private EmployeeSpecifications() {
    }

    public static Specification<Employee> matching(EmployeeSearchCriteria criteria) {
        return Specification.allOf(Stream.of(
                textQuery(criteria.query()),
                equalTo("department", criteria.department()),
                equalTo("employmentType", criteria.employmentType()),
                equalTo("country", criteria.country()),
                equalTo("active", criteria.active()))
                .filter(Objects::nonNull)
                .toList());
    }

    /** Matches an employee code exactly, otherwise a case-insensitive substring of name or job title. */
    public static Specification<Employee> textQuery(String query) {
        if (query == null || query.isBlank()) {
            return null;
        }
        return EmployeeCode.parse(query)
                .<Specification<Employee>>map(id -> (root, cq, cb) -> cb.equal(root.get("id"), id))
                .orElseGet(() -> {
                    String pattern = "%" + query.strip().toLowerCase(Locale.ROOT) + "%";
                    return (root, cq, cb) -> cb.or(
                            cb.like(cb.lower(root.get("fullName")), pattern),
                            cb.like(cb.lower(root.get("jobTitle")), pattern));
                });
    }

    private static Specification<Employee> equalTo(String attribute, Object value) {
        return value == null ? null : (root, cq, cb) -> cb.equal(root.get(attribute), value);
    }
}
