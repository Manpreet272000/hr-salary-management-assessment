import { HttpErrorResponse } from '@angular/common/http';
import { problemMessage } from './problem.interceptor';

describe('problemMessage', () => {
  it('shows the problem detail from the backend', () => {
    const error = new HttpErrorResponse({ status: 422, error: { detail: 'Employee EMP000001 is inactive' } });
    expect(problemMessage(error)).toBe('Employee EMP000001 is inactive');
  });

  it('lists field validation errors', () => {
    const error = new HttpErrorResponse({
      status: 400,
      error: { detail: 'Request validation failed', errors: { fullName: 'must not be blank' } },
    });
    expect(problemMessage(error)).toBe('Request validation failed · fullName: must not be blank');
  });

  it('explains when the server is unreachable', () => {
    expect(problemMessage(new HttpErrorResponse({ status: 0 }))).toContain('Cannot reach the server');
  });
});
