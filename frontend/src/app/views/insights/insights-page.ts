import { LowerCasePipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { HumanizePipe, MoneyPipe } from '../../shared/format';
import { InsightsService } from '../../services/insights.service';
import { MetaService } from '../../services/meta.service';
import { groupByCurrency, percentOf } from './pay-stats';

/** "How does the org pay people?": headcount and pay distribution by a chosen dimension. */
@Component({
  selector: 'app-insights-page',
  imports: [LowerCasePipe, MatCardModule, MatFormFieldModule, MatSelectModule, MatProgressBarModule, HumanizePipe, MoneyPipe],
  templateUrl: './insights-page.html',
  styleUrl: './insights-page.scss',
})
export class InsightsPage {
  private readonly insightsService = inject(InsightsService);
  protected readonly meta = inject(MetaService);

  protected readonly groupBy = signal('DEPARTMENT');
  protected readonly country = signal('');

  protected readonly headcount = rxResource({
    params: () => this.groupBy(),
    stream: ({ params }) => this.insightsService.headcount(params),
  });
  protected readonly payStats = rxResource({
    params: () => ({ groupBy: this.groupBy(), country: this.country() || undefined }),
    stream: ({ params }) => this.insightsService.payStats(params.groupBy, params.country),
  });

  protected readonly totals = computed(() => {
    const rows = this.headcount.value() ?? [];
    const active = rows.reduce((sum, r) => sum + r.active, 0);
    const inactive = rows.reduce((sum, r) => sum + r.inactive, 0);
    return { active, inactive, total: active + inactive };
  });
  protected readonly currencySections = computed(() => groupByCurrency(this.payStats.value() ?? []));
  protected readonly pct = percentOf;
}
