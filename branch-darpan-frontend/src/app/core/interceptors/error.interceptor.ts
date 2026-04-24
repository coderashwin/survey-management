import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthStore } from '../auth/auth.store';

export const errorInterceptor: HttpInterceptorFn = (request, next) => {
  const router = inject(Router);
  const store = inject(AuthStore);
  return next(request).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        store.clearAuth();
        router.navigate(['/login']);
      }
      const message = error.error?.message ?? error.message ?? 'Request failed';
      console.error(`[API] ${request.method} ${request.url}: ${message}`);
      return throwError(() => error);
    }),
  );
};
