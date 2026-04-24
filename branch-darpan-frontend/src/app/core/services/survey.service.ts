import { Injectable, inject } from '@angular/core';
import { ApiService } from './api.service';

@Injectable({ providedIn: 'root' })
export class SurveyService {
  private readonly api = inject(ApiService);
  getSurveys = () => this.api.getSurveys();
  getActiveSurvey = () => this.api.getActiveSurvey();
  getSurvey = (id: number) => this.api.getSurvey(id);
  updateEndDate = (id: number, endDate: string) => this.api.updateSurveyEndDate(id, endDate);
  createAttempt = (surveyId: number) => this.api.createAttempt(surveyId);
  updateAttempt = (id: number, payload: unknown) => this.api.updateAttempt(id, payload);
  getAttempt = (id: number) => this.api.getAttempt(id);
  getMyAttempts = () => this.api.getMyAttempts();
  uploadFile = (file: File) => this.api.uploadFile(file);
}
