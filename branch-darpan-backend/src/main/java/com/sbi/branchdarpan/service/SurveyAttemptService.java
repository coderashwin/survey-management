package com.sbi.branchdarpan.service;

import static com.sbi.branchdarpan.model.dto.survey.SurveyDtos.AnswerInput;
import static com.sbi.branchdarpan.model.dto.survey.SurveyDtos.AnswerResponse;
import static com.sbi.branchdarpan.model.dto.survey.SurveyDtos.CreateAttemptRequest;
import static com.sbi.branchdarpan.model.dto.survey.SurveyDtos.SurveyAttemptResponse;
import static com.sbi.branchdarpan.model.dto.survey.SurveyDtos.UpdateAttemptRequest;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sbi.branchdarpan.exception.BadRequestException;
import com.sbi.branchdarpan.exception.ResourceNotFoundException;
import com.sbi.branchdarpan.model.entity.Question;
import com.sbi.branchdarpan.model.entity.Survey;
import com.sbi.branchdarpan.model.entity.SurveyAnswer;
import com.sbi.branchdarpan.model.entity.SurveyAttempt;
import com.sbi.branchdarpan.model.enums.AuditRequestType;
import com.sbi.branchdarpan.model.enums.SurveyAttemptStatus;
import com.sbi.branchdarpan.model.enums.WorkflowRequestStatus;
import com.sbi.branchdarpan.repository.ExemptionRequestRepository;
import com.sbi.branchdarpan.repository.QuestionRepository;
import com.sbi.branchdarpan.repository.SurveyAnswerRepository;
import com.sbi.branchdarpan.repository.SurveyAttemptRepository;
import com.sbi.branchdarpan.repository.SurveyRepository;
import com.sbi.branchdarpan.repository.UserRepository;
import com.sbi.branchdarpan.security.UserPrincipal;
import com.sbi.branchdarpan.util.RoleHierarchyUtil;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class SurveyAttemptService {

    private final SurveyRepository surveyRepository;
    private final SurveyAttemptRepository surveyAttemptRepository;
    private final SurveyAnswerRepository surveyAnswerRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final ExemptionRequestRepository exemptionRequestRepository;
    private final DraftService draftService;
    private final AuditService auditService;
    private final SurveyService surveyService;

    public SurveyAttemptResponse createAttempt(CreateAttemptRequest request, UserPrincipal principal) {
        surveyService.ensureSurveyExists();
        Survey survey = surveyRepository.findById(request.surveyId())
            .orElseThrow(() -> new ResourceNotFoundException("Survey not found"));

        if (!survey.isActive()) {
            throw new BadRequestException("Survey is not active");
        }

        if (exemptionRequestRepository.existsBySurveyIdAndBranchCodeAndStatus(
            survey.getId(),
            principal.getBranchCode(),
            WorkflowRequestStatus.APPROVED
        )) {
            throw new BadRequestException("Branch is exempted from this survey");
        }

        surveyAttemptRepository.findFirstBySurveyIdAndBranchCodeAndStatusIn(
            survey.getId(),
            principal.getBranchCode(),
            List.of(
                SurveyAttemptStatus.DRAFT,
                SurveyAttemptStatus.PENDING_BRANCH_CHECKER,
                SurveyAttemptStatus.PENDING_RBO_CHECKER
            )
        ).ifPresent(existing -> {
            throw new BadRequestException("An active attempt already exists for this branch");
        });

        SurveyAttempt attempt = SurveyAttempt.builder()
            .survey(survey)
            .branchCode(principal.getBranchCode())
            .branchName(principal.getBranchName())
            .submittedBy(userRepository.findById(principal.getId()).orElseThrow())
            .status(SurveyAttemptStatus.DRAFT)
            .build();

        SurveyAttempt saved = surveyAttemptRepository.save(attempt);
        auditService.log(AuditRequestType.SURVEY_ATTEMPT, saved.getId(), saved.getStatus().name(), principal, null, "Survey attempt created");
        return toResponse(saved);
    }

    public SurveyAttemptResponse updateAttempt(Long attemptId, UpdateAttemptRequest request, UserPrincipal principal) {
        SurveyAttempt attempt = surveyAttemptRepository.findById(attemptId)
            .orElseThrow(() -> new ResourceNotFoundException("Survey attempt not found"));
        validateAccess(attempt, principal);

        request.answers().forEach(answer -> upsertAnswer(attempt, answer));

        if ("SUBMIT".equalsIgnoreCase(request.action())) {
            attempt.setStatus(SurveyAttemptStatus.PENDING_BRANCH_CHECKER);
            attempt.setSubmittedAt(Instant.now());
            draftService.deleteDraft(attempt.getSurvey().getId(), principal);
            auditService.log(AuditRequestType.SURVEY_ATTEMPT, attempt.getId(), attempt.getStatus().name(), principal, null, "Survey submitted");
        } else {
            attempt.setStatus(SurveyAttemptStatus.DRAFT);
            auditService.log(AuditRequestType.SURVEY_DRAFT, attempt.getId(), attempt.getStatus().name(), principal, null, "Survey draft updated");
        }

        return toResponse(attempt);
    }

    @Transactional(readOnly = true)
    public SurveyAttemptResponse getAttempt(Long attemptId, UserPrincipal principal) {
        SurveyAttempt attempt = surveyAttemptRepository.findById(attemptId)
            .orElseThrow(() -> new ResourceNotFoundException("Survey attempt not found"));
        validateAccess(attempt, principal);
        return toResponse(attempt);
    }

    @Transactional(readOnly = true)
    public List<SurveyAttemptResponse> getMyAttempts(UserPrincipal principal) {
        return surveyAttemptRepository.findBySubmittedByIdOrderByUpdatedAtDesc(principal.getId())
            .stream()
            .map(this::toResponse)
            .toList();
    }

    public SurveyAttemptResponse toResponse(SurveyAttempt attempt) {
        return new SurveyAttemptResponse(
            attempt.getId(),
            attempt.getSurvey().getId(),
            attempt.getSurvey().getTitle(),
            attempt.getBranchCode(),
            attempt.getBranchName(),
            attempt.getStatus().name(),
            attempt.getAttemptNumber(),
            attempt.getSubmittedAt() == null ? null : attempt.getSubmittedAt().toString(),
            surveyAnswerRepository.findByAttemptId(attempt.getId()).stream()
                .map(answer -> new AnswerResponse(
                    answer.getQuestion().getId(),
                    answer.getAnswerValue(),
                    answer.getFilePath(),
                    answer.getBranchCheckerStatus().name(),
                    answer.getBranchCheckerRemarks(),
                    answer.getRboCheckerStatus().name(),
                    answer.getRboCheckerRemarks(),
                    answer.isLocked()
                ))
                .toList()
        );
    }

    private void upsertAnswer(SurveyAttempt attempt, AnswerInput input) {
        Question question = questionRepository.findById(input.questionId())
            .orElseThrow(() -> new ResourceNotFoundException("Question not found"));
        SurveyAnswer answer = surveyAnswerRepository.findByAttemptIdAndQuestionId(attempt.getId(), input.questionId())
            .orElseGet(() -> SurveyAnswer.builder()
                .attempt(attempt)
                .question(question)
                .build());
        answer.setAnswerValue(input.answerValue());
        answer.setFilePath(input.filePath());
        surveyAnswerRepository.save(answer);
    }

    private void validateAccess(SurveyAttempt attempt, UserPrincipal principal) {
        if (!RoleHierarchyUtil.canAccessBranch(principal, attempt.getBranchCode())) {
            throw new BadRequestException("You do not have access to this branch attempt");
        }
    }
}
