import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { SurveyAttempt } from '../../core/models/app.models';
import { ApprovalService } from '../../core/services/approval.service';
import { ListingComponent } from '../../shared/components/listing/listing.component';

@Component({
  selector: 'app-pending-approvals-list',
  standalone: true,
  imports: [ListingComponent],
  template: `
    <section class="stack">
      <div class="panel-header">
        <div>
          <div class="eyebrow">Survey Approval</div>
          <h2 class="section-title">Queue of pending branch and RBO reviews</h2>
        </div>
      </div>
      <app-listing [columns]="columns" [data]="attempts" (rowClicked)="open($event)" />
    </section>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PendingApprovalsListComponent {
  private readonly approvalService = inject(ApprovalService);
  private readonly router = inject(Router);

  attempts: SurveyAttempt[] = [];
  columns = [
    { key: 'surveyTitle', label: 'Survey' },
    { key: 'branchName', label: 'Branch' },
    { key: 'status', label: 'Status', kind: 'status' as const },
  ];

  constructor() {
    this.approvalService.getPendingApprovals().subscribe((attempts) => (this.attempts = attempts));
  }

  open(row: any): void {
    this.router.navigate(['/app/survey-approval', row['id']]);
  }
}
