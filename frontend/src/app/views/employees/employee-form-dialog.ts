import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { HumanizePipe } from '../../shared/format';
import { EmployeeService } from '../../services/employee.service';
import { MetaService } from '../../services/meta.service';
import { Employee, EmployeeProfile } from '../../models';

/** Creates an employee, or edits one when an employee is passed as dialog data. Closes with the saved employee. */
@Component({
  selector: 'app-employee-form-dialog',
  imports: [ReactiveFormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatButtonModule, HumanizePipe],
  template: `
    <h2 mat-dialog-title>{{ existing ? 'Edit ' + existing.fullName : 'Add employee' }}</h2>
    <form [formGroup]="form" (ngSubmit)="save()">
      <mat-dialog-content class="form-grid">
        <mat-form-field>
          <mat-label>Full name</mat-label>
          <input matInput formControlName="fullName" maxlength="120" cdkFocusInitial />
        </mat-form-field>
        <mat-form-field>
          <mat-label>Job title</mat-label>
          <input matInput formControlName="jobTitle" maxlength="120" />
        </mat-form-field>
        <mat-form-field>
          <mat-label>Department</mat-label>
          <mat-select formControlName="department">
            @for (d of meta.departments(); track d) {
              <mat-option [value]="d">{{ d | humanize }}</mat-option>
            }
          </mat-select>
        </mat-form-field>
        <mat-form-field>
          <mat-label>Employment type</mat-label>
          <mat-select formControlName="employmentType">
            @for (t of meta.employmentTypes(); track t) {
              <mat-option [value]="t">{{ t | humanize }}</mat-option>
            }
          </mat-select>
        </mat-form-field>
        <mat-form-field>
          <mat-label>Country</mat-label>
          <mat-select formControlName="country">
            @for (c of meta.countries(); track c.code) {
              <mat-option [value]="c.code">{{ c.name }} ({{ c.currency }})</mat-option>
            }
          </mat-select>
        </mat-form-field>
        @if (existing && changesPayStructure()) {
          <p class="hint">
            Changing department, employment type or country ends any pay that no longer applies
            (e.g. a sales incentive, or pay in the old currency). You can set the new pay afterwards.
          </p>
        }
      </mat-dialog-content>
      <mat-dialog-actions align="end">
        <button mat-button type="button" mat-dialog-close>Cancel</button>
        <button mat-flat-button type="submit" [disabled]="form.invalid || saving">Save</button>
      </mat-dialog-actions>
    </form>
  `,
  styles: `
    .form-grid { display: grid; gap: 4px; min-width: min(420px, 80vw); padding-top: 8px; }
    .hint { margin: 0; color: var(--mat-sys-on-surface-variant); font: var(--mat-sys-body-small); }
  `,
})
export class EmployeeFormDialog {
  private readonly employeeService = inject(EmployeeService);
  private readonly dialogRef = inject<MatDialogRef<EmployeeFormDialog, Employee>>(MatDialogRef);
  protected readonly meta = inject(MetaService);
  protected readonly existing = inject<Employee | null>(MAT_DIALOG_DATA, { optional: true });
  protected saving = false;

  protected readonly form = inject(FormBuilder).nonNullable.group({
    fullName: [this.existing?.fullName ?? '', [Validators.required, Validators.maxLength(120)]],
    jobTitle: [this.existing?.jobTitle ?? '', [Validators.required, Validators.maxLength(120)]],
    department: [this.existing?.department ?? '', Validators.required],
    employmentType: [this.existing?.employmentType ?? 'FTE', Validators.required],
    country: [this.existing?.country ?? '', Validators.required],
  });

  protected changesPayStructure(): boolean {
    const { department, employmentType, country } = this.form.getRawValue();
    return !!this.existing && (department !== this.existing.department
      || employmentType !== this.existing.employmentType || country !== this.existing.country);
  }

  protected save(): void {
    if (this.form.invalid) {
      return;
    }
    const profile: EmployeeProfile = this.form.getRawValue();
    this.saving = true;
    const request = this.existing
      ? this.employeeService.update(this.existing.id, profile)
      : this.employeeService.create(profile);
    request.subscribe({
      next: (saved) => this.dialogRef.close(saved),
      error: () => (this.saving = false),
    });
  }
}
