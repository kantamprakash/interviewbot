package com.ai.interview.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleInterviewRequest {

    @NotNull(message = "Candidate is required")
    private Long candidateId;

    @NotBlank(message = "Title is required")
    private String title;

    @NotEmpty(message = "At least one question must be assigned")
    private List<Long> questionIds;

    private LocalDateTime dueAt;
}
