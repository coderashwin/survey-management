# Branch Darpan — Part 1: Project Setup & Database Design

## 🎯 PROJECT OVERVIEW

**Branch Darpan** is an enterprise application for SBI with:
- Hierarchical user management (maker-checker pattern)
- Dynamic survey system with per-question approval
- Multi-level approval workflows (Reversal, Exemption)
- MIS dashboards (including public access)

---

## ✅ FINALIZED REQUIREMENTS SUMMARY

### Authentication
- **SSO-based login** (support both external redirect & direct API)
- JWT response contains user role + basic info
- On refresh → validate JWT with backend
- Additional user data (jurisdiction, PFID, name) fetched via profile API

### User Hierarchy (top → bottom)
```
Super Admin → CC Maker/Checker → Circle Maker/Checker → AO Maker/Checker → RBO Maker/Checker → Branch Maker/Checker
```

### User Management Rules
- Maker submits → request locked, no edits until Checker acts
- Checker rejects → Maker can resubmit (new request)
- No notifications — requests appear in History tab
- Branch Checker changing Branch Maker → same User Form → approval by RBO Checker

### Survey Lifecycle
- **1 active survey** at a time (created by cron job)
- CC Maker can only change end date
- Sections/subsections/questions pre-seeded by backend
- No question edits once survey is active

### Survey Flow
```
Branch Maker (fill/draft/submit)
  → Branch Checker (per-question approve/reject)
    → All approved → auto-forward to RBO Checker
    → Any rejected → back to Branch Maker
  → RBO Checker (per-question approve/reject)
    → All approved → survey FULLY APPROVED
    → Any rejected → back to Branch Maker (reattempt only rejected Qs)
```

### Reattempt Rules (after RBO rejection)
- RBO-approved questions → **locked**
- RBO-rejected questions → **editable** by Branch Maker
- Resubmitted → goes to Branch Checker (sees full survey, acts on reattempted Qs only)
- Branch Checker approves → auto-forward to RBO Checker again
- Branch Checker rejects → back to Branch Maker (same loop)

### Reversal Workflow
- Circle Maker initiates (branches under their jurisdiction only)
- Chain: Circle Checker → CC Maker → CC Checker
- On approval → survey resets to initial state (no answers)

### Exemption Workflow
- Circle Maker initiates for specific survey + branch
- Chain: Circle Checker → CC Maker → CC Checker
- On approval → branch doesn't see that survey
- Per-survey only, not permanent

### History Tab (all roles)
- Columns: Request Type | PFID (of creator) | Status | Date
- Granular status: "Pending at Branch Checker", "Rejected by RBO Checker", etc.
- Click row → navigate to relevant component in correct mode

### Mutually Exclusive Sections
- **Anytime Channels** and **eCorner** are mutually exclusive

### Question Frequency
- Monthly or Quarterly (backend-driven via cron)

---

## 🛠️ TECH STACK

| Layer | Technology |
|-------|-----------|
| **Frontend** | Angular 21, Standalone Components, Signals, Angular Material, SCSS |
| **Backend** | Java 21, Spring Boot 3.x, Spring Security (JWT), Spring Data JPA |
| **Database** | MySQL 8.x |
| **Auth** | SSO + JWT |
| **Build** | Maven (backend), Angular CLI (frontend) |
| **API Style** | REST, versioned `/api/v1` |

---

## 🗄️ MySQL DATABASE SCHEMA

### 1. User & Auth Tables

