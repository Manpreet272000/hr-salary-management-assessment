-- Enum-like columns are TEXT validated by the application, so adding a department or pay type
-- needs no migration. Money is NUMERIC with two decimals; amounts are monthly.

CREATE TABLE employee (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    full_name       TEXT      NOT NULL,
    job_title       TEXT      NOT NULL,
    department      TEXT      NOT NULL,
    employment_type TEXT      NOT NULL,
    country         TEXT      NOT NULL,
    active          INTEGER   NOT NULL DEFAULT 1 CHECK (active IN (0, 1)),
    created_at      TIMESTAMP NOT NULL,
    updated_at      TIMESTAMP NOT NULL
);

CREATE INDEX idx_employee_department      ON employee (department);
CREATE INDEX idx_employee_country         ON employee (country);
CREATE INDEX idx_employee_employment_type ON employee (employment_type);
CREATE INDEX idx_employee_active          ON employee (active);

-- Effective-dated pay history: effective_to IS NULL marks the current amount of each pay type.
CREATE TABLE salary_component (
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    employee_id    INTEGER   NOT NULL REFERENCES employee (id),
    pay_type       TEXT      NOT NULL,
    amount         NUMERIC   NOT NULL CHECK (amount > 0),
    currency       TEXT      NOT NULL,
    effective_from DATE      NOT NULL,
    effective_to   DATE,
    created_at     TIMESTAMP NOT NULL,
    updated_at     TIMESTAMP NOT NULL
);

-- At most one current row per employee and pay type; also serves "current pay" lookups.
CREATE UNIQUE INDEX uq_salary_component_current
    ON salary_component (employee_id, pay_type) WHERE effective_to IS NULL;
CREATE INDEX idx_salary_component_history ON salary_component (employee_id, effective_from);

CREATE TABLE monthly_attendance (
    id               INTEGER PRIMARY KEY AUTOINCREMENT,
    employee_id      INTEGER   NOT NULL REFERENCES employee (id),
    month            TEXT      NOT NULL, -- ISO year-month, e.g. 2026-11
    sick_leaves      INTEGER   NOT NULL DEFAULT 0 CHECK (sick_leaves >= 0),
    casual_leaves    INTEGER   NOT NULL DEFAULT 0 CHECK (casual_leaves >= 0),
    unplanned_leaves INTEGER   NOT NULL DEFAULT 0 CHECK (unplanned_leaves >= 0),
    created_at       TIMESTAMP NOT NULL,
    updated_at       TIMESTAMP NOT NULL,
    UNIQUE (employee_id, month)
);
