import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { PayEstimate } from '../models';

/** Monthly net pay estimates. Backed by PayrollController. */
@Injectable({ providedIn: 'root' })
export class PayrollService {
  private readonly http = inject(HttpClient);

  estimate(employeeId: number, month: string): Observable<PayEstimate> {
    return this.http.get<PayEstimate>(`/api/employees/${employeeId}/pay-estimate`, { params: { month } });
  }
}
