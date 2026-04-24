import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HrmsUser, RoleCode, UserRequestSummary } from '../../../core/models/app.models';
import { UserService } from '../../../core/services/user.service';

@Component({
  selector: 'app-user-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <section class="panel stack">
      <div class="panel-header">
        <div>
          <div class="eyebrow">{{ mode | uppercase }}</div>
          <h2 class="section-title">User Management Request</h2>
        </div>
      </div>

      @if (mode === 'create') {
        <div class="form-grid">
          <div class="field">
            <label for="pfid">PFID</label>
            <input id="pfid" [(ngModel)]="pfid" />
          </div>
          <div class="field">
            <label for="role">Requested Role</label>
            <select id="role" [(ngModel)]="selectedRole">
              @for (role of allowedRoles; track role) {
                <option [value]="role">{{ role }}</option>
              }
            </select>
          </div>
        </div>

        <div class="btn-row">
          <button class="btn btn-secondary" type="button" (click)="fetchHrms()">Fetch from HRMS</button>
          <button class="btn btn-primary" type="button" (click)="submitRequest()" [disabled]="!hrmsData()">Submit Request</button>
        </div>
      }

      @if (hrmsData(); as hrms) {
        <div class="grid two">
          <article class="stat-card">
            <span class="muted">Employee</span>
            <strong>{{ hrms.name }}</strong>
            <p class="muted">{{ hrms.designation }}</p>
          </article>
          <article class="stat-card">
            <span class="muted">Jurisdiction</span>
            <strong>{{ hrms.branchName || hrms.circleName }}</strong>
            <p class="muted">PFID {{ hrms.pfid }}</p>
          </article>
        </div>
      }

      @if (request) {
        <div class="stack">
          <p><strong>PFID:</strong> {{ request.pfid }}</p>
          <p><strong>Requested Role:</strong> {{ request.requestedRole }}</p>
          <p><strong>Status:</strong> {{ request.status }}</p>
          <p class="muted">{{ request.remarks || 'No remarks yet.' }}</p>
        </div>
      }
    </section>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UserFormComponent {
  private readonly userService = inject(UserService);

  @Input() mode: 'create' | 'view' | 'approval' = 'create';
  @Input() allowedRoles: RoleCode[] = [];
  @Input() request?: UserRequestSummary | null;

  @Output() requestCreated = new EventEmitter<unknown>();

  readonly hrmsData = signal<HrmsUser | null>(null);

  pfid = '';
  selectedRole = '' as RoleCode;

  fetchHrms(): void {
    if (!this.pfid) {
      return;
    }
    this.userService.fetchHrms(this.pfid).subscribe((response) => this.hrmsData.set(response));
  }

  submitRequest(): void {
    const hrms = this.hrmsData();
    if (!hrms || !this.selectedRole) {
      return;
    }
    this.requestCreated.emit({
      pfid: hrms.pfid,
      requestedRole: this.selectedRole,
      name: hrms.name,
      email: hrms.email,
      mobile: hrms.mobile,
      designation: hrms.designation,
      circleCode: hrms.circleCode,
      circleName: hrms.circleName,
      aoCode: hrms.aoCode,
      aoName: hrms.aoName,
      rboCode: hrms.rboCode,
      rboName: hrms.rboName,
      branchCode: hrms.branchCode,
      branchName: hrms.branchName,
    });
  }
}
