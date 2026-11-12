/**
 * Shapes of the backend's JSON. Enum-like values (department, country, …) are plain strings here:
 * their allowed values come from GET /api/meta, so they are defined once, in the backend.
 */

export interface Page<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface EmployeeProfile {
  fullName: string;
  jobTitle: string;
  department: string;
  employmentType: string;
  country: string;
}

export interface Employee extends EmployeeProfile {
  id: number;
  code: string;
  currency: string;
  active: boolean;
  managerId: number | null;
  monthlyGross: number;
  createdAt: string;
  updatedAt: string;
}

export interface EmployeeFilters {
  query?: string;
  department?: string;
  employmentType?: string;
  country?: string;
  active?: boolean;
}

export interface PayComponent {
  id: number;
  payType: string;
  amount: number;
  currency: string;
  effectiveFrom: string;
  effectiveTo: string | null;
}

export interface Compensation {
  employeeId: number;
  allowedPayTypes: string[];
  components: PayComponent[];
  monthlyGross: number;
}

/** Leave days per leave type code, e.g. { SICK: 1, CASUAL: 0, UNPLANNED: 2 }. Types come from Meta.leaveTypes. */
export type LeaveSummary = Record<string, number>;

export interface LeaveTypeInfo {
  code: string;
  /** Unpaid leave days are loss-of-pay days. */
  paid: boolean;
}

/** A compact reference to an employee, used in reporting lines. */
export interface EmployeeRef {
  id: number;
  code: string;
  fullName: string;
  jobTitle: string;
  department: string;
  employmentType: string;
  active: boolean;
}

export interface Reporting {
  manager: EmployeeRef | null;
  /** From the top of the hierarchy down to the direct manager. */
  chain: EmployeeRef[];
  directReports: EmployeeRef[];
}

export interface Attendance {
  month: string;
  workingDays: number;
  payrollCutoff: string;
  leaves: LeaveSummary;
}

export interface PayBreakdown {
  fixedPay: number;
  variablePay: number;
  grossPay: number;
  workingDays: number;
  lossOfPayDays: number;
  lossOfPayDeduction: number;
  taxablePay: number;
  taxRate: number;
  tax: number;
  netPay: number;
}

export interface PayEstimate {
  employeeId: number;
  employeeCode: string;
  month: string;
  currency: string;
  payrollCutoff: string;
  leaves: LeaveSummary;
  breakdown: PayBreakdown;
}

export interface HeadcountRow {
  group: string;
  active: number;
  inactive: number;
}

export interface PayStatsRow {
  group: string;
  currency: string;
  headcount: number;
  min: number;
  median: number;
  average: number;
  max: number;
  totalMonthlyPayroll: number;
}

/** One audited change: what changed (before → after), when, by whom, and through which operation. */
export interface AuditEntry {
  id: number;
  employeeId: number;
  subject: string;
  operation: string;
  summary: string;
  details: { before: Record<string, string | null>; after: Record<string, string | null> };
  actor: string;
  occurredAt: string;
}

export interface CountryInfo {
  code: string;
  name: string;
  currency: string;
}

export interface Meta {
  departments: string[];
  employmentTypes: string[];
  countries: CountryInfo[];
  payTypes: string[];
  leaveTypes: LeaveTypeInfo[];
  dimensions: string[];
  taxRate: number;
}
