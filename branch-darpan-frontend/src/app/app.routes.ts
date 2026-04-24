import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';
import { roleGuard } from './core/auth/role.guard';

export const appRoutes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'login' },
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login.component').then((m) => m.LoginComponent),
  },
  {
    path: 'public',
    loadComponent: () => import('./layouts/public/public-layout.component').then((m) => m.PublicLayoutComponent),
    children: [
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./features/dashboard/public-dashboard.component').then((m) => m.PublicDashboardComponent),
      },
    ],
  },
  {
    path: 'app',
    loadComponent: () => import('./layouts/shell/shell.component').then((m) => m.ShellComponent),
    children: [
      {
        path: 'dashboard',
        loadComponent: () => import('./features/dashboard/dashboard.component').then((m) => m.DashboardComponent),
      },
      {
        path: 'users',
        loadChildren: () => import('./features/user-management/user.routes').then((m) => m.USER_ROUTES),
      },
      {
        path: 'questionnaire-config',
        canActivate: [roleGuard(['CC_MAKER', 'CC_CHECKER'])],
        loadChildren: () =>
          import('./features/questionnaire-config/qconfig.routes').then((m) => m.QCONFIG_ROUTES),
      },
      {
        path: 'surveys',
        loadChildren: () => import('./features/survey/survey.routes').then((m) => m.SURVEY_ROUTES),
      },
      {
        path: 'survey-approval',
        loadChildren: () =>
          import('./features/survey-approval/approval.routes').then((m) => m.APPROVAL_ROUTES),
      },
      {
        path: 'reversal',
        loadChildren: () => import('./features/reversal/reversal.routes').then((m) => m.REVERSAL_ROUTES),
      },
      {
        path: 'exemption',
        loadChildren: () => import('./features/exemption/exemption.routes').then((m) => m.EXEMPTION_ROUTES),
      },
      {
        path: 'history',
        loadComponent: () => import('./features/history/history.component').then((m) => m.HistoryComponent),
      },
    ],
  },
  { path: '**', redirectTo: 'login' },
];
