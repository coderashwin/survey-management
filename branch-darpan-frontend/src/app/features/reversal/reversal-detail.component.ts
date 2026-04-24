import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { WorkflowRequest } from '../../core/models/app.models';
import { ReversalService } from '../../core/services/reversal.service';
import { ApprovalWrapperComponent } from '../../shared/components/approval-wrapper/approval-wrapper.component';

@Component({
  selector: 'app-reversal-detail',
  standalone: true,
  imports: [ApprovalWrapperComponent],
  template: `
    @if (item) {
      <app-approval-wrapper
        mode="approval"
        title="Reversal Request"
        (approve)="approve($event)"
        (reject)="reject($event)"
      >
        <p><strong>Branch:</strong> {{ item.branchCode }}</p>
        <p><strong>Status:</strong> {{ item.status }}</p>
        <p class="muted">{{ item.reason }}</p>
      </app-approval-wrapper>
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ReversalDetailComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly reversalService = inject(ReversalService);

  item?: WorkflowRequest;

  constructor() {
    this.reversalService.get(Number(this.route.snapshot.paramMap.get('id'))).subscribe((item) => (this.item = item));
  }

  approve(remarks: string): void {
    if (!this.item) {
      return;
    }
    this.reversalService.approve(this.item.id, remarks).subscribe(() => this.router.navigate(['/app/reversal']));
  }

  reject(remarks: string): void {
    if (!this.item) {
      return;
    }
    this.reversalService.reject(this.item.id, remarks).subscribe(() => this.router.navigate(['/app/reversal']));
  }
}
