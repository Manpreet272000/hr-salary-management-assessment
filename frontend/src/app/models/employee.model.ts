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
