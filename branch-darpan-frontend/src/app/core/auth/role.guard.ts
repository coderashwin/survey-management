import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { RoleCode } from '../models/app.models';
import { AuthStore } from './auth.store';

export function roleGuard(allowedRoles: RoleCode[]): CanActivateFn {
  return () => {
    const store = inject(AuthStore);
    const router = inject(Router);
    const role = store.role();
    if (!role || !allowedRoles.includes(role)) {
      router.navigate(['/app/dashboard']);
      return false;
    }
    return true;
  };
}
