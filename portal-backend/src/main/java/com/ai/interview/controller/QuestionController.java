package com.ai.interview.controller;

import com.ai.interview.dto.CreateQuestionRequest;
import com.ai.interview.dto.QuestionDTO;
import com.ai.interview.service.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class QuestionController {

    private final QuestionService questionService;

    @GetMapping
    public ResponseEntity<List<QuestionDTO>> getAllQuestions() {
        return ResponseEntity.ok(questionService.getAllActiveQuestions());
    }

    @GetMapping("/topic/{topic}")
    public ResponseEntity<List<QuestionDTO>> getByTopic(@PathVariable String topic) {
        return ResponseEntity.ok(questionService.getQuestionsByTopic(topic));
    }

    @GetMapping("/difficulty/{difficulty}")
    public ResponseEntity<List<QuestionDTO>> getByDifficulty(@PathVariable String difficulty) {
        return ResponseEntity.ok(questionService.getQuestionsByDifficulty(difficulty));
    }

    @GetMapping("/topic/{topic}/difficulty/{difficulty}")
    public ResponseEntity<List<QuestionDTO>> getByTopicAndDifficulty(
            @PathVariable String topic,
            @PathVariable String difficulty) {
        return ResponseEntity.ok(questionService.getQuestionsByTopicAndDifficulty(topic, difficulty));
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuestionDTO> getQuestion(@PathVariable Long id) {
        return ResponseEntity.ok(questionService.getQuestion(id));
    }

    @PostMapping
    public ResponseEntity<QuestionDTO> createQuestion(
            @Valid @RequestBody CreateQuestionRequest request,
            @RequestHeader("X-User-Id") Long adminUserId) {
        QuestionDTO question = questionService.createQuestion(request, adminUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(question);
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuestionDTO> updateQuestion(
            @PathVariable Long id,
            @Valid @RequestBody CreateQuestionRequest request,
            @RequestHeader("X-User-Id") Long adminUserId) {
        QuestionDTO question = questionService.updateQuestion(id, request, adminUserId);
        return ResponseEntity.ok(question);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestion(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long adminUserId) {
        questionService.deleteQuestion(id, adminUserId);
        return ResponseEntity.noContent().build();
    }
}
