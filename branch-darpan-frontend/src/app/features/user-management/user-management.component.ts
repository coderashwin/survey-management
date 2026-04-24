import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { UserRequestSummary, UserSummary } from '../../core/models/app.models';
import { UserService } from '../../core/services/user.service';
import { ActionDef, ColumnDef, ListingComponent } from '../../shared/components/listing/listing.component';

@Component({
  selector: 'app-user-management',
  standalone: true,
  imports: [CommonModule, ListingComponent],
  template: `
    <section class="stack">
      <div class="panel-header">
        <div>
          <div class="eyebrow">Users</div>
          <h2 class="section-title">Jurisdiction user directory</h2>
        </div>
        <button class="btn btn-primary" type="button" (click)="router.navigate(['/app/users/new'])">Create Request</button>
      </div>

      <app-listing [columns]="userColumns" [data]="users" />

      <section class="stack">
        <div class="panel-header">
          <div>
            <div class="eyebrow">Pending Approval</div>
            <h2 class="section-title">User requests waiting on the checker stage</h2>
          </div>
        </div>
        <app-listing
          [columns]="requestColumns"
          [data]="requests"
          [actions]="requestActions"
          (actionTriggered)="handleRequestAction($event)"
        />
      </section>
    </section>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UserManagementComponent {
  readonly router = inject(Router);
  private readonly userService = inject(UserService);

  readonly userColumns: ColumnDef[] = [
    { key: 'pfid', label: 'PFID' },
    { key: 'name', label: 'Name' },
    { key: 'role', label: 'Role' },
    { key: 'branchName', label: 'Branch' },
  ];

  readonly requestColumns: ColumnDef[] = [
    { key: 'pfid', label: 'PFID' },
    { key: 'requestedRole', label: 'Requested Role' },
    { key: 'status', label: 'Status', kind: 'status' },
    { key: 'createdAt', label: 'Created', kind: 'date' },
  ];

  readonly requestActions: ActionDef[] = [
    { label: 'Approve', action: 'approve', tone: 'primary' },
    { label: 'Reject', action: 'reject', tone: 'danger' },
  ];

  users: UserSummary[] = [];
  requests: UserRequestSummary[] = [];

  constructor() {
    this.reload();
  }

  reload(): void {
    this.userService.getUsers().subscribe((users) => (this.users = users));
    this.userService.getPendingRequests().subscribe((requests) => (this.requests = requests));
  }

  handleRequestAction(event: { action: string; row: any }): void {
    const id = Number(event.row['id']);
    if (event.action === 'approve') {
      this.userService.approve(id).subscribe(() => this.reload());
      return;
    }
    const remarks = window.prompt('Enter rejection remarks') ?? '';
    this.userService.reject(id, remarks).subscribe(() => this.reload());
  }
}
