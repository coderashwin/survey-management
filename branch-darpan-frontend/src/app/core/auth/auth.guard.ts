import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthStore } from './auth.store';

export const authGuard: CanActivateFn = () => {
  const store = inject(AuthStore);
  const router = inject(Router);
  store.hydrate();
  if (!store.token()) {
    router.navigate(['/login']);
    return false;
  }
  return true;
};
