import { Injectable, inject } from '@angular/core';
import { ApiService } from './api.service';

@Injectable({ providedIn: 'root' })
export class ApprovalService {
  private readonly api = inject(ApiService);
  getPendingApprovals = () => this.api.getPendingApprovals();
  approveQuestion = (attemptId: number, questionId: number, payload: unknown) =>
    this.api.approveQuestion(attemptId, questionId, payload);
  submitAll = (attemptId: number) => this.api.submitApproval(attemptId);
}
