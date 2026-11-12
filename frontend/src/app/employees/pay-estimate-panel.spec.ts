import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Employee, PayEstimate } from '../core/models';
import { toMonth } from '../core/months';
import { PayEstimatePanel } from './pay-estimate-panel';

const employee: Employee = {
  id: 7, code: 'EMP000007', fullName: 'Asha Rao', jobTitle: 'Engineer', department: 'ENGINEERING',
  employmentType: 'FTE', country: 'IN', currency: 'INR', active: true, monthlyGross: 42000,
  createdAt: '2026-01-01T00:00:00Z', updatedAt: '2026-01-01T00:00:00Z',
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

  it('shows the net payout after loss of pay and tax', async () => {
    fixture.componentRef.setInput('employee', employee);
    fixture.detectChanges();

    http.expectOne(`/api/employees/7/attendance/${month}`).flush({
      month, workingDays: 21, payrollCutoff: '2026-11-27',
      leaves: { sickLeaves: 1, casualLeaves: 0, unplannedLeaves: 3 },
    });
    http.expectOne(`/api/employees/7/pay-estimate?month=${month}`).flush(estimate());
    await fixture.whenStable();

    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('Loss of pay: 3 of 21 working days');
    expect(text).toContain('₹34,200.00');
    expect(text).toContain('payroll cutoff');
  });

  it('makes no requests and explains why for an inactive employee', async () => {
    fixture.componentRef.setInput('employee', { ...employee, active: false });
    fixture.detectChanges();
    await fixture.whenStable();

    http.expectNone(() => true);
    expect((fixture.nativeElement as HTMLElement).textContent).toContain('is inactive, so no payout is due');
  });

  function estimate(): PayEstimate {
    return {
      employeeId: 7, employeeCode: 'EMP000007', month, currency: 'INR', payrollCutoff: '2026-11-27',
      leaves: { sickLeaves: 1, casualLeaves: 0, unplannedLeaves: 3 },
      breakdown: {
        fixedPay: 42000, variablePay: 0, grossPay: 42000, workingDays: 21, lossOfPayDays: 3,
        lossOfPayDeduction: 6000, taxablePay: 36000, taxRate: 0.05, tax: 1800, netPay: 34200,
      },
    };
  }
});
