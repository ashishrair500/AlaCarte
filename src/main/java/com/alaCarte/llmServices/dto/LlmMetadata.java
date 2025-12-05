package com.alaCarte.llmServices.dto;

import dev.langchain4j.model.output.FinishReason;
import lombok.Data;

/**
 * Metadata for LLM responses
 * Contains information about token usage, model used, and finish reason
 */

@Data
public class LlmMetadata {
    private String modelUsed;
    private String provider;
    private Integer inputTokens;
    private Integer outputTokens;
    private Integer totalTokens;
    private FinishReason finishReason;
    private Long responseTimeMs;

    public LlmMetadata() {
    }

    public LlmMetadata(String modelUsed, String provider) {
        this.modelUsed = modelUsed;
        this.provider = provider;
    }
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String modelUsed;
        private String provider;
        private Integer inputTokens;
        private Integer outputTokens;
        private Integer totalTokens;
        private FinishReason finishReason;
        private Long responseTimeMs;

        public Builder modelUsed(String modelUsed) {
            this.modelUsed = modelUsed;
            return this;
        }

        public Builder provider(String provider) {
            this.provider = provider;
            return this;
        }

        public Builder inputTokens(Integer inputTokens) {
            this.inputTokens = inputTokens;
            return this;
        }

        public Builder outputTokens(Integer outputTokens) {
            this.outputTokens = outputTokens;
            return this;
        }

        public Builder totalTokens(Integer totalTokens) {
            this.totalTokens = totalTokens;
            return this;
        }

        public Builder finishReason(FinishReason finishReason) {
            this.finishReason = finishReason;
            return this;
        }

        public Builder responseTimeMs(Long responseTimeMs) {
            this.responseTimeMs = responseTimeMs;
            return this;
        }

        public LlmMetadata build() {
            LlmMetadata metadata = new LlmMetadata();
            metadata.modelUsed = this.modelUsed;
            metadata.provider = this.provider;
            metadata.inputTokens = this.inputTokens;
            metadata.outputTokens = this.outputTokens;
            metadata.totalTokens = this.totalTokens;
            metadata.finishReason = this.finishReason;
            metadata.responseTimeMs = this.responseTimeMs;
            return metadata;
        }
    }

    @Override
    public String toString() {
        return "LlmMetadata{" +
                "modelUsed='" + modelUsed + '\'' +
                ", provider='" + provider + '\'' +
                ", inputTokens=" + inputTokens +
                ", outputTokens=" + outputTokens +
                ", totalTokens=" + totalTokens +
                ", finishReason='" + finishReason + '\'' +
                ", responseTimeMs=" + responseTimeMs +
                '}';
    }
}

