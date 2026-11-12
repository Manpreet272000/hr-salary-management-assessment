import { Component, computed, inject, input, output, signal } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatTableModule } from '@angular/material/table';
import { Observable } from 'rxjs';
import { HumanizePipe, MoneyPipe } from '../core/format';
import { HrApi } from '../core/hr-api';
import { Compensation, Employee } from '../core/models';

const PAY_TYPE_HINTS: Record<string, string> = {
  REGULAR: 'Fixed monthly salary. Reduced pro rata for unplanned leave.',
  INCENTIVE: 'Monthly sales incentive. Paid in full regardless of leave.',
  STIPEND: 'Fixed monthly intern stipend. Reduced pro rata for unplanned leave.',
};

/** Current monthly pay per allowed pay type, with revise / end actions and the full change history. */
@Component({
  selector: 'app-compensation-panel',
  imports: [FormsModule, MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatExpansionModule, MatTableModule, HumanizePipe, MoneyPipe],
  templateUrl: './compensation-panel.html',
  styleUrl: './detail-panels.scss',
})
export class CompensationPanel {
  private readonly api = inject(HrApi);

  readonly employee = input.required<Employee>();
  /** Bumped by the parent when something outside this panel may have changed pay (e.g. a profile edit). */
  readonly refreshKey = input(0);
  readonly changed = output<void>();

  protected readonly hints = PAY_TYPE_HINTS;
  protected readonly historyColumns = ['payType', 'amount', 'effectiveFrom', 'effectiveTo'];
  /** Amount typed per pay type, before it is saved. */
  protected readonly drafts = signal<Record<string, number | null>>({});
  protected readonly busy = signal(false);

  protected readonly compensation = rxResource({
    params: () => ({ id: this.employee().id, key: this.refreshKey() }),
    stream: ({ params }) => this.api.compensation(params.id),
  });
  protected readonly history = rxResource({
    params: () => ({ id: this.employee().id, key: this.refreshKey() }),
    stream: ({ params }) => this.api.payHistory(params.id),
  });

  /** One row per pay type the employee may receive, with its current amount if set. */
  protected readonly rows = computed(() => {
    const comp = this.compensation.value();
    if (!comp) {
      return [];
    }
    return comp.allowedPayTypes.map((payType) => ({
      payType,
      current: comp.components.find((c) => c.payType === payType),
    }));
  });

  protected setDraft(payType: string, value: number | null): void {
    this.drafts.update((d) => ({ ...d, [payType]: value }));
  }

  protected canSave(payType: string): boolean {
    const amount = this.drafts()[payType];
    return this.employee().active && !this.busy() && amount != null && amount > 0;
  }

  protected revise(payType: string): void {
    const amount = this.drafts()[payType];
    if (amount != null && amount > 0) {
      this.run(this.api.revisePay(this.employee().id, payType, amount), payType);
    }
  }

  protected end(payType: string): void {
    this.run(this.api.endPay(this.employee().id, payType), payType);
  }

  private run(request: Observable<Compensation>, payType: string): void {
    this.busy.set(true);
    request.subscribe({
      next: (updated) => {
        this.compensation.set(updated);
        this.history.reload();
        this.setDraft(payType, null);
        this.busy.set(false);
        this.changed.emit();
      },
      error: () => this.busy.set(false),
    });
  }
}
