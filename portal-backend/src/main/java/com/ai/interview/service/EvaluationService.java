package com.ai.interview.service;

import com.ai.interview.dto.AiEvaluationRequest;
import com.ai.interview.dto.AiEvaluationResponse;
import com.ai.interview.entity.Question;
import com.ai.interview.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class EvaluationService {

    private final QuestionRepository questionRepository;
    private final RestTemplate restTemplate;

    @Value("${ai-service.base-url}")
    private String aiServiceBaseUrl;

    public AiEvaluationResponse evaluate(String answerText, String questionId) {
        Question question = questionRepository.findById(Long.parseLong(questionId))
                .orElseThrow(() -> new RuntimeException("Question not found"));

        AiEvaluationRequest request = new AiEvaluationRequest(question.getQuestionText(), answerText);

        AiEvaluationResponse response = restTemplate.postForObject(
                aiServiceBaseUrl + "/api/evaluate/answer", request, AiEvaluationResponse.class);

        if (response == null) {
            throw new RuntimeException("AI service returned no response");
        }

        return response;
    }
}
