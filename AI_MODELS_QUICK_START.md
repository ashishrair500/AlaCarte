# AI Models Service - Quick Start Guide

## Setup Steps

### 1. Download Dependencies

Run Gradle build to download the new LangChain4j dependencies:

```bash
./gradlew build --refresh-dependencies
```

Or in your IDE, reload the Gradle project.

### 2. Configure API Keys

Update `src/main/resources/application.properties`:

```properties
# Add your actual API keys here
langchain4j.google.ai.gemini.api-key=YOUR_GEMINI_API_KEY
langchain4j.open-ai.api-key=YOUR_OPENAI_API_KEY

# Configure default model
llm.provider=gemini              # or openai
llm.model.name=gemini-1.5-flash  # or gpt-4
llm.temperature=0.7
llm.max-tokens=2000
```

### 3. Test the Service

Create a simple test controller:

```java
@RestController
@RequestMapping("/api/test")
public class TestController {
    
    private final AiModels aiModels;
    
    public TestController(AiModels aiModels) {
        this.aiModels = aiModels;
    }
    
    @GetMapping("/chat")
    public ResponseEntity<Map<String, Object>> testChat(@RequestParam String message) {
        LlmRequest request = LlmRequest.builder()
            .userMessage(message)
            .build();
            
        LlmResponse response = aiModels.generate(request);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", response.isSuccess());
        result.put("content", response.getContent());
        result.put("metadata", response.getMetadata());
        
        return ResponseEntity.ok(result);
    }
    
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> testStream(@RequestParam String message) {
        LlmRequest request = LlmRequest.builder()
            .userMessage(message)
            .build();
            
        return aiModels.generateStream(request);
    }
}
```

### 4. Test Endpoints

Start your application and test:

```bash
# Test non-streaming
curl "http://localhost:8080/api/test/chat?message=Hello"

# Test streaming
curl "http://localhost:8080/api/test/stream?message=Tell me a story"
```

## What Was Created

### ✅ Files Added/Modified

1. **Service Class**: `src/main/java/com/alaCarte/llmServices/AiModels.java`
   - Main service with all LLM functionality
   - 6 methods: 3 non-streaming, 3 streaming
   - Configuration-based model switching

2. **DTOs**: `src/main/java/com/alaCarte/llmServices/dto/`
   - `LlmRequest.java` - Request with builder pattern
   - `LlmResponse.java` - Response wrapper
   - `LlmMetadata.java` - Metadata with token info

3. **Example**: `src/main/java/com/alaCarte/llmServices/AiModelsUsageExample.java`
   - 12 usage examples
   - Can be deleted after review

4. **Documentation**:
   - `AI_MODELS_SERVICE_DOCUMENTATION.md` - Complete guide
   - `AI_MODELS_QUICK_START.md` - This file

5. **Updated Files**:
   - `build.gradle` - Added OpenAI dependency
   - `application.properties` - Added LLM configuration

## Quick Usage Examples

### Simple Request
```java
@Autowired
private AiModels aiModels;

public String getResponse() {
    LlmRequest request = LlmRequest.builder()
        .userMessage("What is AI?")
        .build();
        
    LlmResponse response = aiModels.generate(request);
    return response.getContent();
}
```

### With Parameters
```java
LlmRequest request = LlmRequest.builder()
    .systemMessage("You are a chef")
    .userMessage("Best pasta recipe?")
    .temperature(0.7)
    .maxTokens(300)
    .build();

LlmResponse response = aiModels.generate(request);
```

### Streaming
```java
@GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<String> stream() {
    LlmRequest request = LlmRequest.builder()
        .userMessage("Tell a story")
        .build();
        
    return aiModels.generateStream(request);
}
```

## Switching Models

### Switch to OpenAI GPT-4

In `application.properties`:
```properties
llm.provider=openai
llm.model.name=gpt-4
```

### Switch to Gemini Pro

In `application.properties`:
```properties
llm.provider=gemini
llm.model.name=gemini-1.5-pro
```

### Override for Single Request

```java
LlmRequest request = LlmRequest.builder()
    .userMessage("Question")
    .modelOverride("gpt-4-turbo")
    .build();
```

## Available Methods

### Non-Streaming
- `generate(request)` - Use default provider
- `generateWithGemini(request)` - Use Gemini
- `generateWithOpenAi(request)` - Use OpenAI

### Streaming
- `generateStream(request)` - Use default provider
- `generateWithGeminiStream(request)` - Use Gemini
- `generateWithOpenAiStream(request)` - Use OpenAI

## Response Structure

```java
LlmResponse {
    String content;              // The generated text
    LlmMetadata metadata;        // Contains:
        - modelUsed
        - provider
        - inputTokens
        - outputTokens
        - totalTokens
        - finishReason
        - responseTimeMs
    boolean success;             // Success flag
    String errorMessage;         // Error if failed
}
```

## Architecture Benefits

✅ **Easy Model Switching**: Change provider/model without code changes
✅ **Consistent Interface**: Same API for all providers
✅ **Type Safety**: Strongly typed DTOs
✅ **Metadata Tracking**: Token usage, timing, etc.
✅ **Error Handling**: Comprehensive error management
✅ **Spring Integration**: @Service with DI
✅ **Streaming Support**: Real-time responses
✅ **Flexibility**: Override settings per request

## Next Steps

1. Run `./gradlew build` to download dependencies
2. Add your API keys to `application.properties`
3. Review `AiModelsUsageExample.java` for examples
4. Read full documentation in `AI_MODELS_SERVICE_DOCUMENTATION.md`
5. Delete `AiModelsUsageExample.java` when done (optional)

## Support

For detailed documentation, see `AI_MODELS_SERVICE_DOCUMENTATION.md`

For questions about implementation, check the example file or ask!

