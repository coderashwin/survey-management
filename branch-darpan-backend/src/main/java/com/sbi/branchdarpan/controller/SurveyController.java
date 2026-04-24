package com.sbi.branchdarpan.controller;

import static com.sbi.branchdarpan.model.dto.survey.SurveyDtos.SurveyEndDateRequest;
import static com.sbi.branchdarpan.model.dto.survey.SurveyDtos.SurveyResponse;
import static com.sbi.branchdarpan.model.dto.survey.SurveyDtos.SurveySummary;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sbi.branchdarpan.service.SurveyService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/surveys")
@RequiredArgsConstructor
public class SurveyController {

    private final SurveyService surveyService;

    @GetMapping
    public List<SurveySummary> listSurveys() {
        return surveyService.listSurveys();
    }

    @GetMapping("/{id}")
    public SurveyResponse getSurvey(@PathVariable Long id) {
        return surveyService.getSurvey(id);
    }

    @GetMapping("/active")
    public SurveyResponse getActiveSurvey() {
        return surveyService.getActiveSurvey();
    }

    @PutMapping("/{id}/end-date")
    public SurveyResponse updateEndDate(@PathVariable Long id, @Valid @RequestBody SurveyEndDateRequest request) {
        return surveyService.updateEndDate(id, request);
    }
}
