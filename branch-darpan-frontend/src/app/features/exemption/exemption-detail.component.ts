import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { WorkflowRequest } from '../../core/models/app.models';
import { ExemptionService } from '../../core/services/exemption.service';
import { ApprovalWrapperComponent } from '../../shared/components/approval-wrapper/approval-wrapper.component';

@Component({
  selector: 'app-exemption-detail',
  standalone: true,
  imports: [ApprovalWrapperComponent],
  template: `
    @if (item) {
      <app-approval-wrapper mode="approval" title="Exemption Request" (approve)="approve($event)" (reject)="reject($event)">
        <p><strong>Branch:</strong> {{ item.branchCode }}</p>
        <p><strong>Status:</strong> {{ item.status }}</p>
        <p class="muted">{{ item.reason }}</p>
      </app-approval-wrapper>
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ExemptionDetailComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly exemptionService = inject(ExemptionService);

  item?: WorkflowRequest;

  constructor() {
    this.exemptionService.get(Number(this.route.snapshot.paramMap.get('id'))).subscribe((item) => (this.item = item));
  }

  approve(remarks: string): void {
    if (!this.item) {
      return;
    }
    this.exemptionService.approve(this.item.id, remarks).subscribe(() => this.router.navigate(['/app/exemption']));
  }

  reject(remarks: string): void {
    if (!this.item) {
      return;
    }
    this.exemptionService.reject(this.item.id, remarks).subscribe(() => this.router.navigate(['/app/exemption']));
  }
}
