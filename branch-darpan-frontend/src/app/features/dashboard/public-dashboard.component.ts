import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { DashboardSummary } from '../../core/models/app.models';
import { DashboardService } from '../../core/services/dashboard.service';

@Component({
  selector: 'app-public-dashboard',
  standalone: true,
  imports: [CommonModule],
  template: `
    @if (summary; as data) {
      <section class="stats-grid">
        <article class="stat-card"><span class="muted">Users</span><strong>{{ data.totalUsers }}</strong></article>
        <article class="stat-card"><span class="muted">Pending Requests</span><strong>{{ data.pendingUserRequests }}</strong></article>
        <article class="stat-card"><span class="muted">Pending Approvals</span><strong>{{ data.pendingSurveyApprovals }}</strong></article>
        <article class="stat-card"><span class="muted">Active Survey</span><strong>{{ data.activeSurveyTitle || 'None' }}</strong></article>
      </section>
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PublicDashboardComponent {
  private readonly dashboardService = inject(DashboardService);

  summary?: DashboardSummary;

  constructor() {
    this.dashboardService.getPublicDashboard().subscribe((summary) => (this.summary = summary));
  }
}
