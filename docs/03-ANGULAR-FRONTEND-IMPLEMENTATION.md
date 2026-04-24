# Branch Darpan — Part 3: Angular Frontend Implementation

## 🎨 DESIGN SYSTEM (styles.scss)

```scss
// SBI Brand Colors
:root {
  --sbi-blue: #002D62;
  --sbi-blue-light: #0A4D8C;
  --sbi-gold: #F5A623;
  --sbi-white: #FFFFFF;
  --sbi-gray-50: #F8F9FA;
  --sbi-gray-100: #E9ECEF;
  --sbi-gray-300: #DEE2E6;
  --sbi-gray-600: #6C757D;
  --sbi-gray-900: #212529;
  --sbi-success: #28A745;
  --sbi-danger: #DC3545;
  --sbi-warning: #FFC107;
  --font-primary: 'Inter', sans-serif;
}

body { font-family: var(--font-primary); margin: 0; background: var(--sbi-gray-50); }
```

## 🔧 APP CONFIG (app.config.ts)

```typescript
import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { appRoutes } from './app.routes';
import { authInterceptor } from './core/auth/auth.interceptor';
import { errorInterceptor } from './core/interceptors/error.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(appRoutes),
    provideHttpClient(withInterceptors([authInterceptor, errorInterceptor])),
    provideAnimationsAsync(),
  ]
};
```

## 🗺️ ROUTING (app.routes.ts)

```typescript
import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';
import { roleGuard } from './core/auth/role.guard';

export const appRoutes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', loadComponent: () => import('./features/auth/login.component').then(m => m.LoginComponent) },
  { path: 'public/dashboard', loadComponent: () => import('./features/dashboard/public-dashboard.component').then(m => m.PublicDashboardComponent) },
  {
    path: 'app',
    loadComponent: () => import('./layouts/shell/shell.component').then(m => m.ShellComponent),
    canActivate: [authGuard],
    children: [
      { path: 'dashboard', loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent) },
      { path: 'users', loadChildren: () => import('./features/user-management/user.routes').then(m => m.USER_ROUTES) },
      { path: 'questionnaire-config', loadChildren: () => import('./features/questionnaire-config/qconfig.routes').then(m => m.QCONFIG_ROUTES),
        canActivate: [roleGuard(['CC_MAKER','CC_CHECKER'])] },
      { path: 'surveys', loadChildren: () => import('./features/survey/survey.routes').then(m => m.SURVEY_ROUTES) },
      { path: 'survey-approval', loadChildren: () => import('./features/survey-approval/approval.routes').then(m => m.APPROVAL_ROUTES) },
      { path: 'reversal', loadChildren: () => import('./features/reversal/reversal.routes').then(m => m.REVERSAL_ROUTES) },
      { path: 'exemption', loadChildren: () => import('./features/exemption/exemption.routes').then(m => m.EXEMPTION_ROUTES) },
      { path: 'history', loadComponent: () => import('./features/history/history.component').then(m => m.HistoryComponent) },
    ]
  },
  { path: '**', redirectTo: 'login' }
];
```

## 🔐 AUTH STORE (Signal-based)

```typescript
// core/auth/auth.store.ts
import { signal, computed } from '@angular/core';

export interface User {
  id: number; pfid: string; name: string; email: string;
  role: string; circleCode?: string; circleName?: string;
  aoCode?: string; rboCode?: string; branchCode?: string; branchName?: string;
}

export const authStore = {
  user: signal<User | null>(null),
  token: signal<string | null>(null),
  role: computed(() => authStore.user()?.role ?? null),
  isLoggedIn: computed(() => authStore.user() !== null),
  setAuth: (user: User, token: string) => { authStore.user.set(user); authStore.token.set(token); localStorage.setItem('jwt', token); },
  clearAuth: () => { authStore.user.set(null); authStore.token.set(null); localStorage.removeItem('jwt'); },
};
```

## 🛡️ GUARDS

