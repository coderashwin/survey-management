import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, EventEmitter, Input, OnChanges, Output, SimpleChanges, inject, signal } from '@angular/core';
import { Survey, SurveyAnswer, SurveyAttempt } from '../../../core/models/app.models';
import { SurveyService } from '../../../core/services/survey.service';
import { QuestionItemComponent } from './question-item.component';

@Component({
  selector: 'app-questionnaire',
  standalone: true,
  imports: [CommonModule, QuestionItemComponent],
  template: `
    @if (surveyData) {
      <div class="stack">
        @for (section of surveyData.sections; track section.id) {
          @if (isSectionVisible(section.id, section.isMutuallyExclusiveWith)) {
            <section class="panel stack">
              <div class="panel-header">
                <div>
                  <div class="eyebrow">Section {{ section.displayOrder }}</div>
                  <h2 class="section-title">{{ section.name }}</h2>
                </div>
                @if (section.isMutuallyExclusiveWith && mode !== 'view') {
                  <button class="btn btn-secondary" type="button" (click)="toggleExclusive(section.id)">
                    {{ selectedExclusiveSection() === section.id ? 'Selected' : 'Choose this section' }}
                  </button>
                }
              </div>

              @for (subsection of section.subsections; track subsection.id) {
                <div class="stack">
                  <strong>{{ subsection.name }}</strong>
                  @for (question of subsection.questions; track question.id) {
                    @if (shouldShowQuestion(question.id, question.dependsOnQuestionId, question.dependsOnAnswer)) {
                      <app-question-item
                        [question]="question"
                        [answer]="answers()[question.id]"
                        [mode]="mode"
                        [disabled]="!isQuestionEditable(answers()[question.id])"
                        (answerChanged)="updateAnswer(question.id, $event)"
                        (approvalChanged)="updateApproval(question.id, $event)"
                      />
                    }
                  }
                </div>
              }
            </section>
          }
        }
      </div>

      <div class="actions" style="margin-top: 1rem;">
        @if (mode === 'fill' || mode === 'reattempt') {
          <button class="btn btn-secondary" type="button" (click)="saveDraft.emit(answerMap())">Save Draft</button>
          <button class="btn btn-primary" type="button" (click)="submitForm.emit(buildSubmitPayload())">
            {{ mode === 'reattempt' ? 'Resubmit' : 'Submit' }}
          </button>
        }
        @if (mode === 'approval') {
          <button class="btn btn-primary" type="button" (click)="submitApproval.emit(approvalState())">Submit Decisions</button>
        }
      </div>
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class QuestionnaireComponent implements OnChanges {
  private readonly surveyService = inject(SurveyService);

  @Input({ required: true }) surveyData!: Survey | null;
  @Input() attemptData: SurveyAttempt | null = null;
  @Input() mode: 'fill' | 'reattempt' | 'approval' | 'view' = 'view';

  @Output() saveDraft = new EventEmitter<Record<number, SurveyAnswer>>();
  @Output() submitForm = new EventEmitter<{ answers: Array<{ questionId: number; answerValue?: string; filePath?: string }>; files: Record<number, File> }>();
  @Output() submitApproval = new EventEmitter<Record<number, { status: string; remarks: string }>>();

  readonly answers = signal<Record<number, SurveyAnswer>>({});
  readonly approvalState = signal<Record<number, { status: string; remarks: string }>>({});
  readonly selectedExclusiveSection = signal<number | null>(null);
  private readonly fileMap: Record<number, File> = {};

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['attemptData'] && this.attemptData) {
      const answerState = this.attemptData.answers.reduce<Record<number, SurveyAnswer>>((acc, answer) => {
        acc[answer.questionId] = answer;
        return acc;
      }, {});
      this.answers.set(answerState);
    }
  }

  toggleExclusive(sectionId: number): void {
    this.selectedExclusiveSection.set(sectionId);
  }

  isSectionVisible(sectionId: number, linkedId?: number | null): boolean {
    if (!linkedId) {
      return true;
    }
    const selected = this.selectedExclusiveSection();
    return selected === null || selected === sectionId;
  }

  shouldShowQuestion(questionId: number, dependencyId?: number | null, dependencyValue?: string | null): boolean {
    if (!dependencyId) {
      return true;
    }
    const dependency = this.answers()[dependencyId];
    return dependency?.answerValue === dependencyValue;
  }

  isQuestionEditable(answer?: SurveyAnswer): boolean {
    if (this.mode === 'view') {
      return false;
    }
    if (this.mode === 'approval') {
      return false;
    }
    if (this.mode === 'reattempt') {
      return answer?.rboCheckerStatus === 'REJECTED' || !answer?.isLocked;
    }
    return true;
  }

  updateAnswer(questionId: number, payload: { answerValue?: string; file?: File }): void {
    this.answers.update((current) => ({
      ...current,
      [questionId]: {
        ...(current[questionId] ?? { questionId }),
        questionId,
        answerValue: payload.answerValue,
        filePath: payload.file ? payload.file.name : current[questionId]?.filePath,
      },
    }));
    if (payload.file) {
      this.fileMap[questionId] = payload.file;
    }
  }

  updateApproval(questionId: number, approval: { status: string; remarks: string }): void {
    this.approvalState.update((current) => ({
      ...current,
      [questionId]: approval,
    }));
  }

  answerMap(): Record<number, SurveyAnswer> {
    return this.answers();
  }

  buildSubmitPayload() {
    const answers = Object.values(this.answers()).map((answer) => ({
      questionId: answer.questionId,
      answerValue: answer.answerValue ?? undefined,
      filePath: answer.filePath ?? undefined,
    }));
    return { answers, files: this.fileMap };
  }
}
