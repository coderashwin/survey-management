import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ReversalService } from '../../core/services/reversal.service';

@Component({
  selector: 'app-reversal-create',
  standalone: true,
  imports: [FormsModule],
  template: `
    <section class="panel stack">
      <div class="eyebrow">Create Reversal</div>
      <div class="form-grid">
        <div class="field">
          <label>Survey Attempt ID</label>
          <input type="number" [(ngModel)]="surveyAttemptId" />
        </div>
        <div class="field">
          <label>Branch Code</label>
          <input [(ngModel)]="branchCode" />
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
export class ReversalCreateComponent {
  private readonly reversalService = inject(ReversalService);
  private readonly router = inject(Router);

  surveyAttemptId = 0;
  branchCode = '';
  reason = '';

  create(): void {
    this.reversalService
      .create({ surveyAttemptId: this.surveyAttemptId, branchCode: this.branchCode, reason: this.reason })
      .subscribe(() => this.router.navigate(['/app/reversal']));
  }
}
