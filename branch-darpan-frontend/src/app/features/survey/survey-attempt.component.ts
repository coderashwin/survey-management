import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { forkJoin, of, switchMap } from 'rxjs';
import { Survey, SurveyAnswer, SurveyAttempt } from '../../core/models/app.models';
import { DraftService } from '../../core/services/draft.service';
import { SurveyService } from '../../core/services/survey.service';
import { QuestionnaireComponent } from '../../shared/components/questionnaire/questionnaire.component';

@Component({
  selector: 'app-survey-attempt',
  standalone: true,
  imports: [CommonModule, QuestionnaireComponent],
  template: `
    <section class="stack">
      @if (survey) {
        <app-questionnaire
          [surveyData]="survey"
          [attemptData]="attempt"
          [mode]="mode"
          (saveDraft)="saveDraft($event)"
          (submitForm)="submit($event)"
        />
      }
    </section>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SurveyAttemptComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly surveyService = inject(SurveyService);
  private readonly draftService = inject(DraftService);

  survey: Survey | null = null;
  attempt: SurveyAttempt | null = null;
  mode: 'fill' | 'reattempt' | 'approval' | 'view' = 'fill';

  constructor() {
    const surveyId = Number(this.route.snapshot.paramMap.get('id'));
    const attemptId = this.route.snapshot.queryParamMap.get('attemptId');
    const mode = this.route.snapshot.queryParamMap.get('mode');
    this.mode = (mode as typeof this.mode) || 'fill';

    this.surveyService.getSurvey(surveyId).subscribe((survey) => (this.survey = survey));

    const loadAttempt$ = attemptId
      ? this.surveyService.getAttempt(Number(attemptId))
      : this.surveyService.createAttempt(surveyId);

    loadAttempt$
      .pipe(
        switchMap((attempt) => {
          this.attempt = attempt;
          if (!attemptId) {
            void this.router.navigate([], {
              relativeTo: this.route,
              queryParams: { attemptId: attempt.id, mode: this.mode },
              queryParamsHandling: 'merge',
              replaceUrl: true,
            });
          }
          if (this.mode === 'fill' || this.mode === 'reattempt') {
            return this.draftService.restore(surveyId);
          }
          return of({});
        }),
      )
      .subscribe((draftData) => {
        const answers = Object.entries(draftData).reduce<SurveyAnswer[]>((acc, [questionId, value]) => {
          acc.push({ ...(value as SurveyAnswer), questionId: Number(questionId) });
          return acc;
        }, []);
        if (answers.length && this.attempt) {
          this.attempt = { ...this.attempt, answers };
        }
      });
  }

  saveDraft(answers: Record<number, SurveyAnswer>): void {
    if (!this.survey) {
      return;
    }
    this.draftService.save(this.survey.id, answers as unknown as Record<string, unknown>).subscribe();
  }

  submit(payload: { answers: Array<{ questionId: number; answerValue?: string; filePath?: string }>; files: Record<number, File> }): void {
    if (!this.attempt || !this.survey) {
      return;
    }

    const fileEntries = Object.entries(payload.files);
    const upload$ = fileEntries.length
      ? forkJoin(
          Object.fromEntries(
            fileEntries.map(([questionId, file]) => [
              questionId,
              this.surveyService.uploadFile(file),
            ]),
          ),
        )
      : of({} as Record<string, { path: string }>);

    upload$.subscribe((uploads) => {
      const answers = payload.answers.map((answer) => ({
        ...answer,
        filePath: (uploads as Record<string, { path: string }>)[String(answer.questionId)]?.path ?? answer.filePath,
      }));
      this.surveyService
        .updateAttempt(this.attempt!.id, { action: 'SUBMIT', answers })
        .subscribe(() => this.draftService.clear(this.survey!.id).subscribe(() => this.router.navigate(['/app/surveys'])));
    });
  }
}
