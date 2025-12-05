package com.alaCarte.llmServices;

import com.alaCarte.llmServices.dto.LlmMetadata;
import com.alaCarte.llmServices.dto.LlmRequest;
import com.alaCarte.llmServices.dto.LlmResponse;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.Data;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Data
@Service
public class AiModels {

    @Value("${langchain4j.google.ai.gemini.api-key}")
    private String geminiApiKey ;

    @Value("${langchain4j.open-ai.api-key}")
    private String openaiApiKey;

    @Getter
    @Value("${llm.provider:gemini}")
    private String defaultProvider;

    @Getter
    @Value("${llm.model.name:gemini-2.0-flash}")
    private String defaultModelName ;

    @Getter
    @Value("${llm.temperature:0.7}")
    private Double defaultTemperature = 0.7;

    @Getter
    @Value("${llm.max-tokens:2000}")
    private Integer defaultMaxTokens = 2000;
    /**
     * Generate a response using the default configured model
     */
    public LlmResponse generate(LlmRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            String provider = defaultProvider.toLowerCase();
            String modelName = request.getModelOverride() != null ? request.getModelOverride() : defaultModelName;
            ChatModel chatModel = buildChatModel(provider, modelName, request);
            List<ChatMessage> messages = buildMessages(request);
            ChatResponse response = chatModel.chat(messages);
            return buildLlmResponse(response, provider, startTime);
        } catch (Exception e) {
            System.err.println("Error generating LLM response: " + e.getMessage());
            e.printStackTrace();
            return LlmResponse.error("Failed to generate response: " + e.getMessage());
        }
    }

    public LlmResponse generateWithGemini(LlmRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            String modelName = request.getModelOverride() != null ? request.getModelOverride() : "gemini-2.0-flash";

            ChatModel geminiModel = buildGeminiChatModel(modelName, request);
            List<ChatMessage> messages = buildMessages(request);
            ChatRequest chatRequest = ChatRequest.builder().messages(messages).build();
            ChatResponse chatResponse = geminiModel.chat(chatRequest);

            return buildLlmResponse(chatResponse, "gemini", startTime);
        } catch (Exception e) {
            System.err.println("Error generating Gemini response: " + e.getMessage());
            return LlmResponse.error("Failed to generate Gemini response: " + e.getMessage());
        }
    }


    public LlmResponse generateWithOpenAi(LlmRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            String modelName = request.getModelOverride() != null ? request.getModelOverride() : "gpt-4";
            ChatModel openAiModel = buildOpenAiChatModel(modelName, request);
            List<ChatMessage> messages = buildMessages(request);
            ChatResponse response = openAiModel.chat(messages);
            return buildLlmResponse(response, "openAi", startTime);
        } catch (Exception e) {
            System.err.println("Error generating OpenAI response: " + e.getMessage());
            return LlmResponse.error("Failed to generate OpenAI response: " + e.getMessage());
        }
    }

    // ============ Private Helper Methods ============
    private ChatModel buildChatModel(String provider, String modelName, LlmRequest request) {
        return switch (provider) {
            case "gemini" -> buildGeminiChatModel(modelName, request);
            case "openai" -> buildOpenAiChatModel(modelName, request);
            default -> throw new IllegalArgumentException("Unsupported provider: " + provider);
        };
    }

    private ChatModel buildGeminiChatModel(String modelName, LlmRequest request) {
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

    private ChatModel buildOpenAiChatModel(String modelName, LlmRequest request) {
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


    private LlmMetadata buildMetadata(ChatResponse response, String provider, long startTime) {
        long responseTime = System.currentTimeMillis() - startTime;

        LlmMetadata.Builder metadataBuilder = LlmMetadata.builder()
                .provider(provider)
                .modelUsed(response.metadata().modelName())
                .responseTimeMs(responseTime);

        if (response.tokenUsage() != null) {
            metadataBuilder
                    .inputTokens(response.tokenUsage().inputTokenCount())
                    .outputTokens(response.tokenUsage().outputTokenCount())
                    .totalTokens(response.tokenUsage().totalTokenCount());
        }
        if (response.finishReason() != null) {
            metadataBuilder.finishReason(response.finishReason());
        }
        return metadataBuilder.build();
    }

    private LlmResponse buildLlmResponse(ChatResponse chatResponse, String provider, long startTime){
        LlmResponse llmResponse = new LlmResponse();
        LlmMetadata metadata = buildMetadata(chatResponse, provider, startTime);
        llmResponse.setMetadata(metadata);
        llmResponse.setContent(chatResponse.aiMessage().text());
        llmResponse.setSuccess(true);
        return llmResponse;
    }

    public static void main(String args[]) {
        LlmRequest llmRequest = new LlmRequest();
        llmRequest.setSystemMessage("You are a helpful assistant.");
        llmRequest.setUserMessage("Hello, how are you?");
        llmRequest.setTemperature(0.7);
        llmRequest.setMaxTokens(2000);
        AiModels aiModels = new AiModels();
        System.out.println("Gemini Response :" + aiModels.generate(llmRequest));
    }
}

