package com.sbi.branchdarpan.scheduler;

import java.time.LocalDate;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.sbi.branchdarpan.service.SurveyService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SurveyCronJob {

    private final SurveyService surveyService;

    @Scheduled(cron = "0 0 0 1 * *")
    public void createMonthlySurvey() {
        surveyService.createMonthlySurvey(LocalDate.now());
    }
}
