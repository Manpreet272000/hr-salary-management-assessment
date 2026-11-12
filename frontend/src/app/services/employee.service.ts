import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Employee, EmployeeFilters, EmployeeProfile, Page } from '../models';
import { PageRequest, toParams } from '../shared/http-params';

/** Employee directory: search, profile and status. Backed by EmployeeController. */
@Injectable({ providedIn: 'root' })
export class EmployeeService {
  private readonly http = inject(HttpClient);

  search(filters: EmployeeFilters, page: PageRequest): Observable<Page<Employee>> {
    return this.http.get<Page<Employee>>('/api/employees', { params: toParams({ ...filters, ...page }) });
  }

  get(id: number): Observable<Employee> {
    return this.http.get<Employee>(`/api/employees/${id}`);
  }

  create(profile: EmployeeProfile): Observable<Employee> {
    return this.http.post<Employee>('/api/employees', profile);
  }

  update(id: number, profile: EmployeeProfile): Observable<Employee> {
    return this.http.put<Employee>(`/api/employees/${id}`, profile);
  }

  setActive(id: number, active: boolean): Observable<Employee> {
    return this.http.patch<Employee>(`/api/employees/${id}/status`, { active });
  }
}
