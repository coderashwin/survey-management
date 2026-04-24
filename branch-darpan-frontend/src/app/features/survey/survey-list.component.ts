import { CommonModule, DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { Survey, SurveyAttempt } from '../../core/models/app.models';
import { SurveyService } from '../../core/services/survey.service';
import { ListingComponent } from '../../shared/components/listing/listing.component';

@Component({
  selector: 'app-survey-list',
  standalone: true,
  imports: [CommonModule, DatePipe, ListingComponent],
  template: `
    <section class="stack">
      @if (activeSurvey; as survey) {
        <article class="hero-panel">
          <div class="eyebrow">Active Survey</div>
          <h2 class="section-title">{{ survey.title }}</h2>
          <p class="muted">{{ survey.startDate | date: 'mediumDate' }} to {{ survey.endDate | date: 'mediumDate' }}</p>
          <div class="btn-row">
            <button class="btn btn-primary" type="button" (click)="openSurvey(survey.id)">Start / Resume</button>
          </div>
        </article>
      }

      <section class="stack">
        <div class="panel-header">
          <div>
            <div class="eyebrow">My Attempts</div>
            <h2 class="section-title">Survey history and reattempt loops</h2>
          </div>
        </div>
        <app-listing
          [columns]="columns"
          [data]="attempts"
          (rowClicked)="openAttempt($event)"
        />
      </section>
    </section>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SurveyListComponent {
  private readonly surveyService = inject(SurveyService);
  private readonly router = inject(Router);

  activeSurvey?: Survey;
  attempts: SurveyAttempt[] = [];
  columns = [
    { key: 'surveyTitle', label: 'Survey' },
    { key: 'status', label: 'Status', kind: 'status' as const },
    { key: 'attemptNumber', label: 'Attempt' },
    { key: 'submittedAt', label: 'Submitted', kind: 'date' as const },
  ];

  constructor() {
    this.surveyService.getActiveSurvey().subscribe((survey) => (this.activeSurvey = survey));
    this.surveyService.getMyAttempts().subscribe((attempts) => (this.attempts = attempts));
  }

  openSurvey(surveyId: number): void {
    this.router.navigate(['/app/surveys', surveyId], { queryParams: { mode: 'fill' } });
  }

  openAttempt(row: any): void {
    this.router.navigate(['/app/surveys', row['surveyId']], {
      queryParams: { attemptId: row['id'], mode: 'view' },
    });
  }
}
