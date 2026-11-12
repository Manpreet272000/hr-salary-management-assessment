import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { toParams } from '../shared/http-params';
import { CompensationService } from './compensation.service';
import { EmployeeService } from './employee.service';
import { PayrollService } from './payroll.service';

describe('HTTP services', () => {
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('sends only the employee filters that are set', () => {
    TestBed.inject(EmployeeService)
      .search({ department: 'SALES', active: false, query: undefined }, { page: 2, size: 25, sort: 'fullName,asc' })
      .subscribe();

    const req = http.expectOne((r) => r.url === '/api/employees');
    expect(req.request.params.keys().sort()).toEqual(['active', 'department', 'page', 'size', 'sort']);
    expect(req.request.params.get('active')).toBe('false');
    req.flush({ content: [], page: 2, size: 25, totalElements: 0, totalPages: 0 });
  });

  it('revises pay with a PUT to the pay type', () => {
    TestBed.inject(CompensationService).revise(7, 'INCENTIVE', 1500).subscribe();

    const req = http.expectOne('/api/employees/7/compensation/INCENTIVE');
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual({ amount: 1500 });
    req.flush({});
  });

  it('asks for the pay estimate of a specific month', () => {
    TestBed.inject(PayrollService).estimate(7, '2026-11').subscribe();

    http.expectOne('/api/employees/7/pay-estimate?month=2026-11').flush({});
  });
});

describe('toParams', () => {
  it('drops undefined, null and empty values but keeps false and 0', () => {
    const params = toParams({ a: undefined, b: null, c: '', d: false, e: 0 });
    expect(params.keys()).toEqual(['d', 'e']);
  });
});
