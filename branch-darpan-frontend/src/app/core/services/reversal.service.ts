import { Injectable, inject } from '@angular/core';
import { ApiService } from './api.service';

@Injectable({ providedIn: 'root' })
export class ReversalService {
  private readonly api = inject(ApiService);
  create = (payload: unknown) => this.api.createReversal(payload);
  list = () => this.api.getReversals();
  get = (id: number) => this.api.getReversal(id);
  approve = (id: number, remarks?: string) => this.api.approveReversal(id, remarks);
  reject = (id: number, remarks?: string) => this.api.rejectReversal(id, remarks);
}
