import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { forkJoin } from 'rxjs';
import { Survey, SurveyAttempt } from '../../core/models/app.models';
import { ApprovalService } from '../../core/services/approval.service';
import { SurveyService } from '../../core/services/survey.service';
import { QuestionnaireComponent } from '../../shared/components/questionnaire/questionnaire.component';

@Component({
  selector: 'app-survey-approval',
  standalone: true,
  imports: [CommonModule, QuestionnaireComponent],
  template: `
    @if (survey && attempt) {
      <app-questionnaire
        [surveyData]="survey"
        [attemptData]="attempt"
        mode="approval"
        (submitApproval)="submitApproval($event)"
      />
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SurveyApprovalComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly surveyService = inject(SurveyService);
  private readonly approvalService = inject(ApprovalService);

  survey?: Survey;
  attempt?: SurveyAttempt;

  constructor() {
    const attemptId = Number(this.route.snapshot.paramMap.get('attemptId'));
    this.surveyService.getAttempt(attemptId).subscribe((attempt) => {
      this.attempt = attempt;
      this.surveyService.getSurvey(attempt.surveyId).subscribe((survey) => (this.survey = survey));
    });
  }

  submitApproval(decisions: Record<number, { status: string; remarks: string }>): void {
    if (!this.attempt) {
      return;
    }
    const requests = Object.entries(decisions).map(([questionId, decision]) =>
      this.approvalService.approveQuestion(this.attempt!.id, Number(questionId), decision),
    );

    forkJoin(requests).subscribe(() => {
      this.approvalService.submitAll(this.attempt!.id).subscribe(() => this.router.navigate(['/app/survey-approval']));
    });
  }
}
