package com.sbi.branchdarpan.controller;

import static com.sbi.branchdarpan.model.dto.survey.SurveyDtos.CreateAttemptRequest;
import static com.sbi.branchdarpan.model.dto.survey.SurveyDtos.DraftRequest;
import static com.sbi.branchdarpan.model.dto.survey.SurveyDtos.DraftResponse;
import static com.sbi.branchdarpan.model.dto.survey.SurveyDtos.SurveyAttemptResponse;
import static com.sbi.branchdarpan.model.dto.survey.SurveyDtos.UpdateAttemptRequest;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sbi.branchdarpan.security.UserPrincipal;
import com.sbi.branchdarpan.service.DraftService;
import com.sbi.branchdarpan.service.SurveyAttemptService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class SurveyAttemptController {

    private final SurveyAttemptService surveyAttemptService;
    private final DraftService draftService;

    @PostMapping("/surveys/attempt")
    public SurveyAttemptResponse createAttempt(
        @Valid @RequestBody CreateAttemptRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return surveyAttemptService.createAttempt(request, principal);
    }

    @PutMapping("/surveys/attempt/{id}")
    public SurveyAttemptResponse updateAttempt(
        @PathVariable Long id,
        @Valid @RequestBody UpdateAttemptRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return surveyAttemptService.updateAttempt(id, request, principal);
    }

    @GetMapping("/surveys/attempt/{id}")
    public SurveyAttemptResponse getAttempt(
        @PathVariable Long id,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return surveyAttemptService.getAttempt(id, principal);
    }

    @GetMapping("/surveys/attempts/my")
    public List<SurveyAttemptResponse> getMyAttempts(@AuthenticationPrincipal UserPrincipal principal) {
        return surveyAttemptService.getMyAttempts(principal);
    }

    @PutMapping("/surveys/draft")
    public DraftResponse saveDraft(
        @Valid @RequestBody DraftRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return draftService.saveDraft(request, principal);
    }

    @GetMapping("/surveys/draft/{surveyId}")
    public DraftResponse getDraft(@PathVariable Long surveyId, @AuthenticationPrincipal UserPrincipal principal) {
        return draftService.getDraft(surveyId, principal);
    }

    @DeleteMapping("/surveys/draft/{surveyId}")
    public void deleteDraft(@PathVariable Long surveyId, @AuthenticationPrincipal UserPrincipal principal) {
        draftService.deleteDraft(surveyId, principal);
    }
}
