import { Injectable, inject } from '@angular/core';
import { ApiService } from './api.service';

@Injectable({ providedIn: 'root' })
export class ExemptionService {
  private readonly api = inject(ApiService);
  create = (payload: unknown) => this.api.createExemption(payload);
  list = () => this.api.getExemptions();
  get = (id: number) => this.api.getExemption(id);
  approve = (id: number, remarks?: string) => this.api.approveExemption(id, remarks);
  reject = (id: number, remarks?: string) => this.api.rejectExemption(id, remarks);
}
