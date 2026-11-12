-- Each employee reports to at most one manager (NULL = top of their department's hierarchy).
-- Who may manage whom is enforced by ReportingRule beans in the application.
ALTER TABLE employee ADD COLUMN manager_id INTEGER REFERENCES employee (id);

CREATE INDEX idx_employee_manager ON employee (manager_id);
