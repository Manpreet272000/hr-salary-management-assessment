package com.acme.hr.exception;

/** A request that is well-formed but violates an HR/payroll rule (mapped to HTTP 422). */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
