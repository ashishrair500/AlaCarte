package com.alaCarte.dto.recommendation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationRequest {
    private String mood;
    private String preferences; // e.g., "vegetarian", "spicy", "healthy"
}
