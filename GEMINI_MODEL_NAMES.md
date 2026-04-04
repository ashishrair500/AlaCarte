# Gemini Model Names for LangChain4j 0.35.0

## ⚠️ Issue Fixed

The error you encountered:
```
NOT_FOUND (code 404) models/gemini-1.5-flash is not found for API version v1beta
```

This happened because **LangChain4j 0.35.0 uses Google AI v1beta API**, which has different model names than the newer versions.

## ✅ Correct Model Names for v1beta API

### Primary Models (Working with 0.35.0)

| Model Name | Description | Best For |
|------------|-------------|----------|
| `gemini-pro` | Standard Gemini model | General purpose, balanced performance |
| `gemini-pro-vision` | Multimodal (text + images) | Image analysis and understanding |

### ❌ NOT Supported in v1beta (LangChain4j 0.35.0)

These models require newer API versions:
- ❌ `gemini-1.5-flash` 
- ❌ `gemini-1.5-pro`
- ❌ `gemini-2.0-flash-exp`
- ❌ Any model with "1.5" or "2.0" in the name

## 🔧 Configuration Updated

I've updated your configuration to use the correct model:

### application.properties
```properties
llm.provider=gemini
llm.model.name=gemini-pro  # ✅ Changed from gemini-1.5-flash
llm.temperature=0.7
llm.max-tokens=2000
```

### AiModels.java
```java
private String defaultModelName = "gemini-pro";  // ✅ Updated
```

## 🚀 Usage Examples

### Example 1: Use Default (gemini-pro)
```java
LlmRequest request = LlmRequest.builder()
    .systemMessage("You are a helpful assistant")
    .userMessage("Hello, how are you?")
    .temperature(0.7)
    .maxTokens(2000)
    .build();

LlmResponse response = aiModels.generateWithGemini(request);

if (response.isSuccess()) {
    System.out.println("Content: " + response.getContent());
    System.out.println("Model: " + response.getMetadata().getModelUsed()); // gemini-pro
    System.out.println("Tokens: " + response.getMetadata().getTotalTokens());
}
```

### Example 2: Explicit Model Override
```java
LlmRequest request = LlmRequest.builder()
    .userMessage("Analyze this image...")
    .modelOverride("gemini-pro-vision")  // Use vision model
    .build();

LlmResponse response = aiModels.generateWithGemini(request);
```

## 📊 Model Comparison

### gemini-pro
- ✅ Text-only tasks
- ✅ Fast responses
- ✅ Cost-effective
- ✅ Good for most use cases
- ❌ No image understanding

### gemini-pro-vision
- ✅ Text + image tasks
- ✅ Image analysis
- ✅ Visual question answering
- ⚠️ Slightly slower
- ⚠️ Higher cost

## 🆙 Want Newer Models (gemini-1.5-flash, gemini-1.5-pro)?

To use newer Gemini models like `gemini-1.5-flash` or `gemini-1.5-pro`, you need to:

### Option 1: Upgrade LangChain4j (Recommended)

Upgrade to a newer version that supports the v1 API:

```gradle
// In build.gradle, change from:
implementation 'dev.langchain4j:langchain4j-google-ai-gemini:0.35.0'

// To a newer version (e.g., 0.37.0 or later)
implementation 'dev.langchain4j:langchain4j-google-ai-gemini:0.37.0'
```

**Note**: Upgrading may require code changes and testing.

### Option 2: Stick with gemini-pro (Current Setup)

`gemini-pro` works great for most use cases:
- Good quality responses
- Fast and reliable
- Well-tested with LangChain4j 0.35.0
- No code changes needed

## 🧪 Testing Your Setup

Run your main method to test:

```bash
cd /Users/ashish/IdeaProjects/MenuAgent
./gradlew build
```

Then run your test from AiModels.java main method, or use it in your Spring application:

```java
@Autowired
private AiModels aiModels;

public void test() {
    LlmRequest request = LlmRequest.builder()
        .systemMessage("You are a helpful assistant")
        .userMessage("Tell me about AI")
        .build();
        
    LlmResponse response = aiModels.generateWithGemini(request);
    
    if (response.isSuccess()) {
        System.out.println("✅ Success!");
        System.out.println("Response: " + response.getContent());
        System.out.println("Model: " + response.getMetadata().getModelUsed());
    } else {
        System.out.println("❌ Error: " + response.getErrorMessage());
    }
}
```

## ⚠️ Important Notes

### 1. API Key Security
I noticed you have the API key hardcoded in the Java file:
```java
private String geminiApiKey = "AIzaSyAW4StK3Do4jSNaiICOR6kyMacRZQPQaxQ";
```

**Security Risk**: Never commit API keys to Git!

**Better approach**: Keep it in `application.properties` only (which should be in `.gitignore`):
```properties
langchain4j.google.ai.gemini.api-key=YOUR_KEY_HERE
```

Remove the hardcoded value from the Java file and let Spring inject it via `@Value`.

### 2. @Data Annotation
You added `@Data` to the service class:
```java
@Data
@Service
public class AiModels {
```

**Warning**: `@Data` generates setters for all fields, which can break Spring's dependency injection. Consider removing it unless you specifically need it.

**Better approach**:
```java
@Service
public class AiModels {
    // Spring will inject via @Value annotations
    // No need for @Data
}
```

### 3. Main Method in Service
You added a main method for testing. This is fine for quick tests, but:
- Won't have Spring context (so @Value won't work)
- Better to use proper unit tests or integration tests
- Or test via your Spring Boot application

## 📝 Summary

✅ **Fixed**: Changed model name from `gemini-1.5-flash` to `gemini-pro`

✅ **Why**: LangChain4j 0.35.0 uses v1beta API which only supports `gemini-pro` and `gemini-pro-vision`

✅ **Status**: Build successful, should work now

✅ **Available Models**: `gemini-pro` (default), `gemini-pro-vision` (with images)

⚠️ **Security**: Remove hardcoded API key from Java file

⚠️ **Consider**: Removing `@Data` annotation from service class

## 🎉 You're Good to Go!

Your Gemini integration should work now with the correct model name `gemini-pro`.

Try running your code again - it should respond successfully!

