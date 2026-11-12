package com.acme.hr.insights;

/** Employee attributes analytics can be grouped by, mapped to a whitelisted SQL column. */
public enum Dimension {
    DEPARTMENT("e.department"),
    EMPLOYMENT_TYPE("e.employment_type"),
    COUNTRY("e.country"),
    JOB_TITLE("e.job_title");

    private final String column;

    Dimension(String column) {
        this.column = column;
    }

    String column() {
        return column;
    }
}
