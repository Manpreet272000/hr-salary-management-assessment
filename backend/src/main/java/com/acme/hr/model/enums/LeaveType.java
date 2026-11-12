package com.acme.hr.model.enums;

/**
 * Kinds of leave. Adding a pool (e.g. EARNED, PARENTAL, COMP_OFF) is one constant here: storage,
 * validation, the pay calculation and the UI form all derive from this list.
 */
public enum LeaveType {
    /** Applied sick leave. */
    SICK(true),
    /** Applied casual leave. */
    CASUAL(true),
    /** Absent without an applied leave: loss of pay. */
    UNPLANNED(false);

    private final boolean paid;

    LeaveType(boolean paid) {
        this.paid = paid;
    }

    /** Paid leave doesn't reduce pay; unpaid leave days are loss-of-pay days. */
    public boolean isPaid() {
        return paid;
    }
}
