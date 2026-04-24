# Branch Darpan — Part 4: Shared Components & Delivery Roadmap

## 🧾 QUESTIONNAIRE COMPONENT (Most Critical)

This single component handles all 4 modes: **fill**, **reattempt**, **approval**, **view**.

```typescript
// shared/components/questionnaire/questionnaire.component.ts
@Component({
  selector: 'app-questionnaire',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatRadioModule, MatButtonModule, MatInputModule, MatExpansionModule],
  templateUrl: './questionnaire.component.html',
})
export class QuestionnaireComponent {
  mode = input<'fill' | 'reattempt' | 'approval' | 'view'>('view');
  surveyData = input<Survey>();
  attemptData = input<SurveyAttempt>();

  // For approval mode — per-question decisions
  approvalState = signal<Record<number, { status: string; remarks: string }>>({});

  // Selected mutually exclusive section (Anytime Channels vs eCorner)
  selectedExclusiveSection = signal<number | null>(null);

  isQuestionEditable(question: Question, answer?: SurveyAnswer): boolean {
    switch (this.mode()) {
      case 'fill': return true;
      case 'reattempt':
        // Only RBO-rejected questions are editable
        return answer?.rboCheckerStatus === 'REJECTED';
      case 'approval': return false; // Checker uses approve/reject buttons
      case 'view': return false;
    }
  }

  isQuestionLocked(answer?: SurveyAnswer): boolean {
    return answer?.isLocked === true; // RBO-approved = locked
  }

  isSectionVisible(section: Section): boolean {
    if (!section.isMutuallyExclusiveWith) return true;
    const selected = this.selectedExclusiveSection();
    return selected === null || selected === section.id;
  }

  shouldShowQuestion(question: Question): boolean {
    if (!question.dependsOnQuestionId) return true;
    const depAnswer = this.getAnswer(question.dependsOnQuestionId);
    return depAnswer?.answerValue === question.dependsOnAnswer;
  }

  setApprovalDecision(questionId: number, status: string, remarks: string) {
    this.approvalState.update(s => ({ ...s, [questionId]: { status, remarks } }));
  }

  onSaveDraft = output<Record<number, any>>();
  onSubmit = output<Record<number, any>>();
  onSubmitApproval = output<Record<number, { status: string; remarks: string }>>();
}
```

### Template Structure (questionnaire.component.html)

```html
<!-- Loop through sections -->
<mat-accordion>
  @for (section of surveyData()?.sections; track section.id) {
    @if (isSectionVisible(section)) {
      <mat-expansion-panel>
        <mat-expansion-panel-header>
          <mat-panel-title>{{ section.name }}</mat-panel-title>
        </mat-expansion-panel-header>

        <!-- Mutually exclusive selector -->
        @if (section.isMutuallyExclusiveWith && mode() !== 'view') {
          <div class="exclusive-selector">
            <mat-radio-group [(ngModel)]="selectedExclusiveSection">
              <mat-radio-button [value]="section.id">{{ section.name }}</mat-radio-button>
            </mat-radio-group>
          </div>
        }

        <!-- Subsections -->
        @for (sub of section.subsections; track sub.id) {
          <h4>{{ sub.name }}</h4>
          @for (q of sub.questions; track q.id) {
            @if (shouldShowQuestion(q)) {
              <app-question-item
                [question]="q"
                [answer]="getAnswer(q.id)"
                [mode]="mode()"
                [editable]="isQuestionEditable(q, getAnswer(q.id))"
                [locked]="isQuestionLocked(getAnswer(q.id))"
                [approvalDecision]="approvalState()[q.id]"
                (answerChanged)="onAnswerChanged(q.id, $event)"
                (approvalChanged)="setApprovalDecision(q.id, $event.status, $event.remarks)"
              />
            }
          }
        }
      </mat-expansion-panel>
    }
  }
</mat-accordion>

<!-- Action buttons based on mode -->
<div class="actions">
  @switch (mode()) {
    @case ('fill') {
      <button mat-stroked-button (click)="onSaveDraft.emit(collectAnswers())">Save Draft</button>
      <button mat-raised-button color="primary" (click)="onSubmit.emit(collectAnswers())">Submit</button>
    }
    @case ('reattempt') {
      <button mat-stroked-button (click)="onSaveDraft.emit(collectAnswers())">Save Draft</button>
      <button mat-raised-button color="primary" (click)="onSubmit.emit(collectAnswers())">Resubmit</button>
    }
    @case ('approval') {
      <button mat-raised-button color="primary" (click)="onSubmitApproval.emit(approvalState())">Submit Decisions</button>
    }
  }
</div>
```

### Question Item Sub-Component

```typescript
// shared/components/questionnaire/question-item.component.ts
@Component({ selector: 'app-question-item', standalone: true })
export class QuestionItemComponent {
  question = input<Question>();
  answer = input<SurveyAnswer>();
  mode = input<string>('view');
  editable = input<boolean>(false);
  locked = input<boolean>(false);
  approvalDecision = input<{ status: string; remarks: string }>();
  answerChanged = output<any>();
  approvalChanged = output<{ status: string; remarks: string }>();
}
```

