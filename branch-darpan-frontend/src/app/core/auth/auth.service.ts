import { Injectable, inject } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, catchError, map, of, tap } from 'rxjs';
import { ApiService } from '../services/api.service';
import { AuthResponse, UserProfile } from '../models/app.models';
import { AuthStore } from './auth.store';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly api = inject(ApiService);
  private readonly store = inject(AuthStore);
  private readonly router = inject(Router);

  login(ssoToken: string): Observable<AuthResponse> {
    return this.api.login(ssoToken).pipe(tap((response) => this.store.setAuth(response)));
  }

  validateToken(): Observable<UserProfile | null> {
    return this.api.validateToken().pipe(
      tap((user) => {
        const token = this.store.token();
        if (token) {
          this.store.setAuth({ jwt: token, user });
        }
      }),
      catchError(() => {
        this.store.clearAuth();
        return of(null);
      }),
    );
  }

  bootstrapSession(): Observable<boolean> {
    this.store.hydrate();
    if (!this.store.token()) {
      return of(false);
    }
    return this.validateToken().pipe(map((user) => !!user));
  }

  logout(): void {
    this.store.clearAuth();
    this.router.navigate(['/login']);
  }
}
