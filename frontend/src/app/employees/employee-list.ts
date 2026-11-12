import { Component, inject, signal } from '@angular/core';
import { rxResource, takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { Router } from '@angular/router';
import { debounceTime, map } from 'rxjs';
import { HumanizePipe, MoneyPipe } from '../core/format';
import { HrApi } from '../core/hr-api';
import { MetaStore } from '../core/meta-store';
import { Employee, EmployeeFilters } from '../core/models';
import { EmployeeFormDialog } from './employee-form-dialog';

type StatusFilter = 'active' | 'inactive' | 'all';

/** The employee directory: server-side search, filters, sorting and paging over all employees. */
@Component({
  selector: 'app-employee-list',
  imports: [
    ReactiveFormsModule, MatTableModule, MatPaginatorModule, MatSortModule, MatFormFieldModule, MatInputModule,
    MatSelectModule, MatButtonModule, MatButtonToggleModule, MatIconModule, MatProgressBarModule,
    HumanizePipe, MoneyPipe,
  ],
  templateUrl: './employee-list.html',
  styleUrl: './employee-list.scss',
})
export class EmployeeList {
  private readonly api = inject(HrApi);
  private readonly router = inject(Router);
  private readonly dialog = inject(MatDialog);
  protected readonly meta = inject(MetaStore);

  protected readonly columns = ['code', 'name', 'department', 'employmentType', 'country', 'monthlyGross', 'status'];

  protected readonly filterForm = inject(FormBuilder).nonNullable.group({
    query: '',
    department: '',
    employmentType: '',
    country: '',
    status: 'active' as StatusFilter,
  });

  private readonly filters = signal<EmployeeFilters>(toFilters(this.filterForm.getRawValue()));
  protected readonly page = signal({ page: 0, size: 25 });
  protected readonly sort = signal<string | undefined>(undefined);

  protected readonly employees = rxResource({
    params: () => ({ filters: this.filters(), page: { ...this.page(), sort: this.sort() } }),
    stream: ({ params }) => this.api.searchEmployees(params.filters, params.page),
  });

  constructor() {
    this.filterForm.valueChanges
      .pipe(debounceTime(300), map(() => toFilters(this.filterForm.getRawValue())), takeUntilDestroyed())
      .subscribe((filters) => {
        this.page.update((p) => ({ ...p, page: 0 }));
        this.filters.set(filters);
      });
  }

  protected onPage(event: PageEvent): void {
    this.page.set({ page: event.pageIndex, size: event.pageSize });
  }

  protected onSort(sort: Sort): void {
    this.sort.set(sort.direction ? `${sort.active},${sort.direction}` : undefined);
    this.page.update((p) => ({ ...p, page: 0 }));
  }

  protected open(employee: Employee): void {
    this.router.navigate(['/employees', employee.id]);
  }

  protected addEmployee(): void {
    this.dialog.open(EmployeeFormDialog).afterClosed().subscribe((created?: Employee) => {
      if (created) {
        this.open(created);
      }
    });
  }

  protected clearFilters(): void {
    this.filterForm.reset();
  }
}

function toFilters(value: { query: string; department: string; employmentType: string; country: string; status: StatusFilter }): EmployeeFilters {
  return {
    query: value.query.trim() || undefined,
    department: value.department || undefined,
    employmentType: value.employmentType || undefined,
    country: value.country || undefined,
    active: value.status === 'all' ? undefined : value.status === 'active',
  };
}
