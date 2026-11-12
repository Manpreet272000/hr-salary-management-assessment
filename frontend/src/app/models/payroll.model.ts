import { LeaveSummary } from './attendance.model';

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
