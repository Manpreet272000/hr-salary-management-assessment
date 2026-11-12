import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Employee, PayEstimate } from '../../models';
import { toMonth } from '../../shared/months';
import { PayEstimatePanel } from './pay-estimate-panel';

const employee: Employee = {
  id: 7, code: 'EMP000007', fullName: 'Asha Rao', jobTitle: 'Engineer', department: 'ENGINEERING',
  employmentType: 'FTE', country: 'IN', currency: 'INR', active: true, monthlyGross: 42000,
  managerId: null, createdAt: '2026-01-01T00:00:00Z', updatedAt: '2026-01-01T00:00:00Z',
};

describe('PayEstimatePanel', () => {
  let fixture: ComponentFixture<PayEstimatePanel>;
  let http: HttpTestingController;
  const month = toMonth(new Date());

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [PayEstimatePanel],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    http = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(PayEstimatePanel);
  });

  afterEach(() => http.verify());

  it('builds one leave field per leave type and shows the net payout', async () => {
    fixture.componentRef.setInput('employee', employee);
    fixture.detectChanges();

    http.expectOne('/api/meta').flush({
      departments: [], employmentTypes: [], countries: [], payTypes: [], dimensions: [], taxRate: 0.05,
      leaveTypes: [{ code: 'SICK', paid: true }, { code: 'CASUAL', paid: true }, { code: 'UNPLANNED', paid: false }],
    });
    http.expectOne(`/api/employees/7/attendance/${month}`).flush({
      month, workingDays: 21, payrollCutoff: '2026-11-27',
      leaves: { SICK: 1, CASUAL: 0, UNPLANNED: 3 },
    });
    http.expectOne(`/api/employees/7/pay-estimate?month=${month}`).flush(estimate());
    await fixture.whenStable();

    const element = fixture.nativeElement as HTMLElement;
    const labels = [...element.querySelectorAll('mat-label')].map((l) => l.textContent?.trim());
    expect(labels).toContain('Unplanned (loss of pay)');
    expect(labels).toContain('Sick (paid)');
    const text = element.textContent ?? '';
    expect(text).toContain('Loss of pay: 3 of 21 working days');
    expect(text).toContain('₹34,200.00');
    expect(text).toContain('payroll cutoff');
  });

  it('makes no requests and explains why for an inactive employee', async () => {
    fixture.componentRef.setInput('employee', { ...employee, active: false });
    fixture.detectChanges();
    await fixture.whenStable();

    http.match('/api/meta').forEach((req) => req.flush(null));
    http.expectNone((req) => req.url.startsWith('/api/employees'));
    expect((fixture.nativeElement as HTMLElement).textContent).toContain('is inactive, so no payout is due');
  });

  function estimate(): PayEstimate {
    return {
      employeeId: 7, employeeCode: 'EMP000007', month, currency: 'INR', payrollCutoff: '2026-11-27',
      leaves: { SICK: 1, CASUAL: 0, UNPLANNED: 3 },
      breakdown: {
        fixedPay: 42000, variablePay: 0, grossPay: 42000, workingDays: 21, lossOfPayDays: 3,
        lossOfPayDeduction: 6000, taxablePay: 36000, taxRate: 0.05, tax: 1800, netPay: 34200,
      },
    };
  }
});
