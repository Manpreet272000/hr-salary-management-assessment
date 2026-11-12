import { DatePipe } from '@angular/common';
import { Component, inject, input, numberAttribute, signal } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { RouterLink } from '@angular/router';
import { HumanizePipe, MoneyPipe } from '../../shared/format';
import { EmployeeService } from '../../services/employee.service';
import { MetaService } from '../../services/meta.service';
import { Employee } from '../../models';
import { CompensationPanel } from './compensation-panel';
import { EmployeeFormDialog } from './employee-form-dialog';
import { ActivityPanel } from './activity-panel';
import { PayEstimatePanel } from './pay-estimate-panel';
import { ReportingPanel } from './reporting-panel';

/** One employee: profile and status, monthly pay, and attendance-based pay estimate. */
@Component({
  selector: 'app-employee-detail',
  imports: [DatePipe, RouterLink, MatButtonModule, MatIconModule, MatProgressBarModule, HumanizePipe, MoneyPipe, CompensationPanel, PayEstimatePanel, ReportingPanel, ActivityPanel],
  templateUrl: './employee-detail.html',
  styleUrl: './employee-detail.scss',
})
export class EmployeeDetail {
  private readonly employeeService = inject(EmployeeService);
  private readonly dialog = inject(MatDialog);
  protected readonly meta = inject(MetaService);

  /** Bound from the `:id` route parameter. */
  readonly id = input.required({ transform: numberAttribute });

  protected readonly employee = rxResource({
    params: () => this.id(),
    stream: ({ params }) => this.employeeService.get(params),
  });
  /** Incremented after any change to this employee, so dependent panels (pay, estimate, activity) reload. */
  protected readonly payVersion = signal(0);

  protected countryName(code: string): string {
    return this.meta.countries().find((c) => c.code === code)?.name ?? code;
  }

  protected edit(employee: Employee): void {
    this.dialog.open(EmployeeFormDialog, { data: employee }).afterClosed().subscribe((saved?: Employee) => {
      if (saved) {
        this.employee.set(saved);
        this.payVersion.update((v) => v + 1);
      }
    });
  }

  protected toggleActive(employee: Employee): void {
    this.employeeService.setActive(employee.id, !employee.active).subscribe((saved) => {
      this.employee.set(saved);
      this.payVersion.update((v) => v + 1);
    });
  }

  /** Something changed in a panel: refresh the header and every dependent panel. */
  protected onChanged(): void {
    this.employee.reload();
    this.payVersion.update((v) => v + 1);
  }
}
