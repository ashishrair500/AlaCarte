# FoodByMood Agent

An AI-powered food recommendation agent that analyzes menu PDFs and recommends dishes based on user mood and preferences using Google Gemini AI.

## Features

- 📄 Reads menu from PDF files
- 🤖 AI-powered recommendations using Google Gemini
- 🎯 Mood-based food suggestions
- 🌐 RESTful API endpoint for frontend integration
- ☕ Built with Spring Boot and Spring AI

## Prerequisites

- Java 17 or higher
- Gradle 8.0+ (or use the included Gradle Wrapper)
- Google Gemini API key ([Get one here](https://makersuite.google.com/app/apikey))

## Setup

1. **Clone and navigate to the project:**
   ```bash
   cd MenuAgent
   ```

2. **Set your Gemini API key:**
   ```bash
   export GEMINI_API_KEY=your-api-key-here
   ```
   
   Or edit `src/main/resources/application.properties` and set it directly (not recommended for production).

3. **Add your menu PDF:**
   Place your menu PDF file at `src/main/resources/menu.pdf`

4. **Build the project:**
   ```bash
   ./gradlew build
   ```
   
   Or if you don't have Gradle wrapper:
   ```bash
   gradle build
   ```

5. **Run the application:**
   ```bash
   ./gradlew bootRun
   ```
   
   Or if you don't have Gradle wrapper:
   ```bash
   gradle bootRun
   ```

The application will start on `http://localhost:8080`

## API Endpoints

### POST `/api/food/recommend`

Get food recommendations based on mood and preferences.

**Request Body:**
```json
{
  "mood": "happy",
  "preferences": "vegetarian, spicy"
}
```

**Response:**
```json
{
  "recommendation": "Based on your happy mood and preference for vegetarian spicy food, I recommend...",
  "reasoning": "Based on your mood: happy"
}
```

**Example using curl:**
```bash
curl -X POST http://localhost:8080/api/food/recommend \
  -H "Content-Type: application/json" \
  -d '{
    "mood": "happy",
    "preferences": "vegetarian, spicy"
  }'
```

### GET `/api/food/health`

Health check endpoint.

**Response:**
```
FoodByMood Agent is running!
```

## Project Structure

```
MenuAgent/
├── src/
│   ├── main/
│   │   ├── java/com/foodbymood/
│   │   │   ├── FoodByMoodApplication.java
│   │   │   ├── controller/
│   │   │   │   └── FoodRecommendationController.java
│   │   │   ├── service/
│   │   │   │   ├── MenuService.java
│   │   │   │   └── RecommendationService.java
│   │   │   └── dto/
│   │   │       ├── RecommendationRequest.java
│   │   │       └── RecommendationResponse.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── menu.pdf (add your menu here)
│   └── test/
├── build.gradle
└── settings.gradle
```

## Technologies Used

- **Spring Boot 3.2.0** - Application framework
- **Spring AI** - AI integration framework
- **Google Gemini** - AI model for recommendations
- **Apache PDFBox** - PDF text extraction
- **OpenNLP (Lang4j)** - Natural language processing
- **Lombok** - Reducing boilerplate code

## Configuration

Edit `src/main/resources/application.properties` to customize:

- Server port (default: 8080)
- Menu PDF path
- Gemini API settings

## Notes

- The menu PDF is cached in memory after first read for performance
- No database required - simple input/output as requested
- CORS is enabled for frontend integration

## License

MIT

