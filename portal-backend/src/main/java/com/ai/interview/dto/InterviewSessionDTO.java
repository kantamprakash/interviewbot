package com.ai.interview.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InterviewSessionDTO {
    private Long id;
    private String title;
    private String status;
    private Long candidateId;
    private String candidateName;
    private String candidateEmail;
    private LocalDateTime dueAt;
    private List<AssignedQuestionDTO> assignedQuestions;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private List<AnswerDTO> answers;
    private EvaluationDTO evaluation;
    private boolean recordingAvailable;
}
