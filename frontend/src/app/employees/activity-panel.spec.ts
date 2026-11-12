import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ActivityPanel } from './activity-panel';

describe('ActivityPanel', () => {
  it("lists the employee's audited changes with who made them and how", async () => {
    TestBed.configureTestingModule({ imports: [ActivityPanel], providers: [provideHttpClient(), provideHttpClientTesting()] });
    const http = TestBed.inject(HttpTestingController);
    const fixture = TestBed.createComponent(ActivityPanel);
    fixture.componentRef.setInput('employeeId', 7);
    fixture.detectChanges();

    const req = http.expectOne((r) => r.url === '/api/audit');
    expect(req.request.params.get('employeeId')).toBe('7');
    req.flush({
      content: [{
        id: 1, employeeId: 7, subject: 'PAY', operation: 'Revise pay', actor: 'HR Manager',
        summary: 'Pay changed: REGULAR INR 50,000.00 → INR 60,000.00', occurredAt: '2026-11-11T10:00:00Z',
        details: { before: { REGULAR: 'INR 50,000.00' }, after: { REGULAR: 'INR 60,000.00' } },
      }],
      page: 0, size: 10, totalElements: 1, totalPages: 1,
    });
    await fixture.whenStable();

    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('Pay changed: REGULAR INR 50,000.00 → INR 60,000.00');
    expect(text).toContain('HR Manager · via Revise pay');
    http.verify();
  });
});
