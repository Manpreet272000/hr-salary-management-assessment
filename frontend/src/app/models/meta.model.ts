import { LeaveTypeInfo } from './attendance.model';

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
