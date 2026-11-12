# Assumptions

These are the assumptions the v1 build is based on. Each one is either configurable or sits behind
an extension point, so it can change without a rewrite.

## People

| # | Assumption | Where it lives |
|---|---|---|
| P1 | An employee has a name, job title, department, employment type (`FTE`, `CONTRACTUAL`, `INTERN`), country, and an `active` flag. | `Employee`, `EmployeeProfile` |
| P2 | `active = false` means the person has left. They keep their records and history, but get no payout, no pay changes and no attendance entries until reactivated. | `EmployeeService.getActive` |
| P3 | The employee code (`EMP000042`) is derived from the database id, not stored separately. | `EmployeeCode` |
| P4 | Departments, countries and employment types are fixed enums. They are stored as text, so adding one needs no DB migration. | `Department`, `Country`, `EmploymentType` |

## Pay

| # | Assumption | Where it lives |
|---|---|---|
| C1 | Pay is **monthly**. All amounts are monthly figures. | `salary_component.amount` |
| C2 | Pay types: `REGULAR` (salary), `INCENTIVE` (variable sales pay), `STIPEND` (interns). | `PayType` |
| C3 | Interns get only a `STIPEND`. FTE and contractual staff get `REGULAR`. Only Sales (non-interns) can also get an `INCENTIVE`. | `CompensationPolicy` |
| C4 | Each country pays in one currency (IN→INR, US→USD, GB→GBP, DE→EUR, SG→SGD). Currency is taken from the employee's country and never chosen by hand. | `Country` |
| C5 | A pay change takes effect **today**. The old amount is closed (`effective_to = today`) and kept as history. A second change on the same day corrects the amount instead of adding a zero-length history row. | `CompensationService.revise` |
| C6 | If a profile change makes a pay component invalid (leaving Sales, becoming an intern, moving country), that component is ended automatically and HR sets the new pay. Components that are still valid are kept. | `CompensationService.onProfileChanged` |
| C7 | A pay estimate uses the employee's **current** pay, even for a past month (no historical proration). | `PayrollService` |

## Attendance and payout

| # | Assumption | Where it lives |
|---|---|---|
| A1 | Attendance is recorded as monthly counts in whole days: applied **sick** leave, applied **casual** leave, and **unplanned** leave (absent without applying). | `LeaveSummary`, `monthly_attendance` |
| A2 | Applied sick and casual leave are paid. There are no leave balances or approval workflow in v1. | — |
| A3 | Only **unplanned** leave is loss of pay (LOP). | `LeaveSummary.lossOfPayDays` |
| A4 | Working days are Monday–Friday. No public holidays. | `WeekdayCalendar` |
| A5 | LOP deduction = fixed pay × LOP days ÷ working days in the month. It is rounded once, to 2 decimals, half-up. | `PayrollCalculator` |
| A6 | LOP reduces fixed pay (`REGULAR`, `STIPEND`) only. `INCENTIVE` is paid in full. | `PayType.isReducedByLossOfPay` |
| A7 | Tax is a **flat 5%** of (gross − LOP deduction), for everyone and every country. | `app.payroll.tax-rate`, `TaxPolicy` |
| A8 | Net pay = gross − LOP deduction − tax. | `PayrollCalculator` |
| A9 | Attendance is checked and payroll is run on the **second-last working day** of the month (the "payroll cutoff"). v1 shows this date but does not lock the month. | `WorkingDayCalendar.payrollCutoff` |
| A10 | Total leave in a month cannot exceed its working days. Attendance cannot be recorded for future months. | `AttendanceService.record` |

## Insights

| # | Assumption | Where it lives |
|---|---|---|
| I1 | Pay statistics count **active** employees and their **current** pay components. An employee's pay is the sum of their components (salary + incentive, or stipend). | `InsightsRepository` |
| I2 | Figures are always split by currency. Nothing is converted or added across currencies. | `InsightsRepository` |
| I3 | Median is the true median (the mean of the two middle values for an even count), computed in SQL. | `InsightsRepository.payStats` |

## Data and platform

| # | Assumption | Where it lives |
|---|---|---|
| D1 | SQLite is enough for 10k employees with one HR team writing. It runs in WAL mode so reads are not blocked during a write. | `application.yml` |
| D2 | Seed data is deterministic (fixed random seed 42) and loads only into an empty database. About 94% of employees are active, and the country, department and level mixes and pay bands are realistic but made up. | `SeedDataGenerator` |
| D3 | There is no authentication. A single HR Manager persona is assumed. | — |
