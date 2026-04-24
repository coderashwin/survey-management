import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthStore } from '../../core/auth/auth.store';
import { RoleCode } from '../../core/models/app.models';
import { UserService } from '../../core/services/user.service';
import { UserFormComponent } from '../../shared/components/user-form/user-form.component';

const ROLE_CREATION_MAP: Record<RoleCode, RoleCode[]> = {
  SUPER_ADMIN: ['CC_MAKER', 'CC_CHECKER'],
  CC_MAKER: ['CIRCLE_MAKER', 'CIRCLE_CHECKER'],
  CC_CHECKER: [],
  CIRCLE_MAKER: ['AO_MAKER', 'AO_CHECKER'],
  CIRCLE_CHECKER: [],
  AO_MAKER: ['RBO_MAKER', 'RBO_CHECKER'],
  AO_CHECKER: [],
  RBO_MAKER: ['BRANCH_MAKER', 'BRANCH_CHECKER'],
  RBO_CHECKER: [],
  BRANCH_MAKER: [],
  BRANCH_CHECKER: ['BRANCH_MAKER'],
};

@Component({
  selector: 'app-user-request-page',
  standalone: true,
  imports: [UserFormComponent],
  template: `
    <app-user-form
      [allowedRoles]="allowedRoles()"
      (requestCreated)="submit($event)"
    />
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UserRequestPageComponent {
  private readonly store = inject(AuthStore);
  private readonly userService = inject(UserService);
  private readonly router = inject(Router);

  readonly allowedRoles = computed(() => {
    const role = this.store.role();
    return role ? ROLE_CREATION_MAP[role] : [];
  });

  submit(payload: unknown): void {
    this.userService.submitRequest(payload).subscribe(() => this.router.navigate(['/app/users']));
  }
}
