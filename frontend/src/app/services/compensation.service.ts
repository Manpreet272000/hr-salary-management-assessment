import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Compensation, PayComponent } from '../models';

/** Monthly pay components and their history. Backed by CompensationController. */
@Injectable({ providedIn: 'root' })
export class CompensationService {
  private readonly http = inject(HttpClient);

  get(employeeId: number): Observable<Compensation> {
    return this.http.get<Compensation>(`/api/employees/${employeeId}/compensation`);
  }

  revise(employeeId: number, payType: string, amount: number): Observable<Compensation> {
    return this.http.put<Compensation>(`/api/employees/${employeeId}/compensation/${payType}`, { amount });
  }

  end(employeeId: number, payType: string): Observable<Compensation> {
    return this.http.delete<Compensation>(`/api/employees/${employeeId}/compensation/${payType}`);
  }

  history(employeeId: number): Observable<PayComponent[]> {
    return this.http.get<PayComponent[]>(`/api/employees/${employeeId}/compensation/history`);
  }
}