```sql
-- Roles: SUPER_ADMIN, CC_MAKER, CC_CHECKER, CIRCLE_MAKER, CIRCLE_CHECKER,
--        AO_MAKER, AO_CHECKER, RBO_MAKER, RBO_CHECKER, BRANCH_MAKER, BRANCH_CHECKER

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pfid VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    mobile VARCHAR(15),
    designation VARCHAR(100),
    role VARCHAR(30) NOT NULL,
    circle_code VARCHAR(10),
    circle_name VARCHAR(100),
    ao_code VARCHAR(10),
    ao_name VARCHAR(100),
    rbo_code VARCHAR(10),
    rbo_name VARCHAR(100),
    branch_code VARCHAR(10),
    branch_name VARCHAR(100),
    is_active BOOLEAN DEFAULT TRUE,
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE TABLE user_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pfid VARCHAR(20) NOT NULL,
    requested_role VARCHAR(30) NOT NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    mobile VARCHAR(15),
    designation VARCHAR(100),
    circle_code VARCHAR(10),
    circle_name VARCHAR(100),
    ao_code VARCHAR(10),
    ao_name VARCHAR(100),
    rbo_code VARCHAR(10),
    rbo_name VARCHAR(100),
    branch_code VARCHAR(10),
    branch_name VARCHAR(100),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    current_approver_role VARCHAR(30),
    remarks TEXT,
    requested_by BIGINT NOT NULL,
    approved_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (requested_by) REFERENCES users(id),
    FOREIGN KEY (approved_by) REFERENCES users(id)
);
```

### 2. Survey Structure Tables

```sql
CREATE TABLE surveys (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    frequency VARCHAR(20) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    is_active BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE sections (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    survey_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    display_order INT NOT NULL,
    is_mutually_exclusive_with BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (survey_id) REFERENCES surveys(id),
    FOREIGN KEY (is_mutually_exclusive_with) REFERENCES sections(id)
);

CREATE TABLE subsections (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    section_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    display_order INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (section_id) REFERENCES sections(id)
);

CREATE TABLE questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    subsection_id BIGINT NOT NULL,
    question_text TEXT NOT NULL,
    option_type VARCHAR(20) NOT NULL,
    weightage DECIMAL(5,2) DEFAULT 0,
    frequency VARCHAR(20) NOT NULL,
    display_order INT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    depends_on_question_id BIGINT,
    depends_on_answer VARCHAR(200),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (subsection_id) REFERENCES subsections(id),
    FOREIGN KEY (depends_on_question_id) REFERENCES questions(id)
);

CREATE TABLE question_options (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    question_id BIGINT NOT NULL,
    option_text VARCHAR(200) NOT NULL,
    option_value VARCHAR(100) NOT NULL,
    display_order INT NOT NULL,
    FOREIGN KEY (question_id) REFERENCES questions(id)
);
```

### 3. Survey Attempt Tables

```sql
CREATE TABLE survey_attempts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    survey_id BIGINT NOT NULL,
    branch_code VARCHAR(10) NOT NULL,
    branch_name VARCHAR(100),
    status VARCHAR(40) NOT NULL DEFAULT 'DRAFT',
    attempt_number INT DEFAULT 1,
    submitted_by BIGINT,
    branch_checker_id BIGINT,
    rbo_checker_id BIGINT,
    submitted_at TIMESTAMP,
    branch_checker_acted_at TIMESTAMP,
    rbo_checker_acted_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (survey_id) REFERENCES surveys(id),
    FOREIGN KEY (submitted_by) REFERENCES users(id),
    FOREIGN KEY (branch_checker_id) REFERENCES users(id),
    FOREIGN KEY (rbo_checker_id) REFERENCES users(id)
);

CREATE TABLE survey_answers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    attempt_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    answer_value TEXT,
    file_path VARCHAR(500),
    branch_checker_status VARCHAR(20) DEFAULT 'PENDING',
    branch_checker_remarks TEXT,
    rbo_checker_status VARCHAR(20) DEFAULT 'PENDING',
    rbo_checker_remarks TEXT,
    is_locked BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (attempt_id) REFERENCES survey_attempts(id),
    FOREIGN KEY (question_id) REFERENCES questions(id),
    UNIQUE KEY uk_attempt_question (attempt_id, question_id)
);
```

### 4. Reversal & Exemption Tables

