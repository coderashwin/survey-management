import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { TopbarComponent } from '../../shared/components/topbar/topbar.component';
import { AuthStore } from '../../core/auth/auth.store';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [RouterOutlet, TopbarComponent],
  template: `
    <div class="page-shell stack">
      <app-topbar />
      <section class="hero-panel">
        <div class="eyebrow">{{ store.role() ?? 'SECURE WORKSPACE' }}</div>
        <div class="grid two">
          <div>
            <h1 class="hero-title">Decision-heavy workflows, routed without friction.</h1>
            <p class="muted">
              User management, survey approvals, reversals, exemptions, and audit history are organized around the
              current role and jurisdiction.
            </p>
          </div>
          <div class="stats-grid">
            <article class="stat-card">
              <span class="muted">PFID</span>
              <strong>{{ store.user()?.pfid ?? 'N/A' }}</strong>
            </article>
            <article class="stat-card">
              <span class="muted">Branch</span>
              <strong>{{ store.user()?.branchName ?? 'Jurisdiction-wide' }}</strong>
            </article>
          </div>
        </div>
      </section>
      <router-outlet />
    </div>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ShellComponent {
  readonly store = inject(AuthStore);
}
