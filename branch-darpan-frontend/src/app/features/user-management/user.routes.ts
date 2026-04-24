import { Routes } from '@angular/router';
import { UserManagementComponent } from './user-management.component';
import { UserRequestPageComponent } from './user-request-page.component';

export const USER_ROUTES: Routes = [
  { path: '', component: UserManagementComponent },
  { path: 'new', component: UserRequestPageComponent },
];
