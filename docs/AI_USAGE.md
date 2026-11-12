# How AI was used

I built this with **Claude Code** (Anthropic's agentic coding CLI) as a pair programmer. I made the
product and domain decisions. The agent turned them into a design, code, tests and docs, and every
step was checked by running the code.

## Division of work

| I decided (human) | The agent did, and I reviewed |
|---|---|
| Stack: Java/Spring Boot, Angular, SQLite | Scaffolding, configuration, dependency versions |
| Data model: employees (id, name, active flag, department, employment type, timestamps); salary rows linked by `employee_id` with amount and currency | Effective-dated `salary_component` design with a partial unique index; Flyway migration |
| Pay types `REGULAR` / `INCENTIVE` / `STIPEND`; month-on-month pay; incentives for sales | `PayType` / `CompensationPolicy` encoding those rules, with tests |
| Leave rules: applied sick and casual leave are paid, unplanned leave is loss of pay; checked on the second-last working day | `LeaveSummary`, `WeekdayCalendar`, `PayrollCalculator`, with worked-example tests |
| Flat 5% tax for now | Configurable `TaxPolicy` |
| TDD, seed 10k employees, scalability, no duplicated logic | Pure rule classes, an event so services react to profile changes without coupling, a JDBC batch seeder |
| Keep an assumptions artifact; review all code before committing | `docs/ASSUMPTIONS.md` and the other docs; incremental commits after review |

## The main prompt (verbatim)

> in terms of HR being able to manage and update salaries of 10k employees, there will be an employee
> table having employee id, name, created_at, active flag boolean which means currently in company or not,
> updated_at, department, employment_type (contractual or FTE or Intern) one more table that should be of
> salary, that corresponds F.K employee_id, salary_figure, currency let's add more components on top of it
> once the problem statement is more clear, now it also want the salaries data via web-based software and
> be able to answer questions about how the org pays people. For now I want that it's a month on month pay
> system and say there are sales people as well so there is an incentive for the employees in sales so pay
> type can be regular, incentive, stipend an enum maintained and also in the salary structure, we would also
> want to maintain employee's attendance in that month and basis their leave count we would be deducting the
> payout for the month unless it's applied leaves already, if it's unplanned leave and no proper sick or
> casual leave applied only then would the salary deduction be applicable, Follow test driven development
> architecture, use SQLite for maintaining relational DB, seed entry of 10k employees, test cases for all the
> scenarios mentioned when employee is in the company or not and calculations basis the leaves, maintain
> attendance structure as well, this will be checked at 2nd last working day of every month that is before
> the payout is done, but for now we are dealing with maintaining and structuring the employees view and
> making changes in salary or marking employees active or inactive so let's cater to that and estimated pay
> for the month if we receive input of sick, casual leaves and LOP leaves and basis that monthly payout after
> taxes, for taxes assume a flat 5% cut for now, maintain an artifact listing out all of these assumptions
> […], java as backend and angular for FE as requested should be catered here, make better efficient choices
> wherever possible avoid repetition of logic and make the code scalable for further additions wherever possible

Follow-up prompts during review:

> what are the design patterns kept in mind here that offer extensibility if we want to add another leave
> pool, or want to introduce a reporting hierarchy of employees within each other that should also be done
> here with validations that a sales person should only report to a sales manager not an engineering manager

> Also i need the auditing maintained against employees leaves taken, salary changes, when happened, done by
> who (i know for now we are having HR itself) still have an extension room for the same

> auditing must follow AOP

> Can we have the mvc architecture be followed where the dao is used for queries and storing jpa repository,
> controller layer for controllers, model having all necessary models, i need these things structured and
> if possible angular be in the view

The assessment brief was given as extra context (multi-country org, one-page requirements doc,
commit artifacts, incremental commits).

## How AI output was checked

- **Tests first for rules.** Pay calculation, working days, payroll cutoff and eligibility have unit tests with hand-computed expected values (e.g. 42,000 × 3 ÷ 21 = 6,000 LOP, then (42,000 − 6,000) × 0.95 = 34,200 net).
- **Real database in API tests.** No mocked repositories. Tests run Flyway migrations on a temporary SQLite file with a fixed clock, and cover the active and inactive employee scenarios end to end.
- **Running the real thing.** I booted the app, seeded 10k rows (1.5 s), timed the API with `curl` (20–90 ms queries), and took screenshots of each screen against the real data.

## Design corrections from review

- **Leave pools weren't extensible.** Asked about adding a leave pool, the agent admitted that the first design (three fixed leave columns) needed changes in five places. It refactored leave to `LeaveType` with a `paid` flag, stored as rows (migration V3), so a pool is now one enum constant.
- **Audit moved from events to AOP.** The first audit draft had services publish audit events. On review I asked for AOP, so it became `@Audited` + `AuditAspect` with per-area snapshots, and services have no audit code at all.

- **Package-by-feature → layered MVC.** The first structure grouped code by feature. On review I asked for MVC layers (`controller` / `service` / `dao` / `model`, with Angular as the View). The agent moved the classes with a script and pulled DTOs out of controllers into `model.dto`. It also added an ArchUnit test that enforces the layering, and that test then caught two real violations:
  - `EmployeeController` handled entities directly. Fixed with `EmployeeDirectoryService`.
  - An `aspect` ↔ `service` package cycle. Fixed by moving the audit interfaces into `aspect`.

## Mistakes caught along the way

- `List.of(...)` was used with nullable search filters and threw a `NullPointerException`. An API test caught it, and the fix filters out nulls first.
- Spring Boot's built-in problem-details handler overrode the custom validation response, so the field errors went missing. A test asserting `$.errors` caught it. The fix extends `ResponseEntityExceptionHandler` so there is one handler.
- A first draft of the payroll-cutoff calculation used a hard-to-read stream `reduce`. It was rewritten to "take the second-last working day" before the tests went green.