```typescript
// core/auth/auth.guard.ts
export const authGuard: CanActivateFn = (route, state) => {
  const token = localStorage.getItem('jwt');
  if (!token) { inject(Router).navigate(['/login']); return false; }
  return true;
};

// core/auth/role.guard.ts
export function roleGuard(allowedRoles: string[]): CanActivateFn {
  return () => {
    const role = authStore.role();
    if (!role || !allowedRoles.includes(role)) { inject(Router).navigate(['/app/dashboard']); return false; }
    return true;
  };
}
```

## 🔗 INTERCEPTORS

```typescript
// core/auth/auth.interceptor.ts
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = authStore.token();
  if (token) { req = req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }); }
  return next(req);
};

// core/interceptors/error.interceptor.ts
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) { authStore.clearAuth(); inject(Router).navigate(['/login']); }
      inject(MatSnackBar).open(error.error?.message || 'An error occurred', 'Close', { duration: 5000 });
      return throwError(() => error);
    })
  );
};
```

## 📡 API SERVICE

```typescript
// core/services/api.service.ts
@Injectable({ providedIn: 'root' })
export class ApiService {
  private http = inject(HttpClient);
  private base = environment.apiUrl; // '/api/v1'

  // Auth
  ssoLogin(ssoToken: string) { return this.http.post<AuthResponse>(`${this.base}/auth/sso/login`, { ssoToken }); }
  validateToken() { return this.http.post<User>(`${this.base}/auth/validate`, {}); }

  // HRMS
  fetchHrms(pfid: string) { return this.http.get<HrmsUser>(`${this.base}/hrms/${pfid}`); }

  // Users
  submitUserRequest(dto: UserRequestDto) { return this.http.post(`${this.base}/users/request`, dto); }
  getPendingUserRequests() { return this.http.get<UserRequest[]>(`${this.base}/users/requests`); }
  approveUser(id: number) { return this.http.put(`${this.base}/users/requests/${id}/approve`, {}); }
  rejectUser(id: number, remarks: string) { return this.http.put(`${this.base}/users/requests/${id}/reject`, { remarks }); }

  // Surveys
  getActiveSurvey() { return this.http.get<Survey>(`${this.base}/surveys/active`); }
  getSurvey(id: number) { return this.http.get<Survey>(`${this.base}/surveys/${id}`); }
  updateSurveyEndDate(id: number, endDate: string) { return this.http.put(`${this.base}/surveys/${id}/end-date`, { endDate }); }

  // Survey Attempts
  createAttempt(surveyId: number) { return this.http.post<SurveyAttempt>(`${this.base}/surveys/attempt`, { surveyId }); }
  submitAttempt(id: number, answers: Answer[], action: string) { return this.http.put(`${this.base}/surveys/attempt/${id}`, { action, answers }); }
  getAttempt(id: number) { return this.http.get<SurveyAttempt>(`${this.base}/surveys/attempt/${id}`); }
  getMyAttempts() { return this.http.get<SurveyAttempt[]>(`${this.base}/surveys/attempts/my`); }

  // Drafts
  saveDraft(surveyId: number, data: any) { return this.http.put(`${this.base}/surveys/draft`, { surveyId, draftData: data }); }
  getDraft(surveyId: number) { return this.http.get<any>(`${this.base}/surveys/draft/${surveyId}`); }

  // Approval
  getPendingApprovals() { return this.http.get<SurveyAttempt[]>(`${this.base}/surveys/approval/pending`); }
  approveQuestion(attemptId: number, qId: number, status: string, remarks?: string) {
    return this.http.put(`${this.base}/surveys/approval/${attemptId}/question/${qId}`, { status, remarks });
  }
  submitApprovalDecisions(attemptId: number) { return this.http.put(`${this.base}/surveys/approval/${attemptId}/submit`, {}); }

  // Reversal
  createReversal(dto: any) { return this.http.post(`${this.base}/reversals`, dto); }
  getReversals() { return this.http.get<any[]>(`${this.base}/reversals`); }
  approveReversal(id: number, remarks: string) { return this.http.put(`${this.base}/reversals/${id}/approve`, { remarks }); }
  rejectReversal(id: number, remarks: string) { return this.http.put(`${this.base}/reversals/${id}/reject`, { remarks }); }

  // Exemption
  createExemption(dto: any) { return this.http.post(`${this.base}/exemptions`, dto); }
  getExemptions() { return this.http.get<any[]>(`${this.base}/exemptions`); }
  approveExemption(id: number, remarks: string) { return this.http.put(`${this.base}/exemptions/${id}/approve`, { remarks }); }
  rejectExemption(id: number, remarks: string) { return this.http.put(`${this.base}/exemptions/${id}/reject`, { remarks }); }

  // History
  getHistory(params?: any) { return this.http.get<PagedResponse<HistoryItem>>(`${this.base}/history`, { params }); }

  // Files
  uploadFile(file: File) { const fd = new FormData(); fd.append('file', file); return this.http.post<{path:string}>(`${this.base}/files/upload`, fd); }
}
```

