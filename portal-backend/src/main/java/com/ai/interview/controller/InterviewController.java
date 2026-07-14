package com.ai.interview.controller;

import com.ai.interview.dto.AnswerDTO;
import com.ai.interview.dto.InterviewSessionDTO;
import com.ai.interview.dto.ScheduleInterviewRequest;
import com.ai.interview.dto.SubmitAnswerRequest;
import com.ai.interview.entity.User;
import com.ai.interview.repository.UserRepository;
import com.ai.interview.service.InterviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/interviews")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class InterviewController {

    private final InterviewService interviewService;
    private final UserRepository userRepository;

    private void requireAdmin(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!"ADMIN".equals(user.getRole())) {
            throw new RuntimeException("Access denied: admin role required");
        }
    }

    @PostMapping("/schedule")
    public ResponseEntity<InterviewSessionDTO> scheduleInterview(
            @Valid @RequestBody ScheduleInterviewRequest request,
            @RequestHeader("X-User-Id") Long adminUserId) {
        requireAdmin(adminUserId);
        InterviewSessionDTO session = interviewService.scheduleInterview(request, adminUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(session);
    }

    @GetMapping
    public ResponseEntity<List<InterviewSessionDTO>> getAllSessions(
            @RequestHeader("X-User-Id") Long adminUserId) {
        requireAdmin(adminUserId);
        return ResponseEntity.ok(interviewService.getAllSessionsForAdmin());
    }

    @GetMapping("/candidate/{candidateId}")
    public ResponseEntity<List<InterviewSessionDTO>> getCandidateSessions(
            @PathVariable Long candidateId,
            @RequestHeader("X-User-Id") Long requesterId,
            @RequestHeader(value = "X-User-Role", required = false) String requesterRole) {
        if (!requesterId.equals(candidateId) && !"ADMIN".equals(requesterRole)) {
            throw new RuntimeException("Access denied");
        }
        return ResponseEntity.ok(interviewService.getSessionsForCandidate(candidateId));
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<InterviewSessionDTO> getSession(
            @PathVariable Long sessionId,
            @RequestHeader("X-User-Id") Long requesterId,
            @RequestHeader(value = "X-User-Role", required = false) String requesterRole) {
        return ResponseEntity.ok(interviewService.getSession(sessionId, requesterId, requesterRole));
    }

    @PostMapping("/{sessionId}/answers")
    public ResponseEntity<AnswerDTO> submitAnswer(
            @PathVariable Long sessionId,
            @Valid @RequestBody SubmitAnswerRequest request,
            @RequestHeader("X-User-Id") Long requesterId) {
        AnswerDTO answer = interviewService.submitAnswer(sessionId, requesterId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(answer);
    }

    @PostMapping("/{sessionId}/submit")
    public ResponseEntity<InterviewSessionDTO> submitInterview(
            @PathVariable Long sessionId,
            @RequestHeader("X-User-Id") Long requesterId) {
        return ResponseEntity.ok(interviewService.submitInterview(sessionId, requesterId));
    }
}
