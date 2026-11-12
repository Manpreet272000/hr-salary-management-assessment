import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { HeadcountRow, PayStatsRow } from '../models';
import { toParams } from '../shared/http-params';

/** Org-wide headcount and pay statistics. Backed by InsightsController. */
@Injectable({ providedIn: 'root' })
export class InsightsService {
  private readonly http = inject(HttpClient);

  headcount(groupBy: string): Observable<HeadcountRow[]> {
    return this.http.get<HeadcountRow[]>('/api/insights/headcount', { params: { groupBy } });
  }

  payStats(groupBy: string, country?: string): Observable<PayStatsRow[]> {
    return this.http.get<PayStatsRow[]>('/api/insights/pay', { params: toParams({ groupBy, country }) });
  }
}
