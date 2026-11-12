package com.acme.hr.attendance;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Leave days taken in a month, in whole days.
 *
 * @param sickLeaves      applied sick leave (paid)
 * @param casualLeaves    applied casual leave (paid)
 * @param unplannedLeaves absences without an applied sick/casual leave; these are loss-of-pay days
 */
public record LeaveSummary(
        @PositiveOrZero @Max(31) int sickLeaves,
        @PositiveOrZero @Max(31) int casualLeaves,
        @PositiveOrZero @Max(31) int unplannedLeaves) {

    public static final LeaveSummary NONE = new LeaveSummary(0, 0, 0);

    public int totalDays() {
        return sickLeaves + casualLeaves + unplannedLeaves;
    }

    public int lossOfPayDays() {
        return unplannedLeaves;
    }
}