## 🧭 TOPBAR — Role-Based Navigation

```typescript
// shared/components/topbar/topbar.component.ts
const NAV_CONFIG: Record<string, NavItem[]> = {
  'SUPER_ADMIN':    [{label:'Dashboard',route:'/app/dashboard'},{label:'User Management',route:'/app/users'},{label:'History',route:'/app/history'}],
  'CC_MAKER':       [{label:'Dashboard',route:'/app/dashboard'},{label:'Manage Questionnaire',route:'/app/questionnaire-config'},{label:'Manage Survey',route:'/app/surveys'},{label:'Exempt Branch',route:'/app/exemption'},{label:'User Management',route:'/app/users'},{label:'History',route:'/app/history'}],
  'CC_CHECKER':     [{label:'Dashboard',route:'/app/dashboard'},{label:'Manage Questionnaire',route:'/app/questionnaire-config'},{label:'Manage Survey',route:'/app/surveys'},{label:'Exempt Branch',route:'/app/exemption'},{label:'User Management',route:'/app/users'},{label:'History',route:'/app/history'}],
  'CIRCLE_MAKER':   [{label:'Dashboard',route:'/app/dashboard'},{label:'User Management',route:'/app/users'},{label:'Reverse Survey',route:'/app/reversal'},{label:'Exempt Branch',route:'/app/exemption'},{label:'History',route:'/app/history'}],
  'CIRCLE_CHECKER': [{label:'Dashboard',route:'/app/dashboard'},{label:'User Management',route:'/app/users'},{label:'Reverse Survey',route:'/app/reversal'},{label:'Exempt Branch',route:'/app/exemption'},{label:'History',route:'/app/history'}],
  'AO_MAKER':       [{label:'Dashboard',route:'/app/dashboard'},{label:'User Management',route:'/app/users'},{label:'History',route:'/app/history'}],
  'AO_CHECKER':     [{label:'Dashboard',route:'/app/dashboard'},{label:'User Management',route:'/app/users'},{label:'History',route:'/app/history'}],
  'RBO_MAKER':      [{label:'Dashboard',route:'/app/dashboard'},{label:'User Management',route:'/app/users'},{label:'History',route:'/app/history'}],
  'RBO_CHECKER':    [{label:'Dashboard',route:'/app/dashboard'},{label:'User Management',route:'/app/users'},{label:'History',route:'/app/history'}],
  'BRANCH_MAKER':   [{label:'Dashboard',route:'/app/dashboard'},{label:'Surveys',route:'/app/surveys'},{label:'History',route:'/app/history'}],
  'BRANCH_CHECKER': [{label:'Dashboard',route:'/app/dashboard'},{label:'Pending Surveys',route:'/app/survey-approval'},{label:'User Management',route:'/app/users'},{label:'History',route:'/app/history'}],
};
```

---

> **Next:** See `04-ANGULAR-COMPONENTS-AND-DELIVERY.md` for shared components and delivery roadmap.
