import { DatePipe } from '@angular/common';
import { Component, inject, input, signal } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { MatCardModule } from '@angular/material/card';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { HumanizePipe } from '../core/format';
import { HrApi } from '../core/hr-api';

/** The employee's audit trail: every change to profile, status, manager, pay and leave, newest first. */
@Component({
  selector: 'app-activity-panel',
  imports: [DatePipe, MatCardModule, MatPaginatorModule, HumanizePipe],
  template: `
    <mat-card appearance="outlined">
      <mat-card-header>
        <mat-card-title>Activity</mat-card-title>
        <mat-card-subtitle>Every change to this employee: what, when and by whom</mat-card-subtitle>
      </mat-card-header>
      <mat-card-content>
        <ol class="timeline">
          @for (entry of activity.value()?.content ?? []; track entry.id) {
            <li>
              <span class="subject">{{ entry.subject | humanize }}</span>
              <div class="body">
                <div>{{ entry.summary }}</div>
                <div class="muted">
                  {{ entry.occurredAt | date: 'medium' }} · {{ entry.actor }} · via {{ entry.operation }}
                </div>
              </div>
            </li>
          } @empty {
            <li class="muted">No changes recorded yet.</li>
          }
        </ol>
        @if ((activity.value()?.totalElements ?? 0) > page().size) {
          <mat-paginator [length]="activity.value()?.totalElements ?? 0" [pageIndex]="page().page"
                         [pageSize]="page().size" [hidePageSize]="true" (page)="onPage($event)" />
        }
      </mat-card-content>
    </mat-card>
  `,
  styles: `
    mat-card { margin-bottom: 16px; }
    .timeline { list-style: none; margin: 8px 0 0; padding: 0; }
    .timeline li {
      display: grid;
      grid-template-columns: 96px minmax(0, 1fr);
      gap: 12px;
      padding: 10px 0;
      border-bottom: 1px solid var(--mat-sys-outline-variant);
    }
    .subject {
      align-self: start;
      justify-self: start;
      padding: 2px 10px;
      border-radius: 999px;
      font: var(--mat-sys-label-medium);
      background: var(--mat-sys-secondary-container);
      color: var(--mat-sys-on-secondary-container);
    }
  `,
})
export class ActivityPanel {
  private readonly api = inject(HrApi);

  readonly employeeId = input.required<number>();
  /** Bumped by the parent after any change, so the newest entries appear. */
  readonly refreshKey = input(0);

  protected readonly page = signal({ page: 0, size: 10 });
  protected readonly activity = rxResource({
    params: () => ({ employeeId: this.employeeId(), page: this.page(), key: this.refreshKey() }),
    stream: ({ params }) => this.api.audit({ employeeId: params.employeeId }, params.page),
  });

  protected onPage(event: PageEvent): void {
    this.page.set({ page: event.pageIndex, size: event.pageSize });
  }
}
