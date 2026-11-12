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
- **Reporting hierarchy.** Each employee has one manager, validated by rules: same department (a Sales rep reports to a Sales manager), active full-time managers, no loops. The manager picker only lists valid managers.
- **Monthly pay.** `REGULAR` salary, `INCENTIVE` (Sales only), `STIPEND` (interns only), always in the country's currency. Every revision is kept as history.
- **Attendance and pay estimate.** Record a month's leave per leave type (sick and casual are paid; unplanned is loss of pay; new pools are one enum value). See gross pay → loss-of-pay deduction → 5% tax → net payout, along with the payroll cutoff date (the second-last working day).
- **Audit trail.** Every change to profile, status, manager, pay and leave is recorded: before → after, when, who, and via which operation. It's applied with AOP (`@Audited`) and is append-only.
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
cd backend && mvn test           # 101 tests: unit (rules), API tests on a temporary SQLite DB, architecture rules
cd frontend && npx ng test --watch=false   # 17 tests
```

Backend tests use a fixed clock (2026-11-11), so month and working-day maths is deterministic.

## Project layout

MVC: Angular is the View, Spring controllers are the Controller, and `model` (backed by services and
DAOs) is the Model. The backend layering is enforced by an ArchUnit test.

```
backend/src/main/java/com/acme/hr/
  controller/     REST endpoints only (delegate to one service, return DTOs)
  service/        business logic + transactions
    policy/       compensation and reporting rules (strategy beans)
    payroll/      pure pay calculator, tax policy
    calendar/     working days, payroll cutoff
    audit/        audit snapshots of profile, status, manager, pay, leave
  dao/            all queries and storage: JPA repositories, specifications, aggregate SQL, bulk inserts
  model/
    entity/       JPA entities
    dto/          request/response records and value objects
    enums/        departments, countries, pay and leave types, …
    event/        domain events
  aspect/         @Audited + AuditAspect (cross-cutting audit)
  config/  exception/  util/  seed/
backend/src/main/resources/db/migration/   Flyway schema (V1–V4)
frontend/src/app/                          the View layer
  models/         TypeScript interfaces mirroring model/dto
  services/       one HTTP service per backend controller, error interceptor
  views/          employees/ (list, detail and its panels), insights/
  shared/         pipes and helpers
docs/             requirements, assumptions, architecture, AI usage
```
