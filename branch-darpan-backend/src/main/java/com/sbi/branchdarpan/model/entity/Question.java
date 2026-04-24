package com.sbi.branchdarpan.model.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.sbi.branchdarpan.model.enums.QuestionOptionType;
import com.sbi.branchdarpan.model.enums.SurveyFrequency;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "questions")
public class Question extends CreatedEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subsection_id", nullable = false)
    private Subsection subsection;

    @Lob
    @Column(name = "question_text", nullable = false)
    private String questionText;

    @Enumerated(EnumType.STRING)
    @Column(name = "option_type", nullable = false, length = 20)
    private QuestionOptionType optionType;

    @Column(nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal weightage = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SurveyFrequency frequency;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "depends_on_question_id")
    private Question dependsOnQuestion;

    @Column(name = "depends_on_answer", length = 200)
    private String dependsOnAnswer;

    @Builder.Default
    @OrderBy("displayOrder asc")
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuestionOption> options = new ArrayList<>();
}
