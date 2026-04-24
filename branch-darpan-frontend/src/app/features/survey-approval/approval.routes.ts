import { Routes } from '@angular/router';
import { PendingApprovalsListComponent } from './pending-approvals-list.component';
import { SurveyApprovalComponent } from './survey-approval.component';

export const APPROVAL_ROUTES: Routes = [
  { path: '', component: PendingApprovalsListComponent },
  { path: ':attemptId', component: SurveyApprovalComponent },
];
