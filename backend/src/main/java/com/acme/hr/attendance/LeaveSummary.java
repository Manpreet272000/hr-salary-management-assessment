package com.acme.hr.attendance;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * Whole leave days taken in a month, per {@link LeaveType}. Serialized as a plain object, e.g.
 * {@code {"SICK": 1, "CASUAL": 0, "UNPLANNED": 2}}; missing types count as zero.
 */
public record LeaveSummary(Map<LeaveType, @NotNull @PositiveOrZero @Max(31) Integer> days) {

    public static final LeaveSummary NONE = new LeaveSummary(Map.of());

    @JsonCreator
    public LeaveSummary {
        EnumMap<LeaveType, Integer> complete = new EnumMap<>(LeaveType.class);
        for (LeaveType type : LeaveType.values()) {
            Integer value = days == null ? null : days.get(type);
            complete.put(type, value == null ? 0 : value);
        }
        days = Collections.unmodifiableMap(complete);
    }

    @Override
    @JsonValue
    public Map<LeaveType, Integer> days() {
        return days;
    }

    public int daysOf(LeaveType type) {
        return days.get(type);
    }

    public int totalDays() {
        return days.values().stream().mapToInt(Integer::intValue).sum();
    }

    /** Days of unpaid leave types; each reduces fixed pay by one working day's worth. */
    public int lossOfPayDays() {
        return days.entrySet().stream().filter(e -> !e.getKey().isPaid()).mapToInt(Map.Entry::getValue).sum();
    }
}
