import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { Meta } from '../models';

/** Reference data (departments, countries, leave types…) loaded once and shared by every view. Backed by MetaController. */
@Injectable({ providedIn: 'root' })
export class MetaService {
  readonly meta = toSignal(inject(HttpClient).get<Meta>('/api/meta'));

  readonly departments = computed(() => this.meta()?.departments ?? []);
  readonly employmentTypes = computed(() => this.meta()?.employmentTypes ?? []);
  readonly countries = computed(() => this.meta()?.countries ?? []);
  readonly leaveTypes = computed(() => this.meta()?.leaveTypes ?? []);
  readonly dimensions = computed(() => this.meta()?.dimensions ?? []);
  readonly taxRate = computed(() => this.meta()?.taxRate ?? 0);
}
