package com.ai.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationResponse {
    private String feedback;
    private Double score;
    private String strengths;
    private String areasForImprovement;
}
