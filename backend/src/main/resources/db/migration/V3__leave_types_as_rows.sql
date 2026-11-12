-- One row per (employee, month, leave type) instead of a column per leave type, so a new leave
-- pool is an application enum value rather than a schema change.
CREATE TABLE monthly_leave (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    employee_id INTEGER   NOT NULL REFERENCES employee (id),
    month       TEXT      NOT NULL, -- ISO year-month, e.g. 2026-11
    leave_type  TEXT      NOT NULL,
    days        INTEGER   NOT NULL CHECK (days > 0),
    created_at  TIMESTAMP NOT NULL,
    updated_at  TIMESTAMP NOT NULL,
    UNIQUE (employee_id, month, leave_type)
);

INSERT INTO monthly_leave (employee_id, month, leave_type, days, created_at, updated_at)
SELECT employee_id, month, 'SICK', sick_leaves, created_at, updated_at FROM monthly_attendance WHERE sick_leaves > 0
UNION ALL
SELECT employee_id, month, 'CASUAL', casual_leaves, created_at, updated_at FROM monthly_attendance WHERE casual_leaves > 0
UNION ALL
SELECT employee_id, month, 'UNPLANNED', unplanned_leaves, created_at, updated_at FROM monthly_attendance WHERE unplanned_leaves > 0;

DROP TABLE monthly_attendance;
