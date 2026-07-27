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
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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

    @PostMapping("/{sessionId}/recording")
    public ResponseEntity<Void> uploadRecording(
            @PathVariable Long sessionId,
            @RequestParam("file") MultipartFile file,
            @RequestHeader("X-User-Id") Long requesterId) {
        interviewService.attachRecording(sessionId, requesterId, file);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{sessionId}/recording")
    public ResponseEntity<ResourceRegion> getRecording(
            @PathVariable Long sessionId,
            @RequestParam(value = "role", required = false) String requesterRole,
            @RequestHeader HttpHeaders headers) throws IOException {
        UrlResource video = interviewService.getRecording(sessionId, requesterRole);
        long contentLength = video.contentLength();
        List<HttpRange> ranges = headers.getRange();

        ResourceRegion region;
        if (ranges.isEmpty()) {
            long rangeLength = Math.min(5 * 1024 * 1024, contentLength);
            region = new ResourceRegion(video, 0, rangeLength);
        } else {
            region = ranges.get(0).toResourceRegion(video);
        }

        return ResponseEntity.status(ranges.isEmpty() ? HttpStatus.OK : HttpStatus.PARTIAL_CONTENT)
                .contentType(MediaType.valueOf("video/webm"))
                .body(region);
    }
}
