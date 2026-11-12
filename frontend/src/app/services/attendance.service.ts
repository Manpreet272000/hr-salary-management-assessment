import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Attendance, LeaveSummary } from '../models';

/** Monthly leave per leave type. Backed by AttendanceController. */
@Injectable({ providedIn: 'root' })
export class AttendanceService {
  private readonly http = inject(HttpClient);

  get(employeeId: number, month: string): Observable<Attendance> {
    return this.http.get<Attendance>(`/api/employees/${employeeId}/attendance/${month}`);
  }

  record(employeeId: number, month: string, leaves: LeaveSummary): Observable<Attendance> {
    return this.http.put<Attendance>(`/api/employees/${employeeId}/attendance/${month}`, leaves);
  }
}
