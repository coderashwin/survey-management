import { RoleCode } from '../models/app.models';

export interface NavItem {
  label: string;
  route: string;
}

export const NAV_CONFIG: Record<RoleCode, NavItem[]> = {
  SUPER_ADMIN: [
    { label: 'Dashboard', route: '/app/dashboard' },
    { label: 'User Management', route: '/app/users' },
    { label: 'History', route: '/app/history' },
  ],
  CC_MAKER: [
    { label: 'Dashboard', route: '/app/dashboard' },
    { label: 'Manage Questionnaire', route: '/app/questionnaire-config' },
    { label: 'Manage Survey', route: '/app/surveys' },
    { label: 'Exempt Branch', route: '/app/exemption' },
    { label: 'User Management', route: '/app/users' },
    { label: 'History', route: '/app/history' },
  ],
  CC_CHECKER: [
    { label: 'Dashboard', route: '/app/dashboard' },
    { label: 'Manage Questionnaire', route: '/app/questionnaire-config' },
    { label: 'Manage Survey', route: '/app/surveys' },
    { label: 'Exempt Branch', route: '/app/exemption' },
    { label: 'User Management', route: '/app/users' },
    { label: 'History', route: '/app/history' },
  ],
  CIRCLE_MAKER: [
    { label: 'Dashboard', route: '/app/dashboard' },
    { label: 'User Management', route: '/app/users' },
    { label: 'Reverse Survey', route: '/app/reversal' },
    { label: 'Exempt Branch', route: '/app/exemption' },
    { label: 'History', route: '/app/history' },
  ],
  CIRCLE_CHECKER: [
    { label: 'Dashboard', route: '/app/dashboard' },
    { label: 'User Management', route: '/app/users' },
    { label: 'Reverse Survey', route: '/app/reversal' },
    { label: 'Exempt Branch', route: '/app/exemption' },
    { label: 'History', route: '/app/history' },
  ],
  AO_MAKER: [
    { label: 'Dashboard', route: '/app/dashboard' },
    { label: 'User Management', route: '/app/users' },
    { label: 'History', route: '/app/history' },
  ],
  AO_CHECKER: [
    { label: 'Dashboard', route: '/app/dashboard' },
    { label: 'User Management', route: '/app/users' },
    { label: 'History', route: '/app/history' },
  ],
  RBO_MAKER: [
    { label: 'Dashboard', route: '/app/dashboard' },
    { label: 'User Management', route: '/app/users' },
    { label: 'History', route: '/app/history' },
  ],
  RBO_CHECKER: [
    { label: 'Dashboard', route: '/app/dashboard' },
    { label: 'Pending Surveys', route: '/app/survey-approval' },
    { label: 'User Management', route: '/app/users' },
    { label: 'History', route: '/app/history' },
  ],
  BRANCH_MAKER: [
    { label: 'Dashboard', route: '/app/dashboard' },
    { label: 'Surveys', route: '/app/surveys' },
    { label: 'History', route: '/app/history' },
  ],
  BRANCH_CHECKER: [
    { label: 'Dashboard', route: '/app/dashboard' },
    { label: 'Pending Surveys', route: '/app/survey-approval' },
    { label: 'User Management', route: '/app/users' },
    { label: 'History', route: '/app/history' },
  ],
};
