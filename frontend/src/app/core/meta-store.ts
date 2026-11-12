import { Injectable, computed, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { HrApi } from './hr-api';

/** Reference data loaded once per session and shared by every screen. */
@Injectable({ providedIn: 'root' })
export class MetaStore {
  readonly meta = toSignal(inject(HrApi).meta());

  readonly departments = computed(() => this.meta()?.departments ?? []);
  readonly employmentTypes = computed(() => this.meta()?.employmentTypes ?? []);
  readonly countries = computed(() => this.meta()?.countries ?? []);
  readonly leaveTypes = computed(() => this.meta()?.leaveTypes ?? []);
  readonly dimensions = computed(() => this.meta()?.dimensions ?? []);
  readonly taxRate = computed(() => this.meta()?.taxRate ?? 0);
}
