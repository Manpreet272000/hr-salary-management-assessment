package com.acme.hr.employee;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Human-facing employee code ("EMP000042"), derived from the database id rather than stored. */
public final class EmployeeCode {

    private static final Pattern PATTERN = Pattern.compile("(?i)^EMP0*(\\d+)$");

    private EmployeeCode() {
    }

    public static String format(long id) {
        return "EMP%06d".formatted(id);
    }

    public static Optional<Long> parse(String code) {
        if (code == null) {
            return Optional.empty();
        }
        Matcher matcher = PATTERN.matcher(code.strip());
        return matcher.matches() ? Optional.of(Long.parseLong(matcher.group(1))) : Optional.empty();
    }
}
