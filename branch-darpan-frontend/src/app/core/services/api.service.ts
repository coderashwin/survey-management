import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  ActionResponse,
  AuthResponse,
  DashboardSummary,
  HistoryItem,
  HrmsUser,
  PagedResponse,
  Survey,
  SurveyAttempt,
  SurveySummary,
  UserProfile,
  UserRequestSummary,
  UserSummary,
  WorkflowRequest,
} from '../models/app.models';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiUrl;

  login(ssoToken: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.base}/auth/sso/login`, { ssoToken });
  }

  validateToken(): Observable<UserProfile> {
    return this.http.post<UserProfile>(`${this.base}/auth/validate`, {});
  }

  getDashboard(publicView = false): Observable<DashboardSummary> {
    return this.http.get<DashboardSummary>(`${this.base}/${publicView ? 'public/' : ''}dashboard`);
  }

  fetchHrms(pfid: string): Observable<HrmsUser> {
    return this.http.get<HrmsUser>(`${this.base}/hrms/${pfid}`);
  }

  getUsers(): Observable<UserSummary[]> {
    return this.http.get<UserSummary[]>(`${this.base}/users`);
  }

  getUser(id: number): Observable<UserSummary> {
    return this.http.get<UserSummary>(`${this.base}/users/${id}`);
  }

  submitUserRequest(payload: unknown): Observable<ActionResponse> {
    return this.http.post<ActionResponse>(`${this.base}/users/request`, payload);
  }

  getPendingUserRequests(): Observable<UserRequestSummary[]> {
    return this.http.get<UserRequestSummary[]>(`${this.base}/users/requests`);
  }

  approveUser(id: number, remarks?: string): Observable<ActionResponse> {
    return this.http.put<ActionResponse>(`${this.base}/users/requests/${id}/approve`, { remarks });
  }

  rejectUser(id: number, remarks: string): Observable<ActionResponse> {
    return this.http.put<ActionResponse>(`${this.base}/users/requests/${id}/reject`, { remarks });
  }

  getSurveys(): Observable<SurveySummary[]> {
    return this.http.get<SurveySummary[]>(`${this.base}/surveys`);
  }

  getActiveSurvey(): Observable<Survey> {
    return this.http.get<Survey>(`${this.base}/surveys/active`);
  }

  getSurvey(id: number): Observable<Survey> {
    return this.http.get<Survey>(`${this.base}/surveys/${id}`);
  }

  updateSurveyEndDate(id: number, endDate: string): Observable<Survey> {
    return this.http.put<Survey>(`${this.base}/surveys/${id}/end-date`, { endDate });
  }

  createAttempt(surveyId: number): Observable<SurveyAttempt> {
    return this.http.post<SurveyAttempt>(`${this.base}/surveys/attempt`, { surveyId });
  }

  updateAttempt(id: number, payload: unknown): Observable<SurveyAttempt> {
    return this.http.put<SurveyAttempt>(`${this.base}/surveys/attempt/${id}`, payload);
  }

  getAttempt(id: number): Observable<SurveyAttempt> {
    return this.http.get<SurveyAttempt>(`${this.base}/surveys/attempt/${id}`);
  }

  getMyAttempts(): Observable<SurveyAttempt[]> {
    return this.http.get<SurveyAttempt[]>(`${this.base}/surveys/attempts/my`);
  }

  saveDraft(payload: unknown) {
    return this.http.put(`${this.base}/surveys/draft`, payload);
  }

  getDraft(surveyId: number) {
    return this.http.get<{ surveyId: number; draftData: Record<string, unknown> }>(
      `${this.base}/surveys/draft/${surveyId}`,
    );
  }

  deleteDraft(surveyId: number) {
    return this.http.delete(`${this.base}/surveys/draft/${surveyId}`);
  }

  getPendingApprovals(): Observable<SurveyAttempt[]> {
    return this.http.get<SurveyAttempt[]>(`${this.base}/surveys/approval/pending`);
  }

  approveQuestion(attemptId: number, questionId: number, payload: unknown) {
    return this.http.put(`${this.base}/surveys/approval/${attemptId}/question/${questionId}`, payload);
  }

  submitApproval(attemptId: number) {
    return this.http.put<{ message: string; newStatus: string }>(
      `${this.base}/surveys/approval/${attemptId}/submit`,
      {},
    );
  }

  createReversal(payload: unknown): Observable<WorkflowRequest> {
    return this.http.post<WorkflowRequest>(`${this.base}/reversals`, payload);
  }

  getReversals(): Observable<WorkflowRequest[]> {
    return this.http.get<WorkflowRequest[]>(`${this.base}/reversals`);
  }

  getReversal(id: number): Observable<WorkflowRequest> {
    return this.http.get<WorkflowRequest>(`${this.base}/reversals/${id}`);
  }

  approveReversal(id: number, remarks?: string): Observable<WorkflowRequest> {
    return this.http.put<WorkflowRequest>(`${this.base}/reversals/${id}/approve`, { remarks });
  }

  rejectReversal(id: number, remarks?: string): Observable<WorkflowRequest> {
    return this.http.put<WorkflowRequest>(`${this.base}/reversals/${id}/reject`, { remarks });
  }

  createExemption(payload: unknown): Observable<WorkflowRequest> {
    return this.http.post<WorkflowRequest>(`${this.base}/exemptions`, payload);
  }

  getExemptions(): Observable<WorkflowRequest[]> {
    return this.http.get<WorkflowRequest[]>(`${this.base}/exemptions`);
  }

  getExemption(id: number): Observable<WorkflowRequest> {
    return this.http.get<WorkflowRequest>(`${this.base}/exemptions/${id}`);
  }

  approveExemption(id: number, remarks?: string): Observable<WorkflowRequest> {
    return this.http.put<WorkflowRequest>(`${this.base}/exemptions/${id}/approve`, { remarks });
  }

  rejectExemption(id: number, remarks?: string): Observable<WorkflowRequest> {
    return this.http.put<WorkflowRequest>(`${this.base}/exemptions/${id}/reject`, { remarks });
  }

  getHistory(params: Record<string, string | number | undefined>): Observable<PagedResponse<HistoryItem>> {
    let httpParams = new HttpParams();
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        httpParams = httpParams.set(key, value);
      }
    });
    return this.http.get<PagedResponse<HistoryItem>>(`${this.base}/history`, { params: httpParams });
  }

  getHistoryItem(id: number): Observable<HistoryItem> {
    return this.http.get<HistoryItem>(`${this.base}/history/${id}`);
  }

  uploadFile(file: File): Observable<{ path: string }> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<{ path: string }>(`${this.base}/files/upload`, formData);
  }
}
