package com.ai.interview.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmitAnswerRequest {
    @NotBlank(message = "Question ID is required")
    private String questionId;

    @NotBlank(message = "Answer text is required")
    private String answer;
}
