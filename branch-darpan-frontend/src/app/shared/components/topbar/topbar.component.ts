import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { NAV_CONFIG } from '../../../core/navigation/nav.config';
import { AuthService } from '../../../core/auth/auth.service';
import { AuthStore } from '../../../core/auth/auth.store';

@Component({
  selector: 'app-topbar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive],
  template: `
    <header class="topbar">
      <div>
        <div class="eyebrow">Branch Darpan</div>
        <strong>{{ store.user()?.name ?? 'Authenticated User' }}</strong>
      </div>

      <nav>
        @for (item of navItems(); track item.route) {
          <a [routerLink]="item.route" routerLinkActive="active">{{ item.label }}</a>
        }
      </nav>

      <div class="btn-row">
        <span class="pill pending">{{ store.role() ?? 'No Role' }}</span>
        <button class="btn btn-secondary" type="button" (click)="logout()">Sign Out</button>
      </div>
    </header>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TopbarComponent {
  readonly store = inject(AuthStore);
  readonly authService = inject(AuthService);
  readonly navItems = computed(() => {
    const role = this.store.role();
    return role ? NAV_CONFIG[role] : [];
  });

  logout(): void {
    this.authService.logout();
  }
}
