package com.ai.service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AIEvaluationService {

    private final ChatClient chatClient;

    public String evaluateAnswer(String question, String answer) {
        String prompt = String.format("""
                You are an expert technical interview evaluator.

                Question: %s
                Candidate's Answer: %s

                Please evaluate the answer considering:
                1. Technical correctness
                2. Completeness of explanation
                3. Best practices demonstrated
                4. Communication clarity

                Provide constructive feedback and a score from 1-10.
                """, question, answer);

        String response = chatClient.prompt(prompt).call().content();
        return response;
    }

    public double scoreAnswer(String question, String answer) {
        String prompt = String.format("""
                Rate the following answer on a scale of 1-10.
                Only respond with a number between 1 and 10.

                Question: %s
                Answer: %s
                """, question, answer);

        String response = chatClient.prompt(prompt).call().content();
        try {
            return Double.parseDouble(response.trim());
        } catch (NumberFormatException e) {
            return 5.0;
        }
    }

    public String generateQuestion(String topic, String difficulty) {
        String prompt = String.format("""
                Generate a technical interview question about %s with %s difficulty.
                The question should be clear, specific, and suitable for a %s-level interview.
                """, topic, difficulty, difficulty);

        return chatClient.prompt(prompt).call().content();
    }
}
