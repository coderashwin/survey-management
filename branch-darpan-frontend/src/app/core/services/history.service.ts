import { Injectable, inject } from '@angular/core';
import { ApiService } from './api.service';

@Injectable({ providedIn: 'root' })
export class HistoryService {
  private readonly api = inject(ApiService);
  list = (params: Record<string, string | number | undefined>) => this.api.getHistory(params);
  get = (id: number) => this.api.getHistoryItem(id);
}
