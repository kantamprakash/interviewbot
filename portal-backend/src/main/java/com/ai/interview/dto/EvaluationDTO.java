package com.ai.interview.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationDTO {
    private Long id;
    private Long sessionId;
    private Double overallScore;
    private Integer totalQuestionsAttempted;
    private Double averageScore;
    private String strengths;
    private String areasForImprovement;
    private String summary;
    private LocalDateTime evaluatedAt;
}
