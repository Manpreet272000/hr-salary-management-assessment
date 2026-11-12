import { HttpParams } from '@angular/common/http';

/** Builds query params, dropping unset values so the backend treats them as "any". */
export function toParams(values: Record<string, string | number | boolean | undefined | null>): HttpParams {
  let params = new HttpParams();
  for (const [key, value] of Object.entries(values)) {
    if (value !== undefined && value !== null && value !== '') {
      params = params.set(key, String(value));
    }
  }
  return params;
}

export interface PageRequest {
  page: number;
  size: number;
  /** e.g. "fullName,asc" */
  sort?: string;
}
