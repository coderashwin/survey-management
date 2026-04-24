import { Routes } from '@angular/router';
import { ExemptionCreateComponent } from './exemption-create.component';
import { ExemptionDetailComponent } from './exemption-detail.component';
import { ExemptionListComponent } from './exemption-list.component';

export const EXEMPTION_ROUTES: Routes = [
  { path: '', component: ExemptionListComponent },
  { path: 'create', component: ExemptionCreateComponent },
  { path: ':id', component: ExemptionDetailComponent },
];
