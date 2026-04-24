import { Injectable, inject } from '@angular/core';
import { ApiService } from './api.service';

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private readonly api = inject(ApiService);
  getDashboard = () => this.api.getDashboard(false);
  getPublicDashboard = () => this.api.getDashboard(true);
}
