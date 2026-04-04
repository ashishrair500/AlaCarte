# AI Models Service Documentation

## Overview

The `AiModels` service provides a unified, flexible interface for interacting with multiple Large Language Model (LLM) providers including Google Gemini and OpenAI. It supports both streaming and non-streaming responses, with easy model switching via configuration.

## Features

- ✅ **Multi-Provider Support**: Seamlessly switch between Gemini and OpenAI
- ✅ **Streaming & Non-Streaming**: Full support for both response types
- ✅ **Configuration-Based**: Easy model switching via `application.properties`
- ✅ **Rich Metadata**: Token usage, finish reason, response time tracking
- ✅ **Flexible Parameters**: System messages, temperature, max tokens customization
- ✅ **Spring Integration**: @Service with constructor injection pattern
- ✅ **Type-Safe**: Strongly typed request/response DTOs
- ✅ **Error Handling**: Comprehensive error handling with detailed messages

## Architecture

```
llmServices/
├── AiModels.java                 # Main service class
├── AiModelsUsageExample.java     # Usage examples (can be deleted)
└── dto/
    ├── LlmRequest.java            # Request DTO with builder pattern
    ├── LlmResponse.java           # Response wrapper with metadata
    └── LlmMetadata.java           # Metadata (tokens, timing, etc.)
```

## Configuration

### 1. Update `application.properties`

```properties
# LangChain4j Gemini Configuration
langchain4j.google.ai.gemini.api-key=YOUR_GEMINI_API_KEY

# LangChain4j OpenAI Configuration
langchain4j.open-ai.api-key=YOUR_OPENAI_API_KEY

# LLM Model Configuration
llm.provider=gemini              # Options: gemini, openai
llm.model.name=gemini-1.5-flash  # or gpt-4, gpt-4-turbo, etc.
llm.temperature=0.7              # 0.0 (focused) to 2.0 (creative)
llm.max-tokens=2000              # Maximum tokens in response
```

### 2. Supported Models

#### Gemini Models
- `gemini-1.5-flash` (default, fast and cost-effective)
- `gemini-1.5-pro` (advanced capabilities)
- `gemini-2.0-flash-exp` (experimental)

#### OpenAI Models
- `gpt-4` (most capable)
- `gpt-4-turbo` (faster GPT-4)
- `gpt-4o` (optimized)
- `gpt-3.5-turbo` (cost-effective)
- `gpt-4o-mini` (lightweight)

### 3. Model Version Switching

To switch models, simply update `application.properties`:

```properties
# Switch to OpenAI GPT-4
llm.provider=openai
llm.model.name=gpt-4

# Or switch to Gemini Pro
llm.provider=gemini
llm.model.name=gemini-1.5-pro
```

No code changes required! The service will automatically use the configured model.

## Usage Examples

### Basic Usage

```java
@Service
public class MyService {
    
    private final AiModels aiModels;
    
    public MyService(AiModels aiModels) {
        this.aiModels = aiModels;
    }
    
    public String getAiResponse() {
        LlmRequest request = LlmRequest.builder()
            .userMessage("Explain quantum computing")
            .build();
            
        LlmResponse response = aiModels.generate(request);
        
        if (response.isSuccess()) {
            return response.getContent();
        }
        return "Error: " + response.getErrorMessage();
    }
}
```

### With System Message

```java
LlmRequest request = LlmRequest.builder()
    .systemMessage("You are a professional chef specializing in Italian cuisine.")
    .userMessage("What's the secret to perfect pasta carbonara?")
    .build();

LlmResponse response = aiModels.generate(request);
```

### Custom Parameters

```java
LlmRequest request = LlmRequest.builder()
    .systemMessage("You are a creative writer.")
    .userMessage("Write a short story about AI")
    .temperature(0.9)    // More creative
    .maxTokens(500)      // Limit response length
    .build();

LlmResponse response = aiModels.generate(request);

// Access metadata
System.out.println("Tokens used: " + response.getMetadata().getTotalTokens());
System.out.println("Response time: " + response.getMetadata().getResponseTimeMs() + "ms");
System.out.println("Model used: " + response.getMetadata().getModelUsed());
```

### Model Override (Per Request)

```java
// Default might be Gemini, but this request uses GPT-4
LlmRequest request = LlmRequest.builder()
    .userMessage("Explain REST APIs")
    .modelOverride("gpt-4")
    .build();

LlmResponse response = aiModels.generate(request);
```

### Explicitly Use Gemini

```java
LlmRequest request = LlmRequest.builder()
    .userMessage("What is machine learning?")
    .temperature(0.7)
    .build();

LlmResponse response = aiModels.generateWithGemini(request);
```

### Explicitly Use OpenAI

```java
LlmRequest request = LlmRequest.builder()
    .userMessage("Explain neural networks")
    .temperature(0.5)
    .build();

LlmResponse response = aiModels.generateWithOpenAi(request);
```

### Streaming Response

```java
@GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<String> streamResponse() {
    LlmRequest request = LlmRequest.builder()
        .systemMessage("You are a storyteller.")
        .userMessage("Tell me a short story")
        .temperature(0.8)
        .build();
    
    return aiModels.generateStream(request);
}
```

### Streaming with Gemini

```java
Flux<String> stream = aiModels.generateWithGeminiStream(request);

stream.subscribe(
    chunk -> System.out.print(chunk),        // Print each chunk
    error -> System.err.println(error),       // Handle errors
    () -> System.out.println("Complete!")     // On completion
);
```

### Streaming with OpenAI

```java
Flux<String> stream = aiModels.generateWithOpenAiStream(request);

// Can be returned directly to a REST controller for SSE
return stream;
```

## API Methods

### Non-Streaming Methods

