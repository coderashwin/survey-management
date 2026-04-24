package com.sbi.branchdarpan.service;

import static com.sbi.branchdarpan.model.dto.survey.SurveyDtos.DraftRequest;
import static com.sbi.branchdarpan.model.dto.survey.SurveyDtos.DraftResponse;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sbi.branchdarpan.exception.ResourceNotFoundException;
import com.sbi.branchdarpan.model.entity.SurveyDraft;
import com.sbi.branchdarpan.repository.SurveyDraftRepository;
import com.sbi.branchdarpan.repository.SurveyRepository;
import com.sbi.branchdarpan.repository.UserRepository;
import com.sbi.branchdarpan.security.UserPrincipal;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class DraftService {

    private final SurveyDraftRepository surveyDraftRepository;
    private final SurveyRepository surveyRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public DraftResponse saveDraft(DraftRequest request, UserPrincipal principal) {
        SurveyDraft draft = surveyDraftRepository
            .findBySurveyIdAndUserIdAndBranchCode(request.surveyId(), principal.getId(), principal.getBranchCode())
            .orElseGet(SurveyDraft::new);

        draft.setSurvey(surveyRepository.findById(request.surveyId()).orElseThrow(() -> new ResourceNotFoundException("Survey not found")));
        draft.setUser(userRepository.findById(principal.getId()).orElseThrow(() -> new ResourceNotFoundException("User not found")));
        draft.setBranchCode(principal.getBranchCode());
        draft.setDraftData(writeJson(request.draftData()));

        SurveyDraft saved = surveyDraftRepository.save(draft);
        return new DraftResponse(saved.getSurvey().getId(), request.draftData());
    }

    @Transactional(readOnly = true)
    public DraftResponse getDraft(Long surveyId, UserPrincipal principal) {
        SurveyDraft draft = surveyDraftRepository
            .findBySurveyIdAndUserIdAndBranchCode(surveyId, principal.getId(), principal.getBranchCode())
            .orElseThrow(() -> new ResourceNotFoundException("Draft not found"));
        return new DraftResponse(surveyId, readJson(draft.getDraftData()));
    }

    public void deleteDraft(Long surveyId, UserPrincipal principal) {
        surveyDraftRepository.deleteBySurveyIdAndUserIdAndBranchCode(surveyId, principal.getId(), principal.getBranchCode());
    }

    private String writeJson(Map<String, Object> draftData) {
        try {
            return objectMapper.writeValueAsString(draftData);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to store draft data", exception);
        }
    }

    private Map<String, Object> readJson(String draftData) {
        try {
            return objectMapper.readValue(draftData, new TypeReference<>() {
            });
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to read draft data", exception);
        }
    }
}
