# Branch Darpan — Part 2: Backend API & Services

## 📋 application.yml Configuration

```yaml
server:
  port: 8080
  servlet:
    context-path: /api/v1

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/branch_darpan?useSSL=false&serverTimezone=Asia/Kolkata
    username: root
    password: ${DB_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect
        format_sql: true
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB

jwt:
  secret: ${JWT_SECRET}
  expiration: 86400000  # 24 hours

sso:
  mode: DIRECT_API  # or EXTERNAL_REDIRECT
  login-url: ${SSO_LOGIN_URL}
  callback-url: ${SSO_CALLBACK_URL}

hrms:
  base-url: ${HRMS_API_URL}
  api-key: ${HRMS_API_KEY}

cors:
  allowed-origins: http://localhost:4200
```

---

## 🔐 AUTHENTICATION & SECURITY

### SecurityConfig.java

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           JwtAuthenticationFilter jwtFilter) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults())
            .sessionManagement(s -> s.sessionCreationPolicy(STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**", "/public/**").permitAll()
                .requestMatchers("/users/**").hasAnyRole(
                    "SUPER_ADMIN","CC_MAKER","CC_CHECKER","CIRCLE_MAKER","CIRCLE_CHECKER",
                    "AO_MAKER","AO_CHECKER","RBO_MAKER","RBO_CHECKER","BRANCH_CHECKER")
                .requestMatchers("/surveys/config/**").hasAnyRole("CC_MAKER","CC_CHECKER")
                .requestMatchers("/surveys/attempt/**").hasAnyRole("BRANCH_MAKER")
                .requestMatchers("/surveys/approval/**").hasAnyRole("BRANCH_CHECKER","RBO_CHECKER")
                .requestMatchers("/reversals/**").hasAnyRole(
                    "CIRCLE_MAKER","CIRCLE_CHECKER","CC_MAKER","CC_CHECKER")
                .requestMatchers("/exemptions/**").hasAnyRole(
                    "CIRCLE_MAKER","CIRCLE_CHECKER","CC_MAKER","CC_CHECKER")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
```

### JwtTokenProvider.java — Key Methods

```java
@Component
public class JwtTokenProvider {
    String generateToken(UserPrincipal user);       // Create JWT with role, pfid, name
    boolean validateToken(String token);             // Validate signature + expiry
    UserPrincipal getUserFromToken(String token);    // Extract user details
}
```

### JwtAuthenticationFilter.java

```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    // Extract JWT from Authorization header
    // Validate → set SecurityContext
    // Invalid → return 401
}
```

---

## 📡 COMPLETE REST API CONTRACTS

### 1. Auth APIs

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/auth/sso/login` | Direct SSO login, returns JWT | Public |
| GET | `/auth/sso/callback` | SSO callback (external redirect flow) | Public |
| POST | `/auth/validate` | Validate JWT, return user profile | Authenticated |
| POST | `/auth/logout` | Invalidate token | Authenticated |

**POST `/auth/sso/login`**
```json
// Request
{ "ssoToken": "string" }

// Response 200
{
  "jwt": "eyJhbG...",
  "user": {
    "id": 1,
    "pfid": "12345678",
    "name": "Rajesh Kumar",
    "role": "BRANCH_MAKER",
    "circleCode": "C01",
    "circleName": "Delhi Circle",
    "branchCode": "B001",
    "branchName": "Connaught Place Branch"
  }
}
```

**POST `/auth/validate`**
```json
// Response 200 — same user object as login
// Response 401 — { "error": "Token expired or invalid" }
```

---

### 2. User Management APIs

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/hrms/{pfid}` | Fetch HRMS data by PFID | Makers |
| POST | `/users/request` | Submit user creation request | Makers |
| GET | `/users/requests` | List pending requests for checker | Checkers |
| PUT | `/users/requests/{id}/approve` | Approve user request | Checkers |
| PUT | `/users/requests/{id}/reject` | Reject user request | Checkers |
| GET | `/users` | List users (filtered by role hierarchy) | All |
| GET | `/users/{id}` | Get user details | All |

**GET `/hrms/{pfid}`**
```json
// Response 200
{
  "pfid": "12345678",
  "name": "Suresh Sharma",
  "email": "suresh@sbi.co.in",
  "mobile": "9876543210",
  "designation": "Manager",
  "circleCode": "C01",
  "circleName": "Delhi Circle",
  "aoCode": "A01",
  "aoName": "Administrative Office 1",
  "rboCode": "R01",
  "rboName": "Regional Business Office 1",
  "branchCode": "B001",
  "branchName": "Connaught Place Branch"
}
```

**POST `/users/request`**
```json
// Request
{
  "pfid": "12345678",
  "requestedRole": "CIRCLE_MAKER",
  "name": "Suresh Sharma",
  "email": "suresh@sbi.co.in",
  "mobile": "9876543210",
  "designation": "Manager",
  "circleCode": "C01",
  "circleName": "Delhi Circle"
}

// Response 201
{ "id": 45, "status": "PENDING", "message": "User creation request submitted" }
```

**PUT `/users/requests/{id}/reject`**
```json
// Request
{ "remarks": "Invalid designation for this role" }
```

---

### 3. Survey Configuration APIs (CC Maker only)

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/surveys` | List all surveys | CC Maker/Checker |
| GET | `/surveys/{id}` | Get survey with sections/questions | All |
| PUT | `/surveys/{id}/end-date` | Update end date only | CC Maker |
| GET | `/surveys/active` | Get current active survey | All |

**GET `/surveys/{id}`** — Full survey structure
```json
{
  "id": 1,
  "title": "Branch Darpan Survey - April 2026",
  "frequency": "MONTHLY",
  "startDate": "2026-04-01",
  "endDate": "2026-04-30",
  "isActive": true,
  "sections": [
    {
      "id": 1,
      "name": "Infrastructure",
      "displayOrder": 1,
      "isMutuallyExclusiveWith": null,
      "subsections": [
        {
          "id": 1,
          "name": "Building Condition",
          "displayOrder": 1,
          "questions": [
            {
              "id": 1,
              "questionText": "Is the branch building in good condition?",
              "optionType": "RADIO",
              "weightage": 5.00,
              "frequency": "MONTHLY",
              "displayOrder": 1,
              "dependsOnQuestionId": null,
              "dependsOnAnswer": null,
              "options": [
                { "id": 1, "optionText": "Yes", "optionValue": "yes", "displayOrder": 1 },
                { "id": 2, "optionText": "No", "optionValue": "no", "displayOrder": 2 }
              ]
            }
          ]
        }
      ]
    }
  ]
}
```

---

### 4. Survey Attempt APIs (Branch Maker)

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/surveys/attempt` | Create new attempt | Branch Maker |
| PUT | `/surveys/attempt/{id}` | Update answers (draft/submit) | Branch Maker |
| GET | `/surveys/attempt/{id}` | Get attempt with answers | All |
| GET | `/surveys/attempts/my` | List my attempts | Branch Maker |

**POST `/surveys/attempt`**
```json
// Request
{ "surveyId": 1 }

// Response 201
{ "id": 100, "surveyId": 1, "status": "DRAFT", "attemptNumber": 1 }
```

**PUT `/surveys/attempt/{id}`**
```json
// Request
{
  "action": "SUBMIT",  // or "SAVE_DRAFT"
  "answers": [
    { "questionId": 1, "answerValue": "yes" },
    { "questionId": 2, "answerValue": "2026-03" },
    { "questionId": 3, "filePath": "/uploads/branch_photo.jpg" }
  ]
}

// Response 200
{ "id": 100, "status": "PENDING_BRANCH_CHECKER", "message": "Survey submitted" }
```

---

### 5. Survey Draft APIs

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| PUT | `/surveys/draft` | Save/update draft | Branch Maker |
| GET | `/surveys/draft/{surveyId}` | Get saved draft | Branch Maker |
| DELETE | `/surveys/draft/{surveyId}` | Delete draft (on submit) | Branch Maker |

**PUT `/surveys/draft`**
```json
{
  "surveyId": 1,
  "draftData": {
    "answers": { "q1": "yes", "q2": "2026-03" },
    "selectedSection": "Infrastructure",
    "lastSavedAt": "2026-04-15T10:30:00"
  }
}
```

---

### 6. Survey Approval APIs

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/surveys/approval/pending` | List pending surveys for approval | Branch/RBO Checker |
| GET | `/surveys/approval/{attemptId}` | Get attempt for review | Checkers |
| PUT | `/surveys/approval/{attemptId}/question/{qId}` | Approve/reject single question | Checkers |
| PUT | `/surveys/approval/{attemptId}/submit` | Submit all decisions | Checkers |

**PUT `/surveys/approval/{attemptId}/question/{qId}`**
```json
// Request
{
  "status": "REJECTED",  // or "APPROVED"
  "remarks": "Photo is not clear, please retake"  // mandatory if REJECTED
}
```

**PUT `/surveys/approval/{attemptId}/submit`**
```json
// Response 200 (Branch Checker — all approved)
{ "message": "Survey forwarded to RBO Checker", "newStatus": "PENDING_RBO_CHECKER" }

// Response 200 (Branch Checker — any rejected)
{ "message": "Survey rejected, sent back to Branch Maker", "newStatus": "REJECTED_BY_BRANCH_CHECKER" }

// Response 200 (RBO Checker — all approved)
{ "message": "Survey fully approved", "newStatus": "APPROVED" }
```

---

### 7. Reversal APIs

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/reversals` | Initiate reversal | Circle Maker |
| GET | `/reversals` | List reversals | Circle/CC roles |
| GET | `/reversals/{id}` | Get reversal detail | Circle/CC roles |
| PUT | `/reversals/{id}/approve` | Approve at current level | Checkers |
| PUT | `/reversals/{id}/reject` | Reject at current level | Checkers |

**POST `/reversals`**
```json
{ "surveyAttemptId": 100, "branchCode": "B001", "reason": "Data entry errors found" }
```

**PUT `/reversals/{id}/approve`**
```json
{ "remarks": "Verified, proceeding with reversal" }
// Backend auto-advances to next approver in chain
// Circle Checker → CC Maker → CC Checker
```

---

### 8. Exemption APIs

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/exemptions` | Initiate exemption | Circle Maker |
| GET | `/exemptions` | List exemptions | Circle/CC roles |
| GET | `/exemptions/{id}` | Get exemption detail | Circle/CC roles |
| PUT | `/exemptions/{id}/approve` | Approve at current level | Checkers |
| PUT | `/exemptions/{id}/reject` | Reject at current level | Checkers |

**POST `/exemptions`**
```json
{ "surveyId": 1, "branchCode": "B001", "branchName": "Connaught Place", "reason": "Branch under renovation" }
```

---

### 9. History APIs

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/history` | List all history for current user's role | All |
| GET | `/history/{id}` | Get history entry detail | All |

**GET `/history`**
```json
// Query params: ?page=0&size=20&requestType=SURVEY_UPDATE&status=PENDING

// Response 200
{
  "content": [
    {
      "id": 1,
      "requestType": "SURVEY_UPDATE",
      "referenceId": 100,
      "status": "PENDING_AT_BRANCH_CHECKER",
      "actorPfid": "12345678",
      "actorName": "Rajesh Kumar",
      "date": "2026-04-15T10:30:00",
      "details": "Survey attempt #1 for April 2026"
    }
  ],
  "totalElements": 45,
  "totalPages": 3,
  "currentPage": 0
}
```

---

### 10. File Upload API

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/files/upload` | Upload file (multipart) | Branch Maker |
| GET | `/files/{filename}` | Download/view file | All |

---

## 🏗️ SERVICE LAYER — KEY BUSINESS LOGIC

### UserService.java — Key Methods

```java
@Service
@Transactional
public class UserService {

    // Determines which roles this maker can create
    public List<Role> getAllowedRolesForCreation(Role makerRole) {
        return switch (makerRole) {
            case SUPER_ADMIN -> List.of(CC_MAKER, CC_CHECKER);
            case CC_MAKER -> List.of(CIRCLE_MAKER, CIRCLE_CHECKER);
            case CIRCLE_MAKER -> List.of(AO_MAKER, AO_CHECKER);
            case AO_MAKER -> List.of(RBO_MAKER, RBO_CHECKER);
            case RBO_MAKER -> List.of(BRANCH_MAKER, BRANCH_CHECKER);
            default -> List.of();
        };
    }

    // Determines which checker role approves this request
    public Role getCheckerRole(Role makerRole) {
        return switch (makerRole) {
            case SUPER_ADMIN -> null; // Super Admin self-approves
            case CC_MAKER -> CC_CHECKER;
            case CIRCLE_MAKER -> CIRCLE_CHECKER;
            case AO_MAKER -> AO_CHECKER;
            case RBO_MAKER -> RBO_CHECKER;
            default -> null;
        };
    }

    public UserRequest submitRequest(UserRequestDto dto, UserPrincipal maker);
    public void approveRequest(Long requestId, UserPrincipal checker);
    public void rejectRequest(Long requestId, String remarks, UserPrincipal checker);
}
```

### SurveyAttemptService.java — Core Logic

```java
@Service
@Transactional
public class SurveyAttemptService {

    public SurveyAttempt createAttempt(Long surveyId, UserPrincipal branchMaker) {
        // 1. Verify survey is active
        // 2. Check no existing DRAFT/PENDING attempt for this branch
        // 3. Check branch is not exempted
        // 4. Create attempt with status DRAFT
    }

    public SurveyAttempt submitAttempt(Long attemptId, List<AnswerDto> answers) {
        // 1. Save all answers
        // 2. Change status to PENDING_BRANCH_CHECKER
        // 3. Delete any saved draft
        // 4. Log to audit_log
    }

    public void saveDraft(Long surveyId, DraftData draftData, UserPrincipal user) {
        // Upsert into survey_drafts table
    }
}
```

### ApprovalService.java — Per-Question Logic

```java
@Service
@Transactional
public class ApprovalService {

    public void approveQuestion(Long attemptId, Long questionId,
                                String status, String remarks, UserPrincipal checker) {
        SurveyAnswer answer = answerRepo.findByAttemptAndQuestion(attemptId, questionId);

        if (checker.getRole() == BRANCH_CHECKER) {
            answer.setBranchCheckerStatus(status);
            answer.setBranchCheckerRemarks(remarks);
        } else if (checker.getRole() == RBO_CHECKER) {
            answer.setRboCheckerStatus(status);
            answer.setRboCheckerRemarks(remarks);
            if ("APPROVED".equals(status)) {
                answer.setIsLocked(true);  // Lock RBO-approved questions
            }
        }
        answerRepo.save(answer);
    }

    public void submitAllDecisions(Long attemptId, UserPrincipal checker) {
        List<SurveyAnswer> answers = answerRepo.findByAttemptId(attemptId);

        if (checker.getRole() == BRANCH_CHECKER) {
            boolean anyRejected = answers.stream()
                .anyMatch(a -> "REJECTED".equals(a.getBranchCheckerStatus()));

            if (anyRejected) {
                attempt.setStatus("REJECTED_BY_BRANCH_CHECKER");
            } else {
                attempt.setStatus("PENDING_RBO_CHECKER");
                // Auto-forward to RBO Checker
            }
        } else if (checker.getRole() == RBO_CHECKER) {
            boolean anyRejected = answers.stream()
                .anyMatch(a -> "REJECTED".equals(a.getRboCheckerStatus()));

            if (anyRejected) {
                attempt.setStatus("REJECTED_BY_RBO_CHECKER");
                // Branch Maker reattempts only rejected questions
            } else {
                attempt.setStatus("APPROVED");
            }
        }
        attemptRepo.save(attempt);
        auditService.log(...);
    }
}
```

### ReversalService.java — Multi-Level Chain

```java
@Service
@Transactional
public class ReversalService {

    public void approve(Long reversalId, String remarks, UserPrincipal approver) {
        ReversalRequest req = reversalRepo.findById(reversalId).orElseThrow();

        switch (approver.getRole()) {
            case CIRCLE_CHECKER -> {
                req.setCircleCheckerStatus("APPROVED");
                req.setStatus("PENDING_CC_MAKER");
            }
            case CC_MAKER -> {
                req.setCcMakerStatus("APPROVED");
                req.setStatus("PENDING_CC_CHECKER");
            }
            case CC_CHECKER -> {
                req.setCcCheckerStatus("APPROVED");
                req.setStatus("APPROVED");
                // Reset the survey attempt to initial state
                resetSurveyAttempt(req.getSurveyAttemptId());
            }
        }
        reversalRepo.save(req);
    }

    private void resetSurveyAttempt(Long attemptId) {
        // Delete all answers for this attempt
        // Reset attempt status to allow Branch Maker to re-fill
        answerRepo.deleteByAttemptId(attemptId);
        SurveyAttempt attempt = attemptRepo.findById(attemptId).orElseThrow();
        attempt.setStatus("DRAFT");
        attempt.setAttemptNumber(attempt.getAttemptNumber() + 1);
        attemptRepo.save(attempt);
    }
}
```

### SurveyCronJob.java — Survey Creation

```java
@Component
public class SurveyCronJob {

    @Scheduled(cron = "0 0 0 1 * *")  // 1st of every month
    public void createMonthlySurvey() {
        // 1. Deactivate current active survey
        // 2. Create new survey with pre-seeded sections/questions
        // 3. Set start_date = 1st, end_date = last day of month
        // 4. Copy question templates from master question bank
    }

    @Scheduled(cron = "0 0 0 1 1,4,7,10 *")  // Quarterly
    public void createQuarterlySurvey() {
        // Same but for quarterly frequency questions
    }
}
```

---

## 🔒 ROLE HIERARCHY UTILITY

```java
public class RoleHierarchyUtil {

    // Map: Maker → who they create | who approves
    private static final Map<Role, RoleMapping> HIERARCHY = Map.of(
        SUPER_ADMIN,   new RoleMapping(List.of(CC_MAKER, CC_CHECKER), null),
        CC_MAKER,      new RoleMapping(List.of(CIRCLE_MAKER, CIRCLE_CHECKER), CC_CHECKER),
        CIRCLE_MAKER,  new RoleMapping(List.of(AO_MAKER, AO_CHECKER), CIRCLE_CHECKER),
        AO_MAKER,      new RoleMapping(List.of(RBO_MAKER, RBO_CHECKER), AO_CHECKER),
        RBO_MAKER,     new RoleMapping(List.of(BRANCH_MAKER, BRANCH_CHECKER), RBO_CHECKER)
    );

    // Check if user can see data for a given jurisdiction
    public static boolean canAccessBranch(User user, String branchCode) {
        return switch (user.getRole()) {
            case SUPER_ADMIN, CC_MAKER, CC_CHECKER -> true;
            case CIRCLE_MAKER, CIRCLE_CHECKER ->
                branchBelongsToCircle(branchCode, user.getCircleCode());
            case AO_MAKER, AO_CHECKER ->
                branchBelongsToAO(branchCode, user.getAoCode());
            case RBO_MAKER, RBO_CHECKER ->
                branchBelongsToRBO(branchCode, user.getRboCode());
            case BRANCH_MAKER, BRANCH_CHECKER ->
                user.getBranchCode().equals(branchCode);
        };
    }
}
```

---

## ⚠️ GLOBAL ERROR HANDLING

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(404)
            .body(new ErrorResponse("NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException ex) {
        return ResponseEntity.status(403)
            .body(new ErrorResponse("FORBIDDEN", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .collect(Collectors.joining(", "));
        return ResponseEntity.status(400)
            .body(new ErrorResponse("VALIDATION_ERROR", errors));
    }
}
```

---

> **Next:** See `03-ANGULAR-FRONTEND-IMPLEMENTATION.md` for complete frontend implementation.
