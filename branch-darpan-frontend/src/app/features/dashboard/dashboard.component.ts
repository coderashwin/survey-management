import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { DashboardSummary } from '../../core/models/app.models';
import { DashboardService } from '../../core/services/dashboard.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  template: `
    @if (summary; as data) {
      <section class="grid two">
        <article class="panel stack">
          <div class="eyebrow">Operational Snapshot</div>
          <h2 class="section-title">{{ data.activeSurveyTitle || 'No active survey' }}</h2>
          <p class="muted">This dashboard compresses the flows most likely to require action from the signed-in role.</p>
        </article>

        <div class="stats-grid">
          <article class="stat-card"><span class="muted">Users</span><strong>{{ data.totalUsers }}</strong></article>
          <article class="stat-card"><span class="muted">Pending User Requests</span><strong>{{ data.pendingUserRequests }}</strong></article>
          <article class="stat-card"><span class="muted">Pending Survey Approvals</span><strong>{{ data.pendingSurveyApprovals }}</strong></article>
          <article class="stat-card"><span class="muted">Approved Surveys</span><strong>{{ data.approvedSurveys }}</strong></article>
        </div>
      </section>
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DashboardComponent {
  private readonly dashboardService = inject(DashboardService);

  summary?: DashboardSummary;

  constructor() {
    this.dashboardService.getDashboard().subscribe((summary) => (this.summary = summary));
  }
}
