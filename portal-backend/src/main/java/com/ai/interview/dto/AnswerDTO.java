package com.ai.interview.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnswerDTO {
    private Long id;
    private String questionId;
    private String answerText;
    private Double score;
    private String feedback;
    private LocalDateTime submittedAt;
}
