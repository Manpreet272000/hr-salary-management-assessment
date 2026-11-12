package com.acme.hr.model.enums;

/**
 * Countries ACME employs people in. Each country pays in exactly one currency, so currency is
 * derived from the employee's country instead of being chosen per salary entry.
 */
public enum Country {
    IN("India", "INR"),
    US("United States", "USD"),
    GB("United Kingdom", "GBP"),
    DE("Germany", "EUR"),
    SG("Singapore", "SGD");

    private final String displayName;
    private final String currency;

    Country(String displayName, String currency) {
        this.displayName = displayName;
        this.currency = currency;
    }

    public String displayName() {
        return displayName;
    }

    public String currency() {
        return currency;
    }
}
