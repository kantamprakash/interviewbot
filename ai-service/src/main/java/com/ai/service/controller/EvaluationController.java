package com.ai.service.controller;

import com.ai.service.dto.EvaluationRequest;
import com.ai.service.dto.EvaluationResponse;
import com.ai.service.service.AIEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/evaluate")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class EvaluationController {

    private final AIEvaluationService evaluationService;

    @PostMapping("/answer")
    public ResponseEntity<EvaluationResponse> evaluateAnswer(@RequestBody EvaluationRequest request) {
        String feedback = evaluationService.evaluateAnswer(request.getQuestion(), request.getAnswer());
        Double score = evaluationService.scoreAnswer(request.getQuestion(), request.getAnswer());

        EvaluationResponse response = new EvaluationResponse();
        response.setFeedback(feedback);
        response.setScore(score);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/question")
    public ResponseEntity<String> generateQuestion(
            @RequestParam String topic,
            @RequestParam(defaultValue = "medium") String difficulty) {
        String question = evaluationService.generateQuestion(topic, difficulty);
        return ResponseEntity.ok(question);
    }
}
