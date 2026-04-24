import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-public-layout',
  standalone: true,
  imports: [RouterOutlet],
  template: `
    <div class="page-shell">
      <div class="hero-panel">
        <div class="eyebrow">SBI Public View</div>
        <h1 class="hero-title">Branch Darpan oversight, streamlined for action.</h1>
        <p class="muted">
          Public-facing dashboard access is deliberately narrow. Internal workflows remain protected behind role-based
          routing and JWT-backed APIs.
        </p>
      </div>
      <div style="height: 1rem"></div>
      <router-outlet />
    </div>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PublicLayoutComponent {}
