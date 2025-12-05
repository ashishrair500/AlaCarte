package com.alaCarte.service;

//import dev.langchain4j.model.chat.ChatLanguageModel;
import org.springframework.stereotype.Service;

@Service
public class RecommendationService {

//    private final ChatLanguageModel chatLanguageModel;
//    private final MenuService menuService;
//
//    public RecommendationService(ChatLanguageModel chatLanguageModel, MenuService menuService) {
//        this.chatLanguageModel = chatLanguageModel;
//        this.menuService = menuService;
//    }
//
//    public String getRecommendation(String mood, String preferences) {
//        try {
//          //  String menuText = menuService.getMenuText();
//
//            String promptText = String.format(
//                "You are a food recommendation assistant. Based on the following menu and the user's mood/preferences, recommend the best food items.\n\n" +
//                "Menu:\n%s\n\n" +
//                "User's Mood: %s\n" +
//                "User's Preferences: %s\n\n" +
//                "Please provide a recommendation with reasoning. Keep it concise and friendly.",
//              //  menuText,
//                mood != null ? mood : "not specified",
//                preferences != null ? preferences : "none"
//            );
//
//            return chatLanguageModel.generate(promptText);
//        } catch (Exception e) {
//            System.err.println("Recommendation Service Error: " + e.getMessage());
//            e.printStackTrace();
//            throw e;
//        }
//    }
}

