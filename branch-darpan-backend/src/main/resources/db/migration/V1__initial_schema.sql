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
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_users_created_by FOREIGN KEY (created_by) REFERENCES users(id)
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
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_requests_requested_by FOREIGN KEY (requested_by) REFERENCES users(id),
    CONSTRAINT fk_user_requests_approved_by FOREIGN KEY (approved_by) REFERENCES users(id)
);

CREATE TABLE surveys (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    frequency VARCHAR(20) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE sections (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    survey_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    display_order INT NOT NULL,
    is_mutually_exclusive_with BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sections_survey FOREIGN KEY (survey_id) REFERENCES surveys(id),
    CONSTRAINT fk_sections_mutual FOREIGN KEY (is_mutually_exclusive_with) REFERENCES sections(id)
);

CREATE TABLE subsections (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    section_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    display_order INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_subsections_section FOREIGN KEY (section_id) REFERENCES sections(id)
);

CREATE TABLE questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    subsection_id BIGINT NOT NULL,
    question_text TEXT NOT NULL,
    option_type VARCHAR(20) NOT NULL,
    weightage DECIMAL(5,2) NOT NULL DEFAULT 0,
    frequency VARCHAR(20) NOT NULL,
    display_order INT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    depends_on_question_id BIGINT,
    depends_on_answer VARCHAR(200),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_questions_subsection FOREIGN KEY (subsection_id) REFERENCES subsections(id),
    CONSTRAINT fk_questions_dependency FOREIGN KEY (depends_on_question_id) REFERENCES questions(id)
);

CREATE TABLE question_options (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    question_id BIGINT NOT NULL,
    option_text VARCHAR(200) NOT NULL,
    option_value VARCHAR(100) NOT NULL,
    display_order INT NOT NULL,
    CONSTRAINT fk_question_options_question FOREIGN KEY (question_id) REFERENCES questions(id)
);

CREATE TABLE survey_attempts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    survey_id BIGINT NOT NULL,
    branch_code VARCHAR(10) NOT NULL,
    branch_name VARCHAR(100),
    status VARCHAR(40) NOT NULL DEFAULT 'DRAFT',
    attempt_number INT NOT NULL DEFAULT 1,
    submitted_by BIGINT,
    branch_checker_id BIGINT,
    rbo_checker_id BIGINT,
    submitted_at TIMESTAMP NULL,
    branch_checker_acted_at TIMESTAMP NULL,
    rbo_checker_acted_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_survey_attempts_survey FOREIGN KEY (survey_id) REFERENCES surveys(id),
    CONSTRAINT fk_survey_attempts_submitted_by FOREIGN KEY (submitted_by) REFERENCES users(id),
    CONSTRAINT fk_survey_attempts_branch_checker FOREIGN KEY (branch_checker_id) REFERENCES users(id),
    CONSTRAINT fk_survey_attempts_rbo_checker FOREIGN KEY (rbo_checker_id) REFERENCES users(id)
);

CREATE TABLE survey_answers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    attempt_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    answer_value TEXT,
    file_path VARCHAR(500),
    branch_checker_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    branch_checker_remarks TEXT,
    rbo_checker_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    rbo_checker_remarks TEXT,
    is_locked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_survey_answers_attempt FOREIGN KEY (attempt_id) REFERENCES survey_attempts(id),
    CONSTRAINT fk_survey_answers_question FOREIGN KEY (question_id) REFERENCES questions(id),
    CONSTRAINT uk_attempt_question UNIQUE (attempt_id, question_id)
);

CREATE TABLE survey_drafts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    survey_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    branch_code VARCHAR(10) NOT NULL,
    draft_data JSON NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_survey_drafts_survey FOREIGN KEY (survey_id) REFERENCES surveys(id),
    CONSTRAINT fk_survey_drafts_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT uk_draft UNIQUE (survey_id, user_id, branch_code)
);

CREATE TABLE reversal_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    survey_attempt_id BIGINT NOT NULL,
    branch_code VARCHAR(10) NOT NULL,
    reason TEXT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING_CIRCLE_CHECKER',
    circle_checker_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    circle_checker_remarks TEXT,
    circle_checker_acted_by BIGINT,
    circle_checker_acted_at TIMESTAMP NULL,
    cc_maker_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    cc_maker_remarks TEXT,
    cc_maker_acted_by BIGINT,
    cc_maker_acted_at TIMESTAMP NULL,
    cc_checker_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    cc_checker_remarks TEXT,
    cc_checker_acted_by BIGINT,
    cc_checker_acted_at TIMESTAMP NULL,
    initiated_by BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_reversal_attempt FOREIGN KEY (survey_attempt_id) REFERENCES survey_attempts(id),
    CONSTRAINT fk_reversal_circle_checker FOREIGN KEY (circle_checker_acted_by) REFERENCES users(id),
    CONSTRAINT fk_reversal_cc_maker FOREIGN KEY (cc_maker_acted_by) REFERENCES users(id),
    CONSTRAINT fk_reversal_cc_checker FOREIGN KEY (cc_checker_acted_by) REFERENCES users(id),
    CONSTRAINT fk_reversal_initiated_by FOREIGN KEY (initiated_by) REFERENCES users(id)
);

CREATE TABLE exemption_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    survey_id BIGINT NOT NULL,
    branch_code VARCHAR(10) NOT NULL,
    branch_name VARCHAR(100),
    reason TEXT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING_CIRCLE_CHECKER',
    circle_checker_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    circle_checker_remarks TEXT,
    circle_checker_acted_by BIGINT,
    circle_checker_acted_at TIMESTAMP NULL,
    cc_maker_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    cc_maker_remarks TEXT,
    cc_maker_acted_by BIGINT,
    cc_maker_acted_at TIMESTAMP NULL,
    cc_checker_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    cc_checker_remarks TEXT,
    cc_checker_acted_by BIGINT,
    cc_checker_acted_at TIMESTAMP NULL,
    initiated_by BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_exemption_survey FOREIGN KEY (survey_id) REFERENCES surveys(id),
    CONSTRAINT fk_exemption_circle_checker FOREIGN KEY (circle_checker_acted_by) REFERENCES users(id),
    CONSTRAINT fk_exemption_cc_maker FOREIGN KEY (cc_maker_acted_by) REFERENCES users(id),
    CONSTRAINT fk_exemption_cc_checker FOREIGN KEY (cc_checker_acted_by) REFERENCES users(id),
    CONSTRAINT fk_exemption_initiated_by FOREIGN KEY (initiated_by) REFERENCES users(id)
);

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
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_actor ON audit_log(actor_pfid);
CREATE INDEX idx_audit_type ON audit_log(request_type);
