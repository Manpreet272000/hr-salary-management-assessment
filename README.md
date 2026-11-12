# ACME Salary Management

A web app for ACME's HR Manager to manage salaries for 10,000 employees in five countries.
It replaces the HR team's spreadsheets for keeping employee and pay records, recording monthly
leave, estimating each month's net payout, and answering "how does the org pay people?".

- **Backend:** Java 21, Spring Boot 3.5, Spring Data JPA, Flyway, SQLite
- **Frontend:** Angular 22, Angular Material
- **Tests:** JUnit 5 + MockMvc against a real SQLite schema (backend), Vitest (frontend)

| Doc | What's in it |
|---|---|
| [docs/REQUIREMENTS.md](docs/REQUIREMENTS.md) | One-page requirements: goal, scope, what's left out and why |
| [docs/ASSUMPTIONS.md](docs/ASSUMPTIONS.md) | Every business assumption and where it lives in the code |
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | Design, key decisions and trade-offs, API, performance, testing |
| [docs/AI_USAGE.md](docs/AI_USAGE.md) | How AI tools were used, and how their output was checked |

## Features

- **Employee directory.** Server-side search (name, title or `EMP` code), filters (department, type, country, active/inactive), sorting and paging over 10k employees.
- **Profiles and status.** Create and edit employees. Mark them inactive when they leave, and reactivate them if they're rehired.
- **Monthly pay.** `REGULAR` salary, `INCENTIVE` (Sales only), `STIPEND` (interns only), always in the country's currency. Every revision is kept as history.
- **Attendance and pay estimate.** Record a month's applied sick and casual leave and unplanned leave. See gross pay → loss-of-pay deduction → 5% tax → net payout, along with the payroll cutoff date (the second-last working day).
- **Pay insights.** Headcount, plus min / median / average / max / total monthly pay by department, country, employment type or job title, split by currency.

## Run locally

Prerequisites: JDK 21, Maven 3.9, Node 22.22+.

```bash
# 1. Backend on :8080. Creates hr.db and seeds 10,000 employees on first start (~1.5 s).
cd backend
mvn spring-boot:run

# 2. Frontend on :4200, proxying /api to :8080
cd frontend
npm ci
npm start
```

Open http://localhost:4200.

| Setting | Default | Purpose |
|---|---|---|
| `DB_PATH` | `hr.db` | SQLite file location |
| `SEED_ENABLED` | `true` | Seed on startup when the employee table is empty |
| `app.payroll.tax-rate` | `0.05` | Flat tax rate |

To start from a clean database, stop the backend and delete `hr.db*`.

## Run as one container

The Docker image builds the Angular app and serves it from Spring Boot, so the UI and API share one origin:

```bash
docker build -t acme-hr .
docker run -p 8080:8080 -v acme-hr-data:/data acme-hr
# http://localhost:8080
```

## Tests

```bash
cd backend && mvn test           # 65 tests: unit (pay rules) + API tests on a temporary SQLite DB
cd frontend && npx ng test --watch=false   # 16 tests
```

Backend tests use a fixed clock (2026-11-11), so month and working-day maths is deterministic.

## Project layout

```
backend/src/main/java/com/acme/hr/
  employee/       directory, profile, active status
  compensation/   pay components, eligibility policy, history
  attendance/     monthly leave, working-day calendar, payroll cutoff
  payroll/        pure pay calculator, tax policy, monthly estimate
  insights/       aggregate SQL for pay and headcount
  meta/           enum reference data for the UI
  seed/           deterministic 10k-employee generator
  common/         errors, money, auditing, SPA serving
backend/src/main/resources/db/migration/   Flyway schema
frontend/src/app/
  core/           API client, models, formatting, error interceptor
  employees/      directory, detail, pay and estimate panels
  insights/       pay insights page
docs/             requirements, assumptions, architecture, AI usage
```
