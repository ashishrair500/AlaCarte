package com.foodbymood.controller;

import com.foodbymood.dto.RecommendationRequest;
import com.foodbymood.dto.RecommendationResponse;
import com.foodbymood.service.RecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/food")
@CrossOrigin(origins = "*")
public class FoodRecommendationController {

    private final RecommendationService recommendationService;

    public FoodRecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @PostMapping("/recommend")
    public ResponseEntity<RecommendationResponse> recommendFood(
            @RequestBody RecommendationRequest request) {
        
        String recommendation = recommendationService.getRecommendation(
            request.getMood(),
            request.getPreferences()
        );

        RecommendationResponse response = new RecommendationResponse();
        response.setRecommendation(recommendation);
        response.setReasoning("Based on your mood: " + 
            (request.getMood() != null ? request.getMood() : "not specified"));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("FoodByMood Agent is running!");
    }
}