```sql
CREATE TABLE reversal_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    survey_attempt_id BIGINT NOT NULL,
    branch_code VARCHAR(10) NOT NULL,
    reason TEXT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING_CIRCLE_CHECKER',
    circle_checker_status VARCHAR(20) DEFAULT 'PENDING',
    circle_checker_remarks TEXT,
    circle_checker_acted_by BIGINT,
    circle_checker_acted_at TIMESTAMP,
    cc_maker_status VARCHAR(20) DEFAULT 'PENDING',
    cc_maker_remarks TEXT,
    cc_maker_acted_by BIGINT,
    cc_maker_acted_at TIMESTAMP,
    cc_checker_status VARCHAR(20) DEFAULT 'PENDING',
    cc_checker_remarks TEXT,
    cc_checker_acted_by BIGINT,
    cc_checker_acted_at TIMESTAMP,
    initiated_by BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (survey_attempt_id) REFERENCES survey_attempts(id),
    FOREIGN KEY (initiated_by) REFERENCES users(id)
);

CREATE TABLE exemption_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    survey_id BIGINT NOT NULL,
    branch_code VARCHAR(10) NOT NULL,
    branch_name VARCHAR(100),
    reason TEXT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING_CIRCLE_CHECKER',
    circle_checker_status VARCHAR(20) DEFAULT 'PENDING',
    circle_checker_remarks TEXT,
    circle_checker_acted_by BIGINT,
    circle_checker_acted_at TIMESTAMP,
    cc_maker_status VARCHAR(20) DEFAULT 'PENDING',
    cc_maker_remarks TEXT,
    cc_maker_acted_by BIGINT,
    cc_maker_acted_at TIMESTAMP,
    cc_checker_status VARCHAR(20) DEFAULT 'PENDING',
    cc_checker_remarks TEXT,
    cc_checker_acted_by BIGINT,
    cc_checker_acted_at TIMESTAMP,
    initiated_by BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (survey_id) REFERENCES surveys(id),
    FOREIGN KEY (initiated_by) REFERENCES users(id)
);
```

### 5. History / Audit & Draft Tables

```sql
CREATE TABLE audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_type VARCHAR(30) NOT NULL,
    reference_id BIGINT NOT NULL,
    status VARCHAR(40) NOT NULL,
    actor_pfid VARCHAR(20) NOT NULL,
    actor_name VARCHAR(100),
    actor_role VARCHAR(30),
    target_pfid VARCHAR(20),
    remarks TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_audit_actor ON audit_log(actor_pfid);
CREATE INDEX idx_audit_type ON audit_log(request_type);

CREATE TABLE survey_drafts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    survey_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    branch_code VARCHAR(10) NOT NULL,
    draft_data JSON NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (survey_id) REFERENCES surveys(id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    UNIQUE KEY uk_draft (survey_id, user_id, branch_code)
);
```

---

## 📁 PROJECT STRUCTURE

### Backend (Spring Boot)

```
branch-darpan-backend/
├── pom.xml
├── src/main/java/com/sbi/branchdarpan/
│   ├── BranchDarpanApplication.java
│   ├── config/
│   │   ├── SecurityConfig.java
│   │   ├── CorsConfig.java
│   │   ├── JwtConfig.java
│   │   └── SwaggerConfig.java
│   ├── security/
│   │   ├── JwtTokenProvider.java
│   │   ├── JwtAuthenticationFilter.java
│   │   ├── SsoAuthService.java
│   │   └── UserPrincipal.java
│   ├── controller/
│   │   ├── AuthController.java
│   │   ├── UserController.java
│   │   ├── SurveyController.java
│   │   ├── SurveyAttemptController.java
│   │   ├── ApprovalController.java
│   │   ├── ReversalController.java
│   │   ├── ExemptionController.java
│   │   ├── DashboardController.java
│   │   └── HistoryController.java
│   ├── service/
│   │   ├── AuthService.java
│   │   ├── UserService.java
│   │   ├── HrmsService.java
│   │   ├── SurveyService.java
│   │   ├── SurveyAttemptService.java
│   │   ├── ApprovalService.java
│   │   ├── ReversalService.java
│   │   ├── ExemptionService.java
│   │   ├── DraftService.java
│   │   ├── DashboardService.java
│   │   ├── HistoryService.java
│   │   └── AuditService.java
│   ├── repository/
│   │   ├── UserRepository.java
│   │   ├── UserRequestRepository.java
│   │   ├── SurveyRepository.java
│   │   ├── SectionRepository.java
│   │   ├── QuestionRepository.java
│   │   ├── SurveyAttemptRepository.java
│   │   ├── SurveyAnswerRepository.java
│   │   ├── ReversalRequestRepository.java
│   │   ├── ExemptionRequestRepository.java
│   │   ├── AuditLogRepository.java
│   │   └── SurveyDraftRepository.java
│   ├── model/
│   │   ├── entity/
│   │   ├── dto/
│   │   └── enums/
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java
│   │   ├── ResourceNotFoundException.java
│   │   └── UnauthorizedException.java
│   ├── scheduler/
│   │   └── SurveyCronJob.java
│   └── util/
│       └── RoleHierarchyUtil.java
├── src/main/resources/
│   ├── application.yml
│   └── application-dev.yml
```

