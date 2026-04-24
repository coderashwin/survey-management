import { Injectable, inject } from '@angular/core';
import { ApiService } from './api.service';

@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly api = inject(ApiService);
  fetchHrms = (pfid: string) => this.api.fetchHrms(pfid);
  getUsers = () => this.api.getUsers();
  getUser = (id: number) => this.api.getUser(id);
  submitRequest = (payload: unknown) => this.api.submitUserRequest(payload);
  getPendingRequests = () => this.api.getPendingUserRequests();
  approve = (id: number, remarks?: string) => this.api.approveUser(id, remarks);
  reject = (id: number, remarks: string) => this.api.rejectUser(id, remarks);
}
