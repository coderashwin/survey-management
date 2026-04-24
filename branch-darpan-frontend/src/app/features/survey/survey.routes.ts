import { Routes } from '@angular/router';
import { SurveyAttemptComponent } from './survey-attempt.component';
import { SurveyListComponent } from './survey-list.component';

export const SURVEY_ROUTES: Routes = [
  { path: '', component: SurveyListComponent },
  { path: ':id', component: SurveyAttemptComponent },
];
