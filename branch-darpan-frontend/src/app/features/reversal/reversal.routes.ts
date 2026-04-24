import { Routes } from '@angular/router';
import { ReversalCreateComponent } from './reversal-create.component';
import { ReversalDetailComponent } from './reversal-detail.component';
import { ReversalListComponent } from './reversal-list.component';

export const REVERSAL_ROUTES: Routes = [
  { path: '', component: ReversalListComponent },
  { path: 'create', component: ReversalCreateComponent },
  { path: ':id', component: ReversalDetailComponent },
];
