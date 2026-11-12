import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { AuditEntry, Page } from '../models';
import { PageRequest, toParams } from '../shared/http-params';

/** The audit trail, newest first. Backed by AuditController. */
@Injectable({ providedIn: 'root' })
export class AuditService {
  private readonly http = inject(HttpClient);

  search(filters: { employeeId?: number; subject?: string }, page: PageRequest): Observable<Page<AuditEntry>> {
    return this.http.get<Page<AuditEntry>>('/api/audit', { params: toParams({ ...filters, ...page }) });
  }
}
