package com.ai.llm.provider;

public interface LLMProvider {

    String generateCompletion(String prompt);

    String generateCompletion(String prompt, double temperature);

    String chat(String systemPrompt, String userMessage);

    double scoreResponse(String criteria, String response);

    boolean isAvailable();

    String getModelName();
}
