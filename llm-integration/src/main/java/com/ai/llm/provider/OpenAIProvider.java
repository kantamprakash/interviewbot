package com.ai.llm.provider;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OpenAIProvider implements LLMProvider {

    private final String apiKey;
    private final String model;
    private volatile boolean available;

    public OpenAIProvider(String apiKey, String model) {
        this.apiKey = apiKey;
        this.model = model;
        this.available = validateConnection();
    }

    @Override
    public String generateCompletion(String prompt) {
        return generateCompletion(prompt, 0.7);
    }

    @Override
    public String generateCompletion(String prompt, double temperature) {
        if (!available) {
            throw new RuntimeException("OpenAI service is not available");
        }

        try {
            // TODO: Call OpenAI API using the API key
            log.info("Generating completion for prompt with temperature: {}", temperature);
            return "OpenAI completion placeholder";
        } catch (Exception e) {
            log.error("Error calling OpenAI API", e);
            throw new RuntimeException("Failed to generate completion", e);
        }
    }

    @Override
    public String chat(String systemPrompt, String userMessage) {
        if (!available) {
            throw new RuntimeException("OpenAI service is not available");
        }

        try {
            // TODO: Call OpenAI chat API
            log.info("Chat message received: {}", userMessage);
            return "OpenAI chat response placeholder";
        } catch (Exception e) {
            log.error("Error calling OpenAI chat API", e);
            throw new RuntimeException("Failed to get chat response", e);
        }
    }

    @Override
    public double scoreResponse(String criteria, String response) {
        String prompt = String.format("Score this response on a scale of 1-10 based on: %s\nResponse: %s\nOnly return a number.", criteria, response);
        String result = generateCompletion(prompt);
        try {
            return Double.parseDouble(result.trim());
        } catch (NumberFormatException e) {
            return 5.0;
        }
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public String getModelName() {
        return model;
    }

    private boolean validateConnection() {
        try {
            return apiKey != null && !apiKey.isEmpty();
        } catch (Exception e) {
            log.warn("Failed to validate OpenAI connection", e);
            return false;
        }
    }
}
