import { CommonModule, DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { SurveySummary } from '../../core/models/app.models';
import { SurveyService } from '../../core/services/survey.service';

@Component({
  selector: 'app-questionnaire-config',
  standalone: true,
  imports: [CommonModule, FormsModule, DatePipe],
  template: `
    <section class="stack">
      <div class="panel-header">
        <div>
          <div class="eyebrow">Survey Control</div>
          <h2 class="section-title">Active survey schedule management</h2>
        </div>
      </div>

      @for (survey of surveys; track survey.id) {
        <article class="panel stack">
          <div class="panel-header">
            <div>
              <strong>{{ survey.title }}</strong>
              <p class="muted">{{ survey.startDate | date: 'mediumDate' }} to {{ survey.endDate | date: 'mediumDate' }}</p>
            </div>
            <span class="pill" [class.approved]="survey.isActive" [class.pending]="!survey.isActive">
              {{ survey.isActive ? 'ACTIVE' : 'ARCHIVED' }}
            </span>
          </div>
          <div class="btn-row">
            <input type="date" [(ngModel)]="endDates[survey.id]" />
            <button class="btn btn-primary" type="button" (click)="updateEndDate(survey)">Update End Date</button>
          </div>
        </article>
      }
    </section>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class QuestionnaireConfigComponent {
  private readonly surveyService = inject(SurveyService);

  surveys: SurveySummary[] = [];
  endDates: Record<number, string> = {};

  constructor() {
    this.surveyService.getSurveys().subscribe((surveys) => {
      this.surveys = surveys;
      surveys.forEach((survey) => {
        this.endDates[survey.id] = survey.endDate;
      });
    });
  }

  updateEndDate(survey: SurveySummary): void {
    this.surveyService.updateEndDate(survey.id, this.endDates[survey.id]).subscribe();
  }
}
