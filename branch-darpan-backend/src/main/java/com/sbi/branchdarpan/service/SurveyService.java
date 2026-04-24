package com.sbi.branchdarpan.service;

import static com.sbi.branchdarpan.model.dto.survey.SurveyDtos.QuestionOptionResponse;
import static com.sbi.branchdarpan.model.dto.survey.SurveyDtos.QuestionResponse;
import static com.sbi.branchdarpan.model.dto.survey.SurveyDtos.SectionResponse;
import static com.sbi.branchdarpan.model.dto.survey.SurveyDtos.SubsectionResponse;
import static com.sbi.branchdarpan.model.dto.survey.SurveyDtos.SurveyEndDateRequest;
import static com.sbi.branchdarpan.model.dto.survey.SurveyDtos.SurveyResponse;
import static com.sbi.branchdarpan.model.dto.survey.SurveyDtos.SurveySummary;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sbi.branchdarpan.exception.ResourceNotFoundException;
import com.sbi.branchdarpan.model.entity.Question;
import com.sbi.branchdarpan.model.entity.QuestionOption;
import com.sbi.branchdarpan.model.entity.Section;
import com.sbi.branchdarpan.model.entity.Subsection;
import com.sbi.branchdarpan.model.entity.Survey;
import com.sbi.branchdarpan.model.enums.QuestionOptionType;
import com.sbi.branchdarpan.model.enums.SurveyFrequency;
import com.sbi.branchdarpan.repository.SurveyRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class SurveyService {

    private final SurveyRepository surveyRepository;

    public List<SurveySummary> listSurveys() {
        ensureSurveyExists();
        return surveyRepository.findAllByOrderByStartDateDesc().stream().map(this::toSummary).toList();
    }

    public SurveyResponse getSurvey(Long id) {
        ensureSurveyExists();
        return toResponse(surveyRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Survey not found")));
    }

    public SurveyResponse getActiveSurvey() {
        ensureSurveyExists();
        Survey survey = surveyRepository.findByActiveTrue().orElseThrow(() -> new ResourceNotFoundException("Active survey not found"));
        return toResponse(survey);
    }

    public SurveyResponse updateEndDate(Long id, SurveyEndDateRequest request) {
        Survey survey = surveyRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Survey not found"));
        survey.setEndDate(request.endDate());
        return toResponse(survey);
    }

    public void ensureSurveyExists() {
        if (surveyRepository.count() == 0) {
            createMonthlySurvey(LocalDate.now());
        }
    }

    public Survey createMonthlySurvey(LocalDate today) {
        surveyRepository.findByActiveTrue().ifPresent(existing -> existing.setActive(false));
        YearMonth yearMonth = YearMonth.from(today);
        Survey survey = Survey.builder()
            .title("Branch Darpan Survey - " + yearMonth.getMonth() + " " + yearMonth.getYear())
            .frequency(SurveyFrequency.MONTHLY)
            .startDate(yearMonth.atDay(1))
            .endDate(yearMonth.atEndOfMonth())
            .active(true)
            .build();

        Section infrastructure = section(survey, "Infrastructure", 1, null);
        Subsection building = subsection(infrastructure, "Building Condition", 1);
        Question q1 = question(building, "Is the branch building in good condition?", QuestionOptionType.RADIO, 1, null, null);
        q1.getOptions().add(option(q1, "Yes", "yes", 1));
        q1.getOptions().add(option(q1, "No", "no", 2));
        building.getQuestions().add(q1);
        infrastructure.getSubsections().add(building);

        Section anytimeChannels = section(survey, "Anytime Channels", 2, null);
        Section ecorner = section(survey, "eCorner", 3, anytimeChannels);
        anytimeChannels.setMutuallyExclusiveWith(ecorner);

        Subsection atcOps = subsection(anytimeChannels, "ATM Operations", 1);
        Question q2 = question(atcOps, "Upload latest ATM maintenance proof", QuestionOptionType.FILE_UPLOAD, 1, null, null);
        atcOps.getQuestions().add(q2);
        anytimeChannels.getSubsections().add(atcOps);

        Subsection ecornerOps = subsection(ecorner, "eCorner Adoption", 1);
        Question q3 = question(ecornerOps, "Enter the month of last eCorner audit", QuestionOptionType.MONTH_PICKER, 1, null, null);
        ecornerOps.getQuestions().add(q3);
        ecorner.getSubsections().add(ecornerOps);

        survey.getSections().add(infrastructure);
        survey.getSections().add(anytimeChannels);
        survey.getSections().add(ecorner);
        return surveyRepository.save(survey);
    }

    public SurveyResponse toResponse(Survey survey) {
        return new SurveyResponse(
            survey.getId(),
            survey.getTitle(),
            survey.getFrequency().name(),
            survey.getStartDate(),
            survey.getEndDate(),
            survey.isActive(),
            survey.getSections().stream().map(section -> new SectionResponse(
                section.getId(),
                section.getName(),
                section.getDisplayOrder(),
                section.getMutuallyExclusiveWith() == null ? null : section.getMutuallyExclusiveWith().getId(),
                section.getSubsections().stream().map(subsection -> new SubsectionResponse(
                    subsection.getId(),
                    subsection.getName(),
                    subsection.getDisplayOrder(),
                    subsection.getQuestions().stream().map(question -> new QuestionResponse(
                        question.getId(),
                        question.getQuestionText(),
                        question.getOptionType().name(),
                        question.getWeightage(),
                        question.getFrequency().name(),
                        question.getDisplayOrder(),
                        question.getDependsOnQuestion() == null ? null : question.getDependsOnQuestion().getId(),
                        question.getDependsOnAnswer(),
                        question.getOptions().stream()
                            .map(option -> new QuestionOptionResponse(option.getId(), option.getOptionText(), option.getOptionValue(), option.getDisplayOrder()))
                            .toList()
                    )).toList()
                )).toList()
            )).toList()
        );
    }

    private SurveySummary toSummary(Survey survey) {
        return new SurveySummary(
            survey.getId(),
            survey.getTitle(),
            survey.getFrequency().name(),
            survey.getStartDate(),
            survey.getEndDate(),
            survey.isActive()
        );
    }

    private Section section(Survey survey, String name, int order, Section mutual) {
        return Section.builder()
            .survey(survey)
            .name(name)
            .displayOrder(order)
            .mutuallyExclusiveWith(mutual)
            .build();
    }

    private Subsection subsection(Section section, String name, int order) {
        return Subsection.builder()
            .section(section)
            .name(name)
            .displayOrder(order)
            .build();
    }

    private Question question(
        Subsection subsection,
        String text,
        QuestionOptionType optionType,
        int order,
        Question dependsOnQuestion,
        String dependsOnAnswer
    ) {
        return Question.builder()
            .subsection(subsection)
            .questionText(text)
            .optionType(optionType)
            .weightage(BigDecimal.valueOf(5))
            .frequency(SurveyFrequency.MONTHLY)
            .displayOrder(order)
            .dependsOnQuestion(dependsOnQuestion)
            .dependsOnAnswer(dependsOnAnswer)
            .build();
    }

    private QuestionOption option(Question question, String text, String value, int order) {
        return QuestionOption.builder()
            .question(question)
            .optionText(text)
            .optionValue(value)
            .displayOrder(order)
            .build();
    }
}
