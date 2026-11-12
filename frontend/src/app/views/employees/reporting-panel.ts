import { LowerCasePipe } from '@angular/common';
import { Component, inject, input, output, signal } from '@angular/core';
import { rxResource, toObservable, toSignal } from '@angular/core/rxjs-interop';
import { MatAutocompleteModule, MatAutocompleteSelectedEvent } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { RouterLink } from '@angular/router';
import { debounceTime } from 'rxjs';
import { HumanizePipe } from '../../shared/format';
import { ReportingService } from '../../services/reporting.service';
import { Employee, EmployeeRef } from '../../models';

/**
 * Who the employee reports to (and the chain above), who reports to them, and changing the manager.
 * The picker only lists managers the backend's reporting rules allow, so no rules are repeated here.
 */
@Component({
  selector: 'app-reporting-panel',
  imports: [LowerCasePipe, RouterLink, MatCardModule, MatFormFieldModule, MatInputModule, MatAutocompleteModule, MatButtonModule, MatIconModule, HumanizePipe],
  templateUrl: './reporting-panel.html',
  styleUrl: './detail-panels.scss',
})
export class ReportingPanel {
  private readonly reportingService = inject(ReportingService);

  readonly employee = input.required<Employee>();
  readonly refreshKey = input(0);
  /** Emitted after the manager changes, so the parent can refresh the employee. */
  readonly changed = output<void>();

  protected readonly search = signal('');
  private readonly debouncedSearch = toSignal(toObservable(this.search).pipe(debounceTime(250)), { initialValue: '' });

  protected readonly reporting = rxResource({
    params: () => ({ id: this.employee().id, key: this.refreshKey() }),
    stream: ({ params }) => this.reportingService.get(params.id),
  });
  protected readonly candidates = rxResource({
    params: () => (this.employee().active ? { id: this.employee().id, query: this.debouncedSearch() } : undefined),
    stream: ({ params }) => this.reportingService.managerCandidates(params.id, params.query),
  });

  protected displayNothing = (): string => '';

  protected onSearch(value: string): void {
    this.search.set(value);
  }

  protected choose(event: MatAutocompleteSelectedEvent): void {
    this.assign((event.option.value as EmployeeRef).id);
    this.search.set('');
  }

  protected remove(): void {
    this.assign(null);
  }

  private assign(managerId: number | null): void {
    this.reportingService.setManager(this.employee().id, managerId).subscribe((updated) => {
      this.reporting.set(updated);
      this.changed.emit();
    });
  }
}
