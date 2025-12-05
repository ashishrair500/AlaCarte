package com.alaCarte.llmServices;

import com.alaCarte.llmServices.dto.LlmMetadata;
import com.alaCarte.llmServices.dto.LlmRequest;
import com.alaCarte.llmServices.dto.LlmResponse;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
//import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.output.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Simplified AI Models Service - Non-Streaming Only
 * Version 0.35.0 compatible
 *
 * NOTE: Streaming is NOT supported in this version due to LangChain4j 0.35.0 limitations
 * Use generate(), generateWithGemini(), or generateWithOpenAi() methods
 */
@Service("aiModelsSimple")
public class AiModelsSimple {
//
//    @Value("${langchain4j.google.ai.gemini.api-key}")
//    private String geminiApiKey;
//
//    @Value("${langchain4j.open-ai.api-key}")
//    private String openaiApiKey;
//
//    @Value("${llm.provider:gemini}")
//    private String defaultProvider;
//
//    @Value("${llm.model.name:gemini-1.5-flash}")
//    private String defaultModelName;
//
//    @Value("${llm.temperature:0.7}")
//    private Double defaultTemperature;
//
//    @Value("${llm.max-tokens:2000}")
//    private Integer defaultMaxTokens;
//
//    /**
//     * Generate a response using the default configured model
//     */
//    public LlmResponse generate(LlmRequest request) {
//        long startTime = System.currentTimeMillis();
//
//        try {
//            String provider = defaultProvider.toLowerCase();
//            String modelName = request.getModelOverride() != null ? request.getModelOverride() : defaultModelName;
//
//            ChatLanguageModel chatModel = buildChatModel(provider, modelName, request);
//            List<ChatMessage> messages = buildMessages(request);
//            Response<AiMessage> response = chatModel.generate(messages);
//
//            String content = response.content().text();
//            LlmMetadata metadata = buildMetadata(response, provider, modelName, startTime);
//
//            return LlmResponse.success(content, metadata);
//
//        } catch (Exception e) {
//            System.err.println("Error generating LLM response: " + e.getMessage());
//            e.printStackTrace();
//            return LlmResponse.error("Failed to generate response: " + e.getMessage());
//        }
//    }
//
//    /**
//     * Generate a response using Gemini model
//     */
//    public LlmResponse generateWithGemini(LlmRequest request) {
//        long startTime = System.currentTimeMillis();
//
//        try {
//            String modelName = request.getModelOverride() != null ? request.getModelOverride() : "gemini-1.5-flash";
//
//            ChatLanguageModel geminiModel = buildGeminiChatModel(modelName, request);
//            List<ChatMessage> messages = buildMessages(request);
//            Response<AiMessage> response = geminiModel.generate(messages);
//
//            String content = response.content().text();
//            LlmMetadata metadata = buildMetadata(response, "gemini", modelName, startTime);
//
//            return LlmResponse.success(content, metadata);
//
//        } catch (Exception e) {
//            System.err.println("Error generating Gemini response: " + e.getMessage());
//            e.printStackTrace();
//            return LlmResponse.error("Failed to generate Gemini response: " + e.getMessage());
//        }
//    }
//
//    /**
//     * Generate a response using OpenAI model
//     */
//    public LlmResponse generateWithOpenAi(LlmRequest request) {
//        long startTime = System.currentTimeMillis();
//
//        try {
//            String modelName = request.getModelOverride() != null ? request.getModelOverride() : "gpt-4";
//
//            ChatLanguageModel openAiModel = buildOpenAiChatModel(modelName, request);
//            List<ChatMessage> messages = buildMessages(request);
//            Response<AiMessage> response = openAiModel.generate(messages);
//
//            String content = response.content().text();
//            LlmMetadata metadata = buildMetadata(response, "openai", modelName, startTime);
//
//            return LlmResponse.success(content, metadata);
//
//        } catch (Exception e) {
//            System.err.println("Error generating OpenAI response: " + e.getMessage());
//            e.printStackTrace();
//            return LlmResponse.error("Failed to generate OpenAI response: " + e.getMessage());
//        }
//    }
//
//    // ============ Private Helper Methods ============
//
//    private ChatLanguageModel buildChatModel(String provider, String modelName, LlmRequest request) {
//        return switch (provider) {
//            case "gemini" -> buildGeminiChatModel(modelName, request);
//            case "openai" -> buildOpenAiChatModel(modelName, request);
//            default -> throw new IllegalArgumentException("Unsupported provider: " + provider);
//        };
//    }
//
//    private ChatLanguageModel buildGeminiChatModel(String modelName, LlmRequest request) {
//        Double temperature = request.getTemperature() != null ? request.getTemperature() : defaultTemperature;
//        Integer maxTokens = request.getMaxTokens() != null ? request.getMaxTokens() : defaultMaxTokens;
//
//        return GoogleAiGeminiChatModel.builder()
//                .apiKey(geminiApiKey)
//                .modelName(modelName)
//                .temperature(temperature)
//                .maxOutputTokens(maxTokens)
//                .timeout(Duration.ofSeconds(60))
//                .build();
//    }
//
//    private ChatLanguageModel buildOpenAiChatModel(String modelName, LlmRequest request) {
//        Double temperature = request.getTemperature() != null ? request.getTemperature() : defaultTemperature;
//        Integer maxTokens = request.getMaxTokens() != null ? request.getMaxTokens() : defaultMaxTokens;
//
//        return OpenAiChatModel.builder()
//                .apiKey(openaiApiKey)
//                .modelName(modelName)
//                .temperature(temperature)
//                .maxTokens(maxTokens)
//                .timeout(Duration.ofSeconds(60))
//                .build();
//    }
//
//    private List<ChatMessage> buildMessages(LlmRequest request) {
//        List<ChatMessage> messages = new ArrayList<>();
//
//        if (request.getSystemMessage() != null && !request.getSystemMessage().isEmpty()) {
//            messages.add(SystemMessage.from(request.getSystemMessage()));
//        }
//
//        if (request.getUserMessage() != null && !request.getUserMessage().isEmpty()) {
//            messages.add(UserMessage.from(request.getUserMessage()));
//        } else {
//            throw new IllegalArgumentException("User message is required");
//        }
//
//        return messages;
//    }
//
//    private LlmMetadata buildMetadata(Response<AiMessage> response, String provider, String modelName, long startTime) {
//        long responseTime = System.currentTimeMillis() - startTime;
//
//        LlmMetadata.Builder metadataBuilder = LlmMetadata.builder()
//                .provider(provider)
//                .modelUsed(modelName)
//                .responseTimeMs(responseTime);
//
//        if (response.tokenUsage() != null) {
//            metadataBuilder
//                    .inputTokens(response.tokenUsage().inputTokenCount())
//                    .outputTokens(response.tokenUsage().outputTokenCount())
//                    .totalTokens(response.tokenUsage().totalTokenCount());
//        }
//
//        if (response.finishReason() != null) {
//            metadataBuilder.finishReason(response.finishReason().toString());
//        }
//
//        return metadataBuilder.build();
//    }
//
//    // ============ Utility Methods ============
//
//    public String getDefaultProvider() {
//        return defaultProvider;
//    }
//
//    public String getDefaultModelName() {
//        return defaultModelName;
//    }
//
//    public Double getDefaultTemperature() {
//        return defaultTemperature;
//    }
//
//    public Integer getDefaultMaxTokens() {
//        return defaultMaxTokens;
//    }
}