Template renders based on `question.optionType`:
- **RADIO** → `mat-radio-group` with dynamic options
- **FILE_UPLOAD** → `<input type="file">` with preview
- **MONTH_PICKER** → `<input type="month">`

In **approval mode**, each question shows:
- The answer (readonly)
- Approve / Reject buttons
- Remarks text field (required if rejected)
- Status badge showing branch_checker/rbo_checker status

---

## 📋 LISTING COMPONENT (Reusable)

```typescript
// shared/components/listing/listing.component.ts
@Component({ selector: 'app-listing', standalone: true })
export class ListingComponent {
  columns = input<ColumnDef[]>([]);
  data = input<any[]>([]);
  actions = input<ActionDef[]>([]);
  onAction = output<{ action: string; row: any }>();
  // Supports pagination, sorting, filtering via MatTableDataSource
}

// Usage in any feature:
// <app-listing [columns]="cols" [data]="items" [actions]="actions" (onAction)="handleAction($event)" />
```

---

## 🔄 APPROVAL WRAPPER COMPONENT

```typescript
// shared/components/approval-wrapper/approval-wrapper.component.ts
@Component({ selector: 'app-approval-wrapper', standalone: true })
export class ApprovalWrapperComponent {
  mode = input<'approval' | 'view'>('view');
  title = input<string>('');
  onApprove = output<string>();  // emits remarks
  onReject = output<string>();   // emits remarks (mandatory)
}
```

Used by: Reversal approval, Exemption approval, User approval.

---

## 👤 USER FORM COMPONENT

```typescript
// shared/components/user-form/user-form.component.ts
@Component({ selector: 'app-user-form', standalone: true })
export class UserFormComponent {
  mode = input<'create' | 'view' | 'approval'>('create');

  pfidControl = new FormControl('');
  hrmsData = signal<HrmsUser | null>(null);
  selectedRole = signal<string>('');
  allowedRoles = signal<string[]>([]);
  isLoading = signal(false);

  fetchHrms() {
    // Call api.fetchHrms(pfid) → populate hrmsData signal
    // Compute allowedRoles based on current user's role
  }

  submit() {
    // POST to /users/request with HRMS data + selected role
  }
}
```

**Flow**: Enter PFID → Fetch → Show readonly preview → Select role → Submit

---

## 📂 FEATURE MODULES — KEY IMPLEMENTATIONS

### Survey Feature (Branch Maker)

```typescript
// features/survey/survey.routes.ts
export const SURVEY_ROUTES: Routes = [
  { path: '', component: SurveyListComponent },         // Listing
  { path: ':id', component: SurveyAttemptComponent },    // Fill/Reattempt/View via query param ?mode=
];

// features/survey/survey-list.component.ts — Shows:
// - Active survey (top card) with "Attempt" button
// - Past approved surveys below
// - Checks for existing draft → "Resume Draft" button

// features/survey/survey-attempt.component.ts — Wraps QuestionnaireComponent
// Reads ?mode= query param and passes to <app-questionnaire [mode]="mode">
// Handles saveDraft, submit, reattempt events
```

### Survey Approval Feature (Branch Checker / RBO Checker)

```typescript
// features/survey-approval/approval.routes.ts
export const APPROVAL_ROUTES: Routes = [
  { path: '', component: PendingApprovalsListComponent },
  { path: ':attemptId', component: SurveyApprovalComponent },
];

// SurveyApprovalComponent wraps QuestionnaireComponent in approval mode
// On submitApproval → calls API for each question decision → then submit all
```

### History Feature

```typescript
// features/history/history.component.ts
// Uses ListingComponent with columns: Request Type, Creator PFID, Status, Date
// Click row → navigate based on HISTORY_NAV_MAP:
const HISTORY_NAV_MAP: Record<string, { route: string; mode: string }> = {
  'USER_CREATE':     { route: '/app/users/form',  mode: 'view' },
  'SURVEY_UPDATE':   { route: '/app/surveys',     mode: 'view' },
  'REVERSAL':        { route: '/app/reversal',    mode: 'view' },
  'EXEMPTION':       { route: '/app/exemption',   mode: 'view' },
  'BRANCH_MAKER_CHANGE': { route: '/app/users/form', mode: 'view' },
};
// For checker roles, pending items open in 'approval' mode instead of 'view'
```

### Reversal Feature

```typescript
// features/reversal/reversal.routes.ts
export const REVERSAL_ROUTES: Routes = [
  { path: '', component: ReversalListComponent },
  { path: 'create', component: ReversalCreateComponent },   // Circle Maker
  { path: ':id', component: ReversalDetailComponent },       // View/Approve
];
// Circle Maker: selects branch + survey → provides reason → submits
// Checkers: view details + approve/reject via ApprovalWrapper
```

### Exemption Feature

```typescript
// features/exemption/exemption.routes.ts
export const EXEMPTION_ROUTES: Routes = [
  { path: '', component: ExemptionListComponent },
  { path: 'create', component: ExemptionCreateComponent },   // Circle Maker
  { path: ':id', component: ExemptionDetailComponent },
];
// Same pattern as Reversal
```

