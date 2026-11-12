# Architecture and trade-offs

## Overview

```mermaid
flowchart LR
    UI["Angular 19 SPA<br/>(Angular Material)"] -- "JSON / REST /api" --> API
    subgraph API["Spring Boot 3.5 (Java 21)"]
        direction TB
        E[employee] --> C[compensation]
        E --> A[attendance]
        C --> P[payroll]
        A --> P
        I[insights] -.SQL.-> DB
        S[seed] -.JDBC batch.-> DB
    end
    API --> DB[("SQLite<br/>(Flyway migrations)")]
```

The backend is **organized by feature** (`employee`, `compensation`, `attendance`, `payroll`,
`insights`, `seed`), not by layer. Each feature holds its own entity, repository, service and
controller, and other features use only its service. A new feature (for example leave balances or
a payroll run) is a new package, not changes spread across shared layers.

| Package | Responsibility |
|---|---|
| `employee` | Directory: profile, active status, search. Publishes `EmployeeProfileChanged`. |
| `compensation` | Effective-dated pay components, eligibility (`CompensationPolicy`), history. |
| `attendance` | Monthly leave counts, working-day calendar, payroll cutoff. |
| `payroll` | Pure `PayrollCalculator` + `TaxPolicy`; `PayrollService` builds a month's estimate. |
| `insights` | Read-only aggregate SQL (headcount, min/median/avg/max/total pay). |
| `meta` | Enum reference data for the UI, so values are defined once, in Java. |
| `seed` | Deterministic 10k-employee generator and JDBC batch loader. |

## Key decisions

### Pay as effective-dated components, not a single `salary` column
`salary_component(employee_id, pay_type, amount, currency, effective_from, effective_to)`.
- **History is built in:** a revision closes the current row and opens a new one. Anyone can see what someone earned and when it changed.
- **Pay types are open for extension:** a new allowance is a new `PayType` constant and a rule in `CompensationPolicy`. No schema change.
- A **partial unique index** (`WHERE effective_to IS NULL`) lets the database itself guarantee at most one current amount per pay type.

### Rules live in small, pure classes
`PayrollCalculator`, `CompensationPolicy`, `WeekdayCalendar` and `SeedDataGenerator` do no I/O,
so the business rules are unit-tested in milliseconds. Services only coordinate.
Behaviour that differs by pay type is a property of the enum (`PayType.isReducedByLossOfPay`),
not an `if` repeated in several places.

### Extension points instead of speculative features
- `TaxPolicy`: flat 5% today; per-country slabs later.
- `WorkingDayCalendar`: Mon–Fri today; per-country holiday calendars later.
- `EmployeePayLookup`: lets the directory show monthly pay without `employee` depending on `compensation` (dependency inversion, no package cycle).
- `EmployeeProfileChanged` event: compensation reacts to department, type or country changes without the employee module knowing about pay.

### Analytics in SQL, split by currency
Insights are single SQL queries with CTEs and window functions (true median via `ROW_NUMBER`).
The app never loads 10k rows into memory to compute a statistic. The `groupBy` dimension is an
enum mapped to a whitelisted column, so a request can't inject SQL into the query.
Amounts are never added across currencies (see ASSUMPTIONS I2).

### SQLite
It needs no setup and runs as a single file, which suits the requirement and the data size.
It is configured with WAL (readers don't block on writes), `busy_timeout`, and foreign keys on.
Flyway owns the schema (`ddl-auto: none`). Moving to Postgres means changing the driver and
dialect. The SQL uses only standard CTEs and window functions.

### API
- REST, resource-oriented. Pay, attendance and estimates sit under `/api/employees/{id}/…`.
- Errors are RFC 9457 `ProblemDetail`: 400 for malformed input or validation (with a field → message map), 404 for an unknown employee, 422 for a business rule (e.g. "employee is inactive").
- Pages use a stable `PageResponse` shape. Page size is capped at 100.

| Method & path | Purpose |
|---|---|
| `GET /api/employees?query&department&country&employmentType&active&page&size&sort` | Search the directory |
| `POST /api/employees` · `GET/PUT /api/employees/{id}` | Create / read / update a profile |
| `PATCH /api/employees/{id}/status` | Activate / deactivate |
| `GET /api/employees/{id}/compensation` · `/history` | Current pay & allowed pay types · full history |
| `PUT/DELETE /api/employees/{id}/compensation/{payType}` | Revise / end a component |
| `GET/PUT /api/employees/{id}/attendance/{yyyy-MM}` | Read / record a month's leave |
| `GET /api/employees/{id}/pay-estimate?month=yyyy-MM` | Net pay estimate |
| `GET /api/insights/headcount?groupBy=` · `/api/insights/pay?groupBy=&country=` | Org insights |
| `GET /api/meta` | Enum reference data for the UI |

## Performance

| Concern | Approach | Measured (10k employees, laptop) |
|---|---|---|
| Seeding | Pure generator + JDBC `batchUpdate` in one transaction (not row-by-row JPA with IDENTITY ids, which can't batch) | ~1.5 s |
| Directory page | Server-side paging and filtering, indexed filter columns; monthly pay for the page fetched in **one** grouped query (no N+1) | ~20–50 ms |
| Insights | One aggregate SQL query per request | ~20–90 ms |
| Current-pay lookups | Partial unique index on `(employee_id, pay_type) WHERE effective_to IS NULL` | — |

## Testing strategy

- **Unit tests** cover the pure rules: pay calculation (LOP, incentive exemption, rounding, tax), working days and cutoff, pay eligibility, employee codes, seed invariants.
- **API tests** run full-stack against a throwaway SQLite file (real Flyway schema, no mocks) with a **fixed clock** (2026-11-11), so dates and month maths are deterministic. Tables are emptied before each test.
- They cover the active/inactive scenarios end to end: no payout, no pay changes and no attendance for inactive employees, and payment resumes after reactivation.
