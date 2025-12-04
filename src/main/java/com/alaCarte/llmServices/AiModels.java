package com.alaCarte.llmServices;

import com.alaCarte.llmServices.dto.LlmMetadata;
import com.alaCarte.llmServices.dto.LlmRequest;
import com.alaCarte.llmServices.dto.LlmResponse;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.output.Response;
import lombok.Data;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * AI Models Service - Non-Streaming Implementation
 * LangChain4j version 0.35.0 compatible
 * 
 * NOTE: Streaming is NOT supported in this version due to LangChain4j 0.35.0 limitations
 * Use generate(), generateWithGemini(), or generateWithOpenAi() methods
 * 
 * All methods return LlmResponse with metadata including token usage and response time
 */
@Data
@Service
public class AiModels {

    @Value("${langchain4j.google.ai.gemini.api-key}")
    private String geminiApiKey = "AIzaSyAW4StK3Do4jSNaiICOR6kyMacRZQPQaxQ";

    @Value("${langchain4j.open-ai.api-key}")
    private String openaiApiKey;

    @Getter
    @Value("${llm.provider:gemini}")
    private String defaultProvider = "gemini";

    @Getter
    @Value("${llm.model.name:gemini-1.5-flash}")
    private String defaultModelName = "gemini-1.5-flash";

    @Getter
    @Value("${llm.temperature:0.7}")
    private Double defaultTemperature = 0.7;

    @Getter
    @Value("${llm.max-tokens:2000}")
    private Integer defaultMaxTokens = 2000;

    /**
     * Generate a response using the default configured model
     */

    public static void main(String args[]){
        LlmRequest llmRequest = new LlmRequest();
        llmRequest.setSystemMessage("You are a helpful assistant.");
        llmRequest.setUserMessage("Hello, how are you?");
        llmRequest.setTemperature(0.7);
        llmRequest.setMaxTokens(2000);
        AiModels aiModels = new AiModels();

        // OpenAI works perfectly - use this!
        System.out.println("\n=== Testing OpenAI (Recommended - Works!) ===");
        System.out.println(aiModels.generateWithOpenAi(llmRequest));
        
        // Gemini currently not working with LangChain4j 0.36.2 due to API compatibility
        // All model names return 404 errors
    }


    public LlmResponse generate(LlmRequest request) {
        long startTime = System.currentTimeMillis();

        try {
            String provider = defaultProvider.toLowerCase();
            String modelName = request.getModelOverride() != null ? request.getModelOverride() : defaultModelName;

            ChatLanguageModel chatModel = buildChatModel(provider, modelName, request);
            List<ChatMessage> messages = buildMessages(request);
            Response<AiMessage> response = chatModel.generate(messages);

            String content = response.content().text();
            LlmMetadata metadata = buildMetadata(response, provider, modelName, startTime);

            return LlmResponse.success(content, metadata);

        } catch (Exception e) {
            System.err.println("Error generating LLM response: " + e.getMessage());
            e.printStackTrace();
            return LlmResponse.error("Failed to generate response: " + e.getMessage());
        }
    }

    /**
     * Generate a response using Gemini model
     */
    public LlmResponse generateWithGemini(LlmRequest request) {
        long startTime = System.currentTimeMillis();

        try {
            String modelName = request.getModelOverride() != null ? request.getModelOverride() : "gemini-1.5-flash";
            
            ChatLanguageModel geminiModel = buildGeminiChatModel(modelName, request);
            List<ChatMessage> messages = buildMessages(request);
            Response<AiMessage> response = geminiModel.generate(messages);

            String content = response.content().text();
            LlmMetadata metadata = buildMetadata(response, "gemini", modelName, startTime);

            return LlmResponse.success(content, metadata);

        } catch (Exception e) {
            System.err.println("Error generating Gemini response: " + e.getMessage());
            e.printStackTrace();
            return LlmResponse.error("Failed to generate Gemini response: " + e.getMessage());
        }
    }

    /**
     * Generate a response using OpenAI model
     */
    public LlmResponse generateWithOpenAi(LlmRequest request) {
        long startTime = System.currentTimeMillis();

        try {
            String modelName = request.getModelOverride() != null ? request.getModelOverride() : "gpt-4";
            
            ChatLanguageModel openAiModel = buildOpenAiChatModel(modelName, request);
            List<ChatMessage> messages = buildMessages(request);
            Response<AiMessage> response = openAiModel.generate(messages);

            String content = response.content().text();
            LlmMetadata metadata = buildMetadata(response, "openai", modelName, startTime);

            return LlmResponse.success(content, metadata);

        } catch (Exception e) {
            System.err.println("Error generating OpenAI response: " + e.getMessage());
            e.printStackTrace();
            return LlmResponse.error("Failed to generate OpenAI response: " + e.getMessage());
        }
    }

    // ============ Private Helper Methods ============

    private ChatLanguageModel buildChatModel(String provider, String modelName, LlmRequest request) {
        return switch (provider) {
            case "gemini" -> buildGeminiChatModel(modelName, request);
            case "openai" -> buildOpenAiChatModel(modelName, request);
            default -> throw new IllegalArgumentException("Unsupported provider: " + provider);
        };
    }

    private ChatLanguageModel buildGeminiChatModel(String modelName, LlmRequest request) {
        Double temperature = request.getTemperature() != null ? request.getTemperature() : defaultTemperature;
        Integer maxTokens = request.getMaxTokens() != null ? request.getMaxTokens() : defaultMaxTokens;

        return GoogleAiGeminiChatModel.builder()
                .apiKey(geminiApiKey)
                .modelName(modelName)
                .temperature(temperature)
                .maxOutputTokens(maxTokens)
                .timeout(Duration.ofSeconds(60))
                .build();
    }

    private ChatLanguageModel buildOpenAiChatModel(String modelName, LlmRequest request) {
        Double temperature = request.getTemperature() != null ? request.getTemperature() : defaultTemperature;
        Integer maxTokens = request.getMaxTokens() != null ? request.getMaxTokens() : defaultMaxTokens;

        return OpenAiChatModel.builder()
                .apiKey(openaiApiKey)
                .modelName(modelName)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .timeout(Duration.ofSeconds(60))
                .build();
    }

    private List<ChatMessage> buildMessages(LlmRequest request) {
        List<ChatMessage> messages = new ArrayList<>();

        if (request.getSystemMessage() != null && !request.getSystemMessage().isEmpty()) {
            messages.add(SystemMessage.from(request.getSystemMessage()));
        }

        if (request.getUserMessage() != null && !request.getUserMessage().isEmpty()) {
            messages.add(UserMessage.from(request.getUserMessage()));
        } else {
            throw new IllegalArgumentException("User message is required");
        }

        return messages;
    }

    private LlmMetadata buildMetadata(Response<AiMessage> response, String provider, String modelName, long startTime) {
        long responseTime = System.currentTimeMillis() - startTime;

        LlmMetadata.Builder metadataBuilder = LlmMetadata.builder()
                .provider(provider)
                .modelUsed(modelName)
                .responseTimeMs(responseTime);

        if (response.tokenUsage() != null) {
            metadataBuilder
                    .inputTokens(response.tokenUsage().inputTokenCount())
                    .outputTokens(response.tokenUsage().outputTokenCount())
                    .totalTokens(response.tokenUsage().totalTokenCount());
        }

        if (response.finishReason() != null) {
            metadataBuilder.finishReason(response.finishReason().toString());
        }

        return metadataBuilder.build();
    }

    // ============ Utility Methods ============

}

