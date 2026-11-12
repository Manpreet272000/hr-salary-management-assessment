import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  Attendance,
  AuditEntry,
  Compensation,
  Employee,
  EmployeeFilters,
  EmployeeRef,
  EmployeeProfile,
  HeadcountRow,
  LeaveSummary,
  Meta,
  Page,
  PayComponent,
  PayEstimate,
  PayStatsRow,
  Reporting,
} from './models';

export interface PageRequest {
  page: number;
  size: number;
  /** e.g. "fullName,asc" */
  sort?: string;
}

/** The single gateway to the backend REST API. */
@Injectable({ providedIn: 'root' })
export class HrApi {
  private readonly http = inject(HttpClient);

  meta(): Observable<Meta> {
    return this.http.get<Meta>('/api/meta');
  }

  searchEmployees(filters: EmployeeFilters, page: PageRequest): Observable<Page<Employee>> {
    return this.http.get<Page<Employee>>('/api/employees', { params: toParams({ ...filters, ...page }) });
  }

  employee(id: number): Observable<Employee> {
    return this.http.get<Employee>(employeeUrl(id));
  }

  createEmployee(profile: EmployeeProfile): Observable<Employee> {
    return this.http.post<Employee>('/api/employees', profile);
  }

  updateEmployee(id: number, profile: EmployeeProfile): Observable<Employee> {
    return this.http.put<Employee>(employeeUrl(id), profile);
  }

  setActive(id: number, active: boolean): Observable<Employee> {
    return this.http.patch<Employee>(`${employeeUrl(id)}/status`, { active });
  }

  reporting(id: number): Observable<Reporting> {
    return this.http.get<Reporting>(`${employeeUrl(id)}/reporting`);
  }

  /** Assigns a manager, or removes the current one when managerId is null. */
  setManager(id: number, managerId: number | null): Observable<Reporting> {
    return this.http.put<Reporting>(`${employeeUrl(id)}/manager`, { managerId });
  }

  /** Employees the reporting rules allow as this employee's manager. */
  managerCandidates(id: number, query: string): Observable<EmployeeRef[]> {
    return this.http.get<EmployeeRef[]>(`${employeeUrl(id)}/manager-candidates`, { params: toParams({ query }) });
  }

  compensation(id: number): Observable<Compensation> {
    return this.http.get<Compensation>(`${employeeUrl(id)}/compensation`);
  }

  revisePay(id: number, payType: string, amount: number): Observable<Compensation> {
    return this.http.put<Compensation>(`${employeeUrl(id)}/compensation/${payType}`, { amount });
  }

  endPay(id: number, payType: string): Observable<Compensation> {
    return this.http.delete<Compensation>(`${employeeUrl(id)}/compensation/${payType}`);
  }

  payHistory(id: number): Observable<PayComponent[]> {
    return this.http.get<PayComponent[]>(`${employeeUrl(id)}/compensation/history`);
  }

  attendance(id: number, month: string): Observable<Attendance> {
    return this.http.get<Attendance>(`${employeeUrl(id)}/attendance/${month}`);
  }

  recordAttendance(id: number, month: string, leaves: LeaveSummary): Observable<Attendance> {
    return this.http.put<Attendance>(`${employeeUrl(id)}/attendance/${month}`, leaves);
  }

  payEstimate(id: number, month: string): Observable<PayEstimate> {
    return this.http.get<PayEstimate>(`${employeeUrl(id)}/pay-estimate`, { params: { month } });
  }

  /** Audit trail, newest first. */
  audit(filters: { employeeId?: number; subject?: string }, page: PageRequest): Observable<Page<AuditEntry>> {
    return this.http.get<Page<AuditEntry>>('/api/audit', { params: toParams({ ...filters, ...page }) });
  }

  headcount(groupBy: string): Observable<HeadcountRow[]> {
    return this.http.get<HeadcountRow[]>('/api/insights/headcount', { params: { groupBy } });
  }

  payStats(groupBy: string, country?: string): Observable<PayStatsRow[]> {
    return this.http.get<PayStatsRow[]>('/api/insights/pay', { params: toParams({ groupBy, country }) });
  }
}

function employeeUrl(id: number): string {
  return `/api/employees/${id}`;
}

/** Drops unset values so the backend treats them as "any". */
export function toParams(values: Record<string, string | number | boolean | undefined | null>): HttpParams {
  let params = new HttpParams();
  for (const [key, value] of Object.entries(values)) {
    if (value !== undefined && value !== null && value !== '') {
      params = params.set(key, String(value));
    }
  }
  return params;
}
