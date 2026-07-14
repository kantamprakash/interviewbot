package com.ai.interview.controller;

import com.ai.interview.dto.EvaluationDTO;
import com.ai.interview.entity.User;
import com.ai.interview.repository.UserRepository;
import com.ai.interview.service.EvaluationResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/evaluations")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class EvaluationController {

    private final EvaluationResultService evaluationService;
    private final UserRepository userRepository;

    @GetMapping("/{sessionId}")
    public ResponseEntity<EvaluationDTO> getEvaluation(
            @PathVariable Long sessionId,
            @RequestHeader("X-User-Id") Long requesterId) {
        User user = userRepository.findById(requesterId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!"ADMIN".equals(user.getRole())) {
            throw new RuntimeException("Access denied: admin role required");
        }
        EvaluationDTO evaluation = evaluationService.getEvaluation(sessionId);
        return ResponseEntity.ok(evaluation);
    }
}
