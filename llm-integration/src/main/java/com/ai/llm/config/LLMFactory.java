package com.ai.llm.config;

import com.ai.llm.provider.LLMProvider;
import com.ai.llm.provider.OpenAIProvider;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LLMFactory {

    private static volatile LLMProvider instance;

    public static LLMProvider getProvider() {
        if (instance == null) {
            synchronized (LLMFactory.class) {
                if (instance == null) {
                    instance = initializeProvider();
                }
            }
        }
        return instance;
    }

    public static void setProvider(LLMProvider provider) {
        synchronized (LLMFactory.class) {
            instance = provider;
            log.info("LLM Provider set to: {}", provider.getModelName());
        }
    }

    private static LLMProvider initializeProvider() {
        String openAIKey = System.getenv("OPENAI_API_KEY");

        if (openAIKey != null && !openAIKey.isEmpty()) {
            log.info("Initializing OpenAI provider");
            return new OpenAIProvider(openAIKey, "gpt-4");
        }

        log.warn("No LLM provider configured. Using placeholder provider.");
        return new PlaceholderProvider();
    }

    @Slf4j
    public static class PlaceholderProvider implements LLMProvider {
        @Override
        public String generateCompletion(String prompt) {
            return generateCompletion(prompt, 0.7);
        }

        @Override
        public String generateCompletion(String prompt, double temperature) {
            log.warn("Using placeholder provider - no actual LLM service available");
            return "Placeholder response";
        }

        @Override
        public String chat(String systemPrompt, String userMessage) {
            log.warn("Using placeholder provider - no actual LLM service available");
            return "Placeholder response";
        }

        @Override
        public double scoreResponse(String criteria, String response) {
            return 5.0;
        }

        @Override
        public boolean isAvailable() {
            return false;
        }

        @Override
        public String getModelName() {
            return "placeholder";
        }
    }
}
