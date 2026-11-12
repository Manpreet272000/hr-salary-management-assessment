import { Component, effect, inject, input, output, signal } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { FormControl, FormRecord, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { HumanizePipe, MoneyPipe } from '../core/format';
import { HrApi } from '../core/hr-api';
import { MetaStore } from '../core/meta-store';
import { Employee, LeaveSummary } from '../core/models';
import { toMonth } from '../core/months';

/**
 * Records a month's leave and shows the resulting net payout estimate. The leave fields come from
 * the backend's leave types, so a new leave pool appears here without a frontend change.
 */
@Component({
  selector: 'app-pay-estimate-panel',
  imports: [ReactiveFormsModule, MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule, HumanizePipe, MoneyPipe],
  templateUrl: './pay-estimate-panel.html',
  styleUrl: './detail-panels.scss',
})
export class PayEstimatePanel {
  private readonly api = inject(HrApi);
  protected readonly meta = inject(MetaStore);

  readonly employee = input.required<Employee>();
  /** Bumped by the parent when pay changed, so the estimate is recalculated. */
  readonly refreshKey = input(0);
  /** Emitted after leave is saved. */
  readonly changed = output<void>();

  protected readonly currentMonth = toMonth(new Date());
  protected readonly month = signal(this.currentMonth);
  private readonly savedLeaveVersion = signal(0);

  /** One whole-day control per leave type, keyed by leave type code. */
  protected readonly leaveForm = new FormRecord<FormControl<number>>({});

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
      for (const type of this.meta.leaveTypes()) {
        if (!this.leaveForm.contains(type.code)) {
          this.leaveForm.addControl(type.code, new FormControl(0, {
            nonNullable: true,
            validators: [Validators.required, Validators.min(0), Validators.max(31)],
          }));
        }
      }
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
      this.changed.emit();
    });
  }
}
