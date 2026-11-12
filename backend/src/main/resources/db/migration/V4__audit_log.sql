-- Append-only history of every change HR makes: what changed (before → after), when, by whom,
-- and through which operation. Written by AuditAspect, never by feature code.
CREATE TABLE audit_event (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    employee_id INTEGER   NOT NULL REFERENCES employee (id),
    subject     TEXT      NOT NULL, -- PROFILE, STATUS, MANAGER, PAY, LEAVE
    operation   TEXT      NOT NULL, -- the HR action that caused it, e.g. "Update profile"
    summary     TEXT      NOT NULL,
    details     TEXT      NOT NULL DEFAULT '{}', -- JSON: changed fields before/after
    actor       TEXT      NOT NULL,
    occurred_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_audit_event_employee ON audit_event (employee_id, occurred_at);
CREATE INDEX idx_audit_event_subject  ON audit_event (subject, occurred_at);

-- History must not be rewritten.
CREATE TRIGGER audit_event_is_append_only
BEFORE UPDATE ON audit_event
BEGIN
    SELECT RAISE(ABORT, 'audit_event is append-only');
END;
