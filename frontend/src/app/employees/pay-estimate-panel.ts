import { Component, effect, inject, input, signal } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MoneyPipe } from '../core/format';
import { HrApi } from '../core/hr-api';
import { Employee, LeaveSummary } from '../core/models';
import { toMonth } from '../core/months';

/** Records a month's leave and shows the resulting net payout estimate. */
@Component({
  selector: 'app-pay-estimate-panel',
  imports: [ReactiveFormsModule, MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule, MoneyPipe],
  templateUrl: './pay-estimate-panel.html',
  styleUrl: './detail-panels.scss',
})
export class PayEstimatePanel {
  private readonly api = inject(HrApi);

  readonly employee = input.required<Employee>();
  /** Bumped by the parent when pay changed, so the estimate is recalculated. */
  readonly refreshKey = input(0);

  protected readonly currentMonth = toMonth(new Date());
  protected readonly month = signal(this.currentMonth);
  private readonly savedLeaveVersion = signal(0);

  protected readonly leaveForm = inject(FormBuilder).nonNullable.group({
    sickLeaves: [0, [Validators.required, Validators.min(0), Validators.max(31)]],
    casualLeaves: [0, [Validators.required, Validators.min(0), Validators.max(31)]],
    unplannedLeaves: [0, [Validators.required, Validators.min(0), Validators.max(31)]],
  });

  /** Inactive employees are not paid, so nothing is requested for them (params undefined = idle). */
  protected readonly attendance = rxResource({
    params: () => (this.employee().active ? { id: this.employee().id, month: this.month() } : undefined),
    stream: ({ params }) => this.api.attendance(params.id, params.month),
  });
  protected readonly estimate = rxResource({
    params: () => this.employee().active
      ? { id: this.employee().id, month: this.month(), pay: this.refreshKey(), leave: this.savedLeaveVersion() }
      : undefined,
    stream: ({ params }) => this.api.payEstimate(params.id, params.month),
  });

  constructor() {
    effect(() => {
      const recorded = this.attendance.value();
      if (recorded) {
        this.leaveForm.reset(recorded.leaves);
      }
    });
  }

  protected selectMonth(value: string): void {
    if (value) {
      this.month.set(value);
    }
  }

  protected saveLeave(): void {
    if (this.leaveForm.invalid) {
      return;
    }
    const leaves: LeaveSummary = this.leaveForm.getRawValue();
    this.api.recordAttendance(this.employee().id, this.month(), leaves).subscribe((saved) => {
      this.attendance.set(saved);
      this.savedLeaveVersion.update((v) => v + 1);
    });
  }
}
