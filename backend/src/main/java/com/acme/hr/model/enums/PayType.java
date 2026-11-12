package com.acme.hr.model.enums;

import com.acme.hr.service.policy.CompensationPolicy;

/** Kinds of monthly pay. Adding a new kind means adding a constant here and a rule in {@link CompensationPolicy}. */
public enum PayType {
    /** Fixed monthly salary for FTE and contractual staff. */
    REGULAR(true),
    /** Variable sales pay; not reduced by loss-of-pay days. */
    INCENTIVE(false),
    /** Fixed monthly payment for interns. */
    STIPEND(true);

    private final boolean reducedByLossOfPay;

    PayType(boolean reducedByLossOfPay) {
        this.reducedByLossOfPay = reducedByLossOfPay;
    }

    /** Whether unplanned (loss-of-pay) leave days reduce this component pro rata. */
    public boolean isReducedByLossOfPay() {
        return reducedByLossOfPay;
    }
}
