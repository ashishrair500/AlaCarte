package com.alaCarte.llmServices.dto;

import lombok.Data;

/**
 * Response DTO for LLM API calls
 * Contains the generated content and metadata
 */
@Data
public class LlmResponse {
    private String content;
    private LlmMetadata metadata;
    private boolean success;
    private String errorMessage;

    public LlmResponse() {
    }

    public LlmResponse(String content, LlmMetadata metadata) {
        this.content = content;
        this.metadata = metadata;
        this.success = true;
    }

    public LlmResponse(String content, LlmMetadata metadata, boolean success, String errorMessage) {
        this.content = content;
        this.metadata = metadata;
        this.success = success;
        this.errorMessage = errorMessage;
    }
    // Static factory methods
    public static LlmResponse success(String content, LlmMetadata metadata) {
        return new LlmResponse(content, metadata, true, null);
    }

    public static LlmResponse error(String errorMessage) {
        return new LlmResponse(null, null, false, errorMessage);
    }

    @Override
    public String toString() {
        return "LlmResponse{" +
                "content='" + content + '\'' +
                ", metadata=" + metadata +
                ", success=" + success +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}

