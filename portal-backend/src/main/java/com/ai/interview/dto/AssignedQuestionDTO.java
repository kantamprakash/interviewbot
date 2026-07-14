package com.ai.interview.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignedQuestionDTO {
    private Long id;
    private String questionText;
    private String topic;
    private String difficulty;
}
