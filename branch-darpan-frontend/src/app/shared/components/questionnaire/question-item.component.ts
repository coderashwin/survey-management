import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Question, SurveyAnswer } from '../../../core/models/app.models';

@Component({
  selector: 'app-question-item',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <article class="question-card">
      <div class="panel-header">
        <div>
          <strong>{{ question.questionText }}</strong>
          <p class="muted">Type: {{ question.optionType }}</p>
        </div>
        @if (answer?.isLocked) {
          <span class="pill approved">Locked</span>
        }
      </div>

      @if (mode === 'approval') {
        <p class="muted">Answer: {{ answer?.answerValue || answer?.filePath || 'No answer provided' }}</p>
        <div class="btn-row">
          <button class="btn btn-secondary" type="button" (click)="setDecision('APPROVED')">Approve</button>
          <button class="btn btn-danger" type="button" (click)="setDecision('REJECTED')">Reject</button>
        </div>
        <div class="field" style="margin-top: 0.8rem;">
          <label>Remarks</label>
          <textarea [(ngModel)]="remarks" (ngModelChange)="emitDecision()"></textarea>
        </div>
      } @else if (question.optionType === 'RADIO') {
        <div class="question-options">
          @for (option of question.options; track option.id) {
            <label class="question-option">
              <input
                type="radio"
                [name]="'q-' + question.id"
                [disabled]="disabled"
                [checked]="option.optionValue === answerValue"
                (change)="updateValue(option.optionValue)"
              />
              <span>{{ option.optionText }}</span>
            </label>
          }
        </div>
      } @else if (question.optionType === 'MONTH_PICKER') {
        <input type="month" [disabled]="disabled" [ngModel]="answerValue" (ngModelChange)="updateValue($event)" />
      } @else if (question.optionType === 'FILE_UPLOAD' || question.optionType === 'FILE') {
        <input type="file" [disabled]="disabled" (change)="handleFile($event)" />
        @if (answer?.filePath) {
          <p class="muted">Attached: {{ answer?.filePath }}</p>
        }
      } @else {
        <textarea [disabled]="disabled" [ngModel]="answerValue" (ngModelChange)="updateValue($event)"></textarea>
      }
    </article>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class QuestionItemComponent {
  @Input({ required: true }) question!: Question;
  @Input() answer?: SurveyAnswer | null;
  @Input() mode: 'fill' | 'reattempt' | 'approval' | 'view' = 'view';
  @Input() disabled = false;
  @Output() answerChanged = new EventEmitter<{ answerValue?: string; file?: File }>();
  @Output() approvalChanged = new EventEmitter<{ status: string; remarks: string }>();

  remarks = '';

  get answerValue(): string | null | undefined {
    return this.answer?.answerValue;
  }

  updateValue(value: string): void {
    this.answerChanged.emit({ answerValue: value });
  }

  handleFile(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (file) {
      this.answerChanged.emit({ answerValue: file.name, file });
    }
  }

  setDecision(status: 'APPROVED' | 'REJECTED'): void {
    this.approvalChanged.emit({ status, remarks: this.remarks });
  }

  emitDecision(): void {
    this.approvalChanged.emit({ status: this.answer?.branchCheckerStatus ?? 'PENDING', remarks: this.remarks });
  }
}
