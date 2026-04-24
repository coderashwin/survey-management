import { Injectable, computed, signal } from '@angular/core';
import { AuthResponse, UserProfile } from '../models/app.models';

@Injectable({ providedIn: 'root' })
export class AuthStore {
  readonly user = signal<UserProfile | null>(null);
  readonly token = signal<string | null>(localStorage.getItem('jwt'));
  readonly role = computed(() => this.user()?.role ?? null);
  readonly isLoggedIn = computed(() => !!this.token());

  hydrate(): void {
    const rawUser = localStorage.getItem('user');
    const rawToken = localStorage.getItem('jwt');
    if (rawUser && rawToken) {
      this.user.set(JSON.parse(rawUser) as UserProfile);
      this.token.set(rawToken);
    }
  }

  setAuth(response: AuthResponse): void {
    this.user.set(response.user);
    this.token.set(response.jwt);
    localStorage.setItem('jwt', response.jwt);
    localStorage.setItem('user', JSON.stringify(response.user));
  }

  clearAuth(): void {
    this.user.set(null);
    this.token.set(null);
    localStorage.removeItem('jwt');
    localStorage.removeItem('user');
  }
}
