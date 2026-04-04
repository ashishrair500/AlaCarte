#!/bin/bash

# Set Java 21
export JAVA_HOME=$(/usr/libexec/java_home -v 21)

# Check if GEMINI_API_KEY is set
if [ -z "$GEMINI_API_KEY" ]; then
    echo "⚠️  WARNING: GEMINI_API_KEY is not set!"
    echo "Please set it with: export GEMINI_API_KEY=your-api-key"
    echo ""
    echo "Or set it in application.properties (not recommended for production)"
    read -p "Do you want to continue anyway? (y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi

echo "🚀 Starting FoodByMood Agent..."
echo "Using Java: $JAVA_HOME"
echo ""

# Build and run
./gradlew bootRun --no-daemon