| Method | Description | Returns |
|--------|-------------|---------|
| `generate(LlmRequest)` | Uses default configured provider | `LlmResponse` |
| `generateWithGemini(LlmRequest)` | Explicitly uses Gemini | `LlmResponse` |
| `generateWithOpenAi(LlmRequest)` | Explicitly uses OpenAI | `LlmResponse` |

### Streaming Methods

| Method | Description | Returns |
|--------|-------------|---------|
| `generateStream(LlmRequest)` | Streams with default provider | `Flux<String>` |
| `generateWithGeminiStream(LlmRequest)` | Streams with Gemini | `Flux<String>` |
| `generateWithOpenAiStream(LlmRequest)` | Streams with OpenAI | `Flux<String>` |

### Utility Methods

| Method | Description | Returns |
|--------|-------------|---------|
| `getDefaultProvider()` | Get configured provider | `String` |
| `getDefaultModelName()` | Get configured model | `String` |
| `getDefaultTemperature()` | Get configured temperature | `Double` |
| `getDefaultMaxTokens()` | Get configured max tokens | `Integer` |

## DTOs

### LlmRequest

```java
LlmRequest request = LlmRequest.builder()
    .systemMessage("System instructions")  // Optional
    .userMessage("User query")             // Required
    .temperature(0.7)                      // Optional (0.0 - 2.0)
    .maxTokens(1000)                       // Optional
    .modelOverride("gpt-4")                // Optional
    .build();
```

### LlmResponse

```java
LlmResponse {
    String content;              // Generated text
    LlmMetadata metadata;        // Response metadata
    boolean success;             // Success flag
    String errorMessage;         // Error message if failed
}
```

### LlmMetadata

```java
LlmMetadata {
    String modelUsed;            // Model that generated response
    String provider;             // Provider (gemini/openai)
    Integer inputTokens;         // Input token count
    Integer outputTokens;        // Output token count
    Integer totalTokens;         // Total token count
    String finishReason;         // Why generation stopped
    Long responseTimeMs;         // Response time in milliseconds
}
```

## Real-World Use Case: Restaurant Recommendation

```java
@Service
public class RestaurantRecommendationService {
    
    private final AiModels aiModels;
    
    public RestaurantRecommendationService(AiModels aiModels) {
        this.aiModels = aiModels;
    }
    
    public String getRecommendation(String mood, String preferences, String menuText) {
        LlmRequest request = LlmRequest.builder()
            .systemMessage(
                "You are a food recommendation assistant. " +
                "Analyze the menu and suggest dishes based on customer preferences."
            )
            .userMessage(String.format(
                "Menu: %s\n\nMood: %s\nPreferences: %s\n\n" +
                "Recommend 2-3 dishes.",
                menuText, mood, preferences
            ))
            .temperature(0.7)
            .maxTokens(300)
            .build();
            
        LlmResponse response = aiModels.generate(request);
        return response.isSuccess() ? 
            response.getContent() : 
            "Unable to generate recommendation";
    }
}
```

## Error Handling

```java
try {
    LlmRequest request = LlmRequest.builder()
        .userMessage("What is AI?")
        .build();
        
    LlmResponse response = aiModels.generate(request);
    
    if (response.isSuccess()) {
        // Use response.getContent()
        String content = response.getContent();
    } else {
        // Handle error
        System.err.println("Error: " + response.getErrorMessage());
    }
} catch (Exception e) {
    // Handle exception
    System.err.println("Exception: " + e.getMessage());
}
```

## Best Practices

1. **Always check `response.isSuccess()`** before using content
2. **Use system messages** to set context and behavior
3. **Adjust temperature** based on use case:
   - 0.0-0.3: Focused, deterministic (documentation, factual)
   - 0.4-0.7: Balanced (general use)
   - 0.8-1.0: Creative (stories, brainstorming)
4. **Set appropriate maxTokens** to control cost and response length
5. **Use streaming** for better user experience in real-time applications
6. **Override models per request** when you need specific model capabilities
7. **Monitor metadata** for token usage and performance optimization

## Cost Optimization

- Use `gemini-1.5-flash` for cost-effective, fast responses
- Use `gpt-3.5-turbo` for OpenAI cost savings
- Set `maxTokens` to limit response length
- Use streaming for better perceived performance
- Cache responses when appropriate

## Switching Models

### To GPT-4
```properties
llm.provider=openai
llm.model.name=gpt-4
```

### To Gemini Pro
```properties
llm.provider=gemini
llm.model.name=gemini-1.5-pro
```

### Per-Request Override
```java
request.setModelOverride("gpt-4-turbo");
```

## Testing

```java
@SpringBootTest
class AiModelsTest {
    
    @Autowired
    private AiModels aiModels;
    
    @Test
    void testGenerate() {
        LlmRequest request = LlmRequest.builder()
            .userMessage("Say hello")
            .build();
            
        LlmResponse response = aiModels.generate(request);
        
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertNotNull(response.getContent());
        assertNotNull(response.getMetadata());
    }
}
```

## Dependencies

Ensure these are in your `build.gradle`:

```gradle
implementation 'dev.langchain4j:langchain4j-google-ai-gemini:0.35.0'
implementation 'dev.langchain4j:langchain4j-open-ai:0.35.0'
implementation 'dev.langchain4j:langchain4j-spring-boot-starter:0.35.0'
implementation 'dev.langchain4j:langchain4j:0.35.0'
```

## Support

- LangChain4j Documentation: https://docs.langchain4j.dev/
- Gemini API: https://ai.google.dev/docs
- OpenAI API: https://platform.openai.com/docs

## Notes

- The `AiModelsUsageExample.java` file can be deleted after reviewing examples
- Always keep API keys secure (use environment variables in production)
- Monitor token usage for cost management
- Consider rate limits for your API keys

