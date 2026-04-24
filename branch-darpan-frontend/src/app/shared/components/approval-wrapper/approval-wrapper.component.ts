import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-approval-wrapper',
  standalone: true,
  imports: [FormsModule],
  template: `
    <section class="panel stack">
      <div class="panel-header">
        <div>
          <div class="eyebrow">{{ mode === 'approval' ? 'Approval Mode' : 'Read Only' }}</div>
          <h2 class="section-title">{{ title }}</h2>
        </div>
      </div>

      <ng-content />

      @if (mode === 'approval') {
        <div class="field">
          <label for="remarks">Remarks</label>
          <textarea id="remarks" [(ngModel)]="remarks"></textarea>
        </div>
        <div class="actions">
          <button class="btn btn-primary" type="button" (click)="approve.emit(remarks)">Approve</button>
          <button class="btn btn-danger" type="button" (click)="reject.emit(remarks)">Reject</button>
        </div>
      }
    </section>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ApprovalWrapperComponent {
  @Input() mode: 'approval' | 'view' = 'view';
  @Input() title = '';
  @Output() approve = new EventEmitter<string>();
  @Output() reject = new EventEmitter<string>();

  remarks = '';
}
