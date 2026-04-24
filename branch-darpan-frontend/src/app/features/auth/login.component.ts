import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="page-shell">
      <section class="hero-panel grid two">
        <div class="stack">
          <div class="eyebrow">Secure Access</div>
          <h1 class="hero-title">Role-aware entry into Branch Darpan.</h1>
          <p class="muted">
            For local development, the demo SSO token format is <code>ROLE:PFID</code>. Example:
            <code>BRANCH_MAKER:12345678</code>.
          </p>
        </div>

        <div class="panel">
          <div class="field">
            <label for="token">SSO Token</label>
            <input id="token" [(ngModel)]="ssoToken" />
          </div>
          <div class="btn-row" style="margin-top: 1rem;">
            <button class="btn btn-primary" type="button" (click)="login()">Sign In</button>
            <a class="btn btn-secondary" routerLink="/public/dashboard">Open Public Dashboard</a>
          </div>
        </div>
      </section>
    </div>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoginComponent {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  ssoToken = 'BRANCH_MAKER:12345678';

  login(): void {
    this.authService.login(this.ssoToken).subscribe(() => {
      this.router.navigate(['/app/dashboard']);
    });
  }
}
