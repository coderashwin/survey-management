export interface UserProfile {
  id: number;
  pfid: string;
  name: string;
  role: RoleCode;
  circleCode?: string | null;
  circleName?: string | null;
  aoCode?: string | null;
  aoName?: string | null;
  rboCode?: string | null;
  rboName?: string | null;
  branchCode?: string | null;
  branchName?: string | null;
}

export interface AuthResponse {
  jwt: string;
  user: UserProfile;
}

export interface HrmsUser {
  pfid: string;
  name: string;
  email?: string;
  mobile?: string;
  designation?: string;
  circleCode?: string;
  circleName?: string;
  aoCode?: string;
  aoName?: string;
  rboCode?: string;
  rboName?: string;
  branchCode?: string;
  branchName?: string;
}

export interface UserSummary extends HrmsUser {
  id: number;
  role: RoleCode;
  isActive: boolean;
}

export interface UserRequestSummary {
  id: number;
  pfid: string;
  requestedRole: RoleCode;
  name: string;
  status: string;
  currentApproverRole?: RoleCode | null;
  createdAt: string;
  remarks?: string | null;
}

export interface QuestionOption {
  id: number;
  optionText: string;
  optionValue: string;
  displayOrder: number;
}

export interface Question {
  id: number;
  questionText: string;
  optionType: string;
  weightage: number;
  frequency: string;
  displayOrder: number;
  dependsOnQuestionId?: number | null;
  dependsOnAnswer?: string | null;
  options: QuestionOption[];
}

export interface Subsection {
  id: number;
  name: string;
  displayOrder: number;
  questions: Question[];
}

export interface Section {
  id: number;
  name: string;
  displayOrder: number;
  isMutuallyExclusiveWith?: number | null;
  subsections: Subsection[];
}

export interface Survey {
  id: number;
  title: string;
  frequency: string;
  startDate: string;
  endDate: string;
  isActive: boolean;
  sections: Section[];
}

export interface SurveySummary {
  id: number;
  title: string;
  frequency: string;
  startDate: string;
  endDate: string;
  isActive: boolean;
}

export interface SurveyAnswer {
  questionId: number;
  answerValue?: string | null;
  filePath?: string | null;
  branchCheckerStatus?: string;
  branchCheckerRemarks?: string | null;
  rboCheckerStatus?: string;
  rboCheckerRemarks?: string | null;
  isLocked?: boolean;
}

export interface SurveyAttempt {
  id: number;
  surveyId: number;
  surveyTitle: string;
  branchCode: string;
  branchName: string;
  status: string;
  attemptNumber: number;
  submittedAt?: string | null;
  answers: SurveyAnswer[];
}

export interface DashboardSummary {
  totalUsers: number;
  pendingUserRequests: number;
  pendingSurveyApprovals: number;
  approvedSurveys: number;
  activeSurveyTitle?: string | null;
}

export interface WorkflowRequest {
  id: number;
  referenceId: number;
  type: 'REVERSAL' | 'EXEMPTION';
  branchCode: string;
  branchName?: string | null;
  reason: string;
  status: string;
  createdAt: string;
}

export interface HistoryItem {
  id: number;
  requestType: string;
  referenceId: number;
  status: string;
  actorPfid: string;
  actorName: string;
  actorRole?: string | null;
  targetPfid?: string | null;
  remarks?: string | null;
  createdAt: string;
}

export interface PagedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
}

export interface ActionResponse {
  id: number;
  status: string;
  message: string;
}

export interface AnswerPayload {
  questionId: number;
  answerValue?: string;
  filePath?: string;
}

export type RoleCode =
  | 'SUPER_ADMIN'
  | 'CC_MAKER'
  | 'CC_CHECKER'
  | 'CIRCLE_MAKER'
  | 'CIRCLE_CHECKER'
  | 'AO_MAKER'
  | 'AO_CHECKER'
  | 'RBO_MAKER'
  | 'RBO_CHECKER'
  | 'BRANCH_MAKER'
  | 'BRANCH_CHECKER';