### Frontend (Angular)

```
branch-darpan-frontend/
├── angular.json
├── package.json
├── src/
│   ├── index.html
│   ├── main.ts
│   ├── styles.scss
│   ├── app/
│   │   ├── app.component.ts
│   │   ├── app.config.ts
│   │   ├── app.routes.ts
│   │   ├── core/
│   │   │   ├── auth/
│   │   │   │   ├── auth.service.ts
│   │   │   │   ├── auth.store.ts
│   │   │   │   ├── auth.guard.ts
│   │   │   │   ├── role.guard.ts
│   │   │   │   └── auth.interceptor.ts
│   │   │   ├── services/
│   │   │   │   ├── api.service.ts
│   │   │   │   ├── hrms.service.ts
│   │   │   │   ├── survey.service.ts
│   │   │   │   ├── user.service.ts
│   │   │   │   ├── approval.service.ts
│   │   │   │   ├── reversal.service.ts
│   │   │   │   ├── exemption.service.ts
│   │   │   │   ├── history.service.ts
│   │   │   │   └── draft.service.ts
│   │   │   └── models/
│   │   │       ├── user.model.ts
│   │   │       ├── survey.model.ts
│   │   │       ├── question.model.ts
│   │   │       ├── approval.model.ts
│   │   │       └── history.model.ts
│   │   ├── shared/
│   │   │   ├── components/
│   │   │   │   ├── listing/
│   │   │   │   ├── questionnaire/
│   │   │   │   ├── approval-wrapper/
│   │   │   │   ├── user-form/
│   │   │   │   ├── topbar/
│   │   │   │   ├── header/
│   │   │   │   └── confirm-dialog/
│   │   │   ├── directives/
│   │   │   └── pipes/
│   │   ├── features/
│   │   │   ├── auth/
│   │   │   ├── dashboard/
│   │   │   ├── user-management/
│   │   │   ├── questionnaire-config/
│   │   │   ├── survey/
│   │   │   ├── survey-approval/
│   │   │   ├── reversal/
│   │   │   ├── exemption/
│   │   │   └── history/
│   │   └── layouts/
│   │       ├── shell/
│   │       └── public/
│   └── assets/
```

---

## 🚀 SETUP INSTRUCTIONS

### Prerequisites
- Node.js 20+, Angular CLI 21
- Java 21, Maven 3.9+
- MySQL 8.x

### Backend Setup

```bash
# 1. Create Spring Boot project via Spring Initializr
#    Dependencies: Spring Web, Spring Security, Spring Data JPA,
#    MySQL Connector, Lombok, Validation, Flyway

# 2. Create database
mysql -u root -p -e "CREATE DATABASE branch_darpan;"

# 3. Run all SQL from Section above to create tables

# 4. Configure application.yml (see Part 2)

# 5. Run
mvn spring-boot:run
```

### Frontend Setup

```bash
# 1. Create Angular project
ng new branch-darpan-frontend --standalone --routing --style=scss --skip-tests

# 2. Install dependencies
cd branch-darpan-frontend
ng add @angular/material
npm install @angular/cdk

# 3. Run
ng serve
```

---

> **Next:** See `02-BACKEND-API-AND-SERVICES.md` for full API contracts and Spring Boot implementation.
