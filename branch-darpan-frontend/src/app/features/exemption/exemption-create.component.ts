import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ExemptionService } from '../../core/services/exemption.service';

@Component({
  selector: 'app-exemption-create',
  standalone: true,
  imports: [FormsModule],
  template: `
    <section class="panel stack">
      <div class="eyebrow">Create Exemption</div>
      <div class="form-grid">
        <div class="field">
          <label>Survey ID</label>
          <input type="number" [(ngModel)]="surveyId" />
        </div>
        <div class="field">
          <label>Branch Code</label>
          <input [(ngModel)]="branchCode" />
        </div>
        <div class="field">
          <label>Branch Name</label>
          <input [(ngModel)]="branchName" />
        </div>
      </div>
      <div class="field">
        <label>Reason</label>
        <textarea [(ngModel)]="reason"></textarea>
      </div>
      <button class="btn btn-primary" type="button" (click)="create()">Submit</button>
    </section>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ExemptionCreateComponent {
  private readonly exemptionService = inject(ExemptionService);
  private readonly router = inject(Router);

  surveyId = 0;
  branchCode = '';
  branchName = '';
  reason = '';

  create(): void {
    this.exemptionService
      .create({ surveyId: this.surveyId, branchCode: this.branchCode, branchName: this.branchName, reason: this.reason })
      .subscribe(() => this.router.navigate(['/app/exemption']));
  }
}
