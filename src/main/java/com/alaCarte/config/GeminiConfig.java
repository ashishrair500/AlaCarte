package com.alaCarte.config;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class GeminiConfig {

    @Value("${langchain4j.google.ai.gemini.api-key}")
    private String apiKey;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return new GeminiChatModelAdapter(apiKey, restTemplate());
    }
}

class Model {
//    ChatModel gemini = GoogleAiGeminiChatModel.builder()
//            .apiKey(System.getenv("GEMINI_AI_KEY"))
//            .modelName("gemini-1.5-flash")
//            .build();
//
//    ChatResponse chatResponse = gemini.chat(builder()
//            .messages(UserMessage.from(
//                    "How many R's are there in the word 'strawberry'?"))
//            .build());
//
//    String response = chatResponse.aiMessage().text();
}

class GeminiChatModelAdapter implements ChatLanguageModel {
    private final String apiKey;
    private final RestTemplate restTemplate;
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-preview-05-20:generateContent";

    public GeminiChatModelAdapter(String apiKey, RestTemplate restTemplate) {
        this.apiKey = apiKey;
        this.restTemplate = restTemplate;
    }

    @Override
    public String generate(String prompt) {
        return generate(List.of(dev.langchain4j.data.message.UserMessage.from(prompt))).content().text();
    }

    @Override
    public Response<AiMessage> generate(List<ChatMessage> messages) {
        try {
            // Combine all messages into a single prompt
            StringBuilder promptBuilder = new StringBuilder();
            for (ChatMessage message : messages) {
                String text = message instanceof dev.langchain4j.data.message.UserMessage
                        ? ((dev.langchain4j.data.message.UserMessage) message).singleText()
                        : ((dev.langchain4j.data.message.AiMessage) message).text();
                promptBuilder.append(text).append("\n");
            }
            String prompt = promptBuilder.toString().trim();

            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> content = new HashMap<>();
            Map<String, Object> part = new HashMap<>();
            part.put("text", prompt);
            content.put("parts", new Object[]{part});
            requestBody.put("contents", new Object[]{content});

            String url = GEMINI_API_URL + "?key=" + apiKey;

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(url, requestBody, Map.class);

            String result = "Unable to generate response";
            if (response != null && response.containsKey("candidates")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> contentMap = (Map<String, Object>) candidates.get(0).get("content");
                    if (contentMap != null) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> parts = (List<Map<String, Object>>) contentMap.get("parts");
                        if (parts != null && !parts.isEmpty()) {
                            result = (String) parts.get(0).get("text");
                        }
                    }
                }
            }
            AiMessage aiMessage = new AiMessage(result);
            return Response.from(aiMessage);
        } catch (Exception e) {
            System.err.println("Gemini API Error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error calling Gemini API: " + e.getMessage(), e);
        }
    }
}
