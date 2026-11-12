# Requirements — ACME Salary Management

## Goal

ACME's HR team manages pay for ~10,000 employees in five countries using spreadsheets. This
product replaces those spreadsheets with a web app where an **HR Manager** can keep employee and
pay records accurate, estimate this month's payout, and answer "how do we pay people?" from
live data.

## Users and the jobs they need done

| As an HR Manager I want to… | So that… |
|---|---|
| Find any employee quickly (search by name/code, filter by department, country, type, status) | I don't scroll through 10k spreadsheet rows |
| Add an employee and edit their profile | records stay current |
| Mark someone inactive when they leave (and reactivate if rehired) | leavers are not paid, but their history is kept |
| Set or revise monthly pay: salary, sales incentive, or intern stipend | pay changes are made in one place, with history |
| Record a month's sick, casual and unplanned leave | the payout reflects attendance |
| See the estimated net payout for a month (after loss-of-pay and tax) | I can check pay before the payroll run |
| See pay statistics (min / median / average / max / total) by department, country, employment type or job title | I can answer pay questions without building a pivot table |

## In scope (v1)

1. **Employee directory**: create, edit, search, filter, sort and paginate through 10k employees; active/inactive status.
2. **Compensation**: effective-dated monthly pay components: `REGULAR` salary, `INCENTIVE` (Sales only), `STIPEND` (interns only). Every change keeps the previous amount as history.
3. **Attendance**: per employee per month: applied sick leave, applied casual leave, and unplanned leave (loss of pay).
4. **Pay estimate**: gross pay, minus loss-of-pay deduction, minus a flat 5% tax, gives net pay, plus the month's payroll cutoff date (the second-last working day).
5. **Pay insights**: headcount and pay statistics grouped by a chosen dimension and split by currency, optionally filtered by country.
6. **Seed data**: 10,000 deterministic, realistic employees across 5 countries, 9 departments and 3 employment types.

## Deliberately left out, and why

| Left out | Why |
|---|---|
| Running payroll (locking a month, generating payslips, bank files) | v1 is about keeping data right and *estimating* pay. A real run needs approvals and audit, which deserves its own design. The cutoff date is shown so that flow can be added later. |
| Real tax rules, deductions and benefits | Tax varies by country and slab. A flat 5% (configurable) keeps the payout flow end-to-end. `TaxPolicy` is the extension point. |
| Currency conversion / org-wide totals in one currency | FX rates change daily and would need a source of truth. Mixing currencies silently is worse than showing per-currency figures. |
| Leave balances, leave approval workflow, daily attendance | We record the month's leave counts that payroll needs. Leave management is a separate product. |
| Public holiday calendars | Working days are Mon–Fri. `WorkingDayCalendar` is the extension point for per-country holidays. |
| Authentication and roles | Single persona (HR Manager), assessment scope. In production this would sit behind company SSO, with field-level audit. |
| Back-dated / future-dated pay changes, bulk edits, CSV import/export | Useful, but each adds rules (overlaps, approvals, validation reports) beyond a v1. Pay changes take effect today. |
| Half-day leave | Whole days keep the arithmetic and the UI simple. |

## Success criteria

- An HR Manager can find any employee and change their pay in under a minute.
- Every pay change is traceable (old amount, new amount, effective dates).
- Inactive employees can never receive a payout, a pay change or attendance entries.
- Insight queries over 10k employees return in well under a second.
- Core rules (pay eligibility, loss of pay, tax, working days) are covered by fast, deterministic tests.