---

## 📊 DRAFT PERSISTENCE

```typescript
// core/services/draft.service.ts
@Injectable({ providedIn: 'root' })
export class DraftService {
  private api = inject(ApiService);

  // Save to both localStorage (instant) and server (background)
  save(surveyId: number, answers: Record<number, any>) {
    localStorage.setItem(`draft_${surveyId}`, JSON.stringify(answers));
    this.api.saveDraft(surveyId, { answers, lastSavedAt: new Date().toISOString() }).subscribe();
  }

  // Restore: try server first, fallback to localStorage
  restore(surveyId: number): Observable<Record<number, any>> {
    return this.api.getDraft(surveyId).pipe(
      map(res => res.draftData.answers),
      catchError(() => {
        const local = localStorage.getItem(`draft_${surveyId}`);
        return of(local ? JSON.parse(local) : {});
      })
    );
  }

  clear(surveyId: number) {
    localStorage.removeItem(`draft_${surveyId}`);
    this.api.deleteDraft(surveyId).subscribe();
  }
}
```

---

## 🗓️ DELIVERY ROADMAP (6 Sprints × 2 Weeks)

| Sprint | Duration | Deliverables |
|--------|----------|-------------|
| **1** | Week 1-2 | Project setup (Angular + Spring Boot), DB schema, Auth (SSO + JWT), Shell layout, Topbar, Role guards, Login page |
| **2** | Week 3-4 | User Management (PFID→HRMS→Role→Submit), User approval flow, Listing component, User Form component |
| **3** | Week 5-6 | Questionnaire component (all 4 modes), Survey listing (Branch Maker), Fill + Save Draft + Submit, CC Maker survey end-date management |
| **4** | Week 7-8 | Branch Checker approval flow (per-question), RBO Checker approval flow, Reattempt mode, Auto-forward logic |
| **5** | Week 9-10 | Reversal workflow (Circle Maker → CC Checker chain), Exemption workflow (same chain), History tab (all roles, navigation mapping) |
| **6** | Week 11-12 | MIS Dashboard (defer details), Polish UI (SBI branding), E2E testing, Bug fixes, Performance optimization |

---

## 📌 KEY DESIGN DECISIONS

| Decision | Rationale |
|----------|-----------|
| Single QuestionnaireComponent with `mode` input | Eliminates duplication across fill/approval/view/reattempt |
| Signal-based stores (not NgRx) | Lightweight, sufficient for this app's complexity |
| All feature routes lazy-loaded | Fast initial load, bundles loaded on demand |
| NAV_CONFIG drives topbar + guards derive from same source | Single source of truth for role access |
| History tab is universal | Same ListingComponent, navigation map drives routing |
| Draft saved to both localStorage + server | Instant save UX + cross-device persistence |
| Per-question approval state in `survey_answers` table | Avoids separate approval table, keeps data co-located |
| Mutually exclusive sections via `is_mutually_exclusive_with` FK | DB-driven, not hardcoded in frontend |
| Cron job creates surveys | CC Maker doesn't manually create, only adjusts end date |

---

## 🧪 TESTING STRATEGY

| Type | Tool | Coverage |
|------|------|----------|
| Unit (Backend) | JUnit 5 + Mockito | Services, Role hierarchy, Approval logic |
| Unit (Frontend) | Jasmine + Karma | Components, Guards, Interceptors |
| Integration (Backend) | Spring Boot Test + Testcontainers (MySQL) | API endpoints, DB operations |
| E2E | Cypress | Critical flows: Login → Fill survey → Approve → History |

---

## 🚀 GETTING STARTED — FIRST COMMANDS

```bash
# 1. Backend
cd d:\Branch Darpan
mkdir branch-darpan-backend && cd branch-darpan-backend
# Use start.spring.io to generate project with:
#   Group: com.sbi
#   Artifact: branch-darpan
#   Dependencies: Spring Web, Spring Security, Spring Data JPA, MySQL, Lombok, Validation

# 2. Frontend
cd d:\Branch Darpan
ng new branch-darpan-frontend --standalone --routing --style=scss --skip-tests
cd branch-darpan-frontend
ng add @angular/material

# 3. Database
mysql -u root -p -e "CREATE DATABASE branch_darpan;"
mysql -u root -p branch_darpan < docs/schema.sql
```

---

## 📄 DOCUMENT INDEX

| File | Contents |
|------|----------|
| `01-PROJECT-SETUP-AND-DATABASE.md` | Requirements, tech stack, full MySQL schema, project structure, setup |
| `02-BACKEND-API-AND-SERVICES.md` | Spring Boot config, security, all REST API contracts, service logic, cron |
| `03-ANGULAR-FRONTEND-IMPLEMENTATION.md` | Angular config, routing, auth store, guards, interceptors, API service, topbar |
| `04-ANGULAR-COMPONENTS-AND-DELIVERY.md` | Shared components (Questionnaire, Listing, UserForm), features, draft, delivery roadmap |
