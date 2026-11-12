import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { EmployeeRef, Reporting } from '../models';
import { toParams } from '../shared/http-params';

/** Reporting lines and manager changes. Backed by ReportingController. */
@Injectable({ providedIn: 'root' })
export class ReportingService {
  private readonly http = inject(HttpClient);

  get(employeeId: number): Observable<Reporting> {
    return this.http.get<Reporting>(`/api/employees/${employeeId}/reporting`);
  }

  /** Assigns a manager, or removes the current one when managerId is null. */
  setManager(employeeId: number, managerId: number | null): Observable<Reporting> {
    return this.http.put<Reporting>(`/api/employees/${employeeId}/manager`, { managerId });
  }

  /** Employees the backend's reporting rules allow as this employee's manager. */
  managerCandidates(employeeId: number, query: string): Observable<EmployeeRef[]> {
    return this.http.get<EmployeeRef[]>(`/api/employees/${employeeId}/manager-candidates`, { params: toParams({ query }) });
  }
}
