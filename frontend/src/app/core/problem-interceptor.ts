import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { catchError, throwError } from 'rxjs';

/**
 * Shows the backend's problem detail (RFC 9457) for any failed request, so screens don't each
 * need their own error handling. The error is re-thrown for callers that want to react further.
 */
export const problemInterceptor: HttpInterceptorFn = (req, next) => {
  const snackBar = inject(MatSnackBar);
  return next(req).pipe(
    catchError((error: unknown) => {
      if (error instanceof HttpErrorResponse) {
        snackBar.open(problemMessage(error), 'Dismiss', { duration: 6000 });
      }
      return throwError(() => error);
    }),
  );
};

export function problemMessage(error: HttpErrorResponse): string {
  const problem = error.error as { detail?: string; errors?: Record<string, string> } | null;
  if (error.status === 0) {
    return 'Cannot reach the server. Is the backend running?';
  }
  const fieldErrors = problem?.errors
    ? Object.entries(problem.errors).map(([field, message]) => `${field}: ${message}`)
    : [];
  return [problem?.detail ?? `Request failed (${error.status})`, ...fieldErrors].join(' · ');
}
