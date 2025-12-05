package com.alaCarte.llmServices.dto;

import jdk.jfr.DataAmount;
import lombok.Data;

/**
 * Request DTO for LLM API calls
 * Contains all necessary parameters for generating responses
 */

@Data
public class LlmRequest {
    private String systemMessage;
    private String userMessage;
    private Double temperature;
    private Integer maxTokens;
    private String modelOverride; // Optional: Override default model for this request

    public LlmRequest() {
    }

    public LlmRequest(String userMessage) {
        this.userMessage = userMessage;
    }

    public LlmRequest(String systemMessage, String userMessage, Double temperature, Integer maxTokens) {
        this.systemMessage = systemMessage;
        this.userMessage = userMessage;
        this.temperature = temperature;
        this.maxTokens = maxTokens;
    }



    // Builder pattern for easy construction
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String systemMessage;
        private String userMessage;
        private Double temperature;
        private Integer maxTokens;
        private String modelOverride;

        public Builder systemMessage(String systemMessage) {
            this.systemMessage = systemMessage;
            return this;
        }

        public Builder userMessage(String userMessage) {
            this.userMessage = userMessage;
            return this;
        }

        public Builder temperature(Double temperature) {
            this.temperature = temperature;
            return this;
        }

        public Builder maxTokens(Integer maxTokens) {
            this.maxTokens = maxTokens;
            return this;
        }

        public Builder modelOverride(String modelOverride) {
            this.modelOverride = modelOverride;
            return this;
        }

        public LlmRequest build() {
            LlmRequest request = new LlmRequest();
            request.systemMessage = this.systemMessage;
            request.userMessage = this.userMessage;
            request.temperature = this.temperature;
            request.maxTokens = this.maxTokens;
            request.modelOverride = this.modelOverride;
            return request;
        }
    }

    @Override
    public String toString() {
        return "LlmRequest{" +
                "systemMessage='" + systemMessage + '\'' +
                ", userMessage='" + userMessage + '\'' +
                ", temperature=" + temperature +
                ", maxTokens=" + maxTokens +
                ", modelOverride='" + modelOverride + '\'' +
                '}';
    }
}

