/** Leave days per leave type code, e.g. { SICK: 1, CASUAL: 0, UNPLANNED: 2 }. Types come from Meta.leaveTypes. */
export type LeaveSummary = Record<string, number>;

export interface LeaveTypeInfo {
  code: string;
  /** Unpaid leave days are loss-of-pay days. */
  paid: boolean;
}

export interface Attendance {
  month: string;
  workingDays: number;
  payrollCutoff: string;
  leaves: LeaveSummary;
}
