# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Myanmar Teacher's AI Assistant - Android app helping teachers manage schedules, student data, and admin tasks using AI (OpenAI/Gemini or local Ollama).

## Build & Run Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run unit tests
./gradlew test

# Run single test class
./gradlew test --tests "com.sayar.assistant.ClassName"

# Run Android instrumented tests
./gradlew connectedAndroidTest

# Lint check
./gradlew lint

# Clean build
./gradlew clean
```

## Architecture

### Layer Structure (Clean Architecture)

```
app/src/main/java/com/sayar/assistant/
├── data/
│   ├── local/          # Room database, DAOs
│   ├── remote/         # Retrofit services (OpenAI, Ollama)
│   └── repository/     # Repository implementations
├── domain/
│   ├── model/          # Domain entities (User, Timetable, Student)
│   ├── repository/     # Repository interfaces
│   └── usecase/        # Use cases
├── presentation/
│   ├── MainActivity.kt
│   ├── navigation/     # SayarNavHost with route definitions
│   ├── ui/
│   │   ├── screens/    # LoginScreen, HomeScreen, etc.
│   │   ├── components/ # Reusable UI components
│   │   └── theme/      # Material3 Color, Type, Theme
│   └── viewmodel/      # Hilt ViewModels
├── di/                 # Hilt modules (AppModule, NetworkModule)
└── util/
```

### Key Patterns

**Auth Flow**: Google Identity Services (Credential Manager) → ID token decoded → User stored in DataStore → AuthRepository exposes Flow<User?>

**AI Provider Switching**: NetworkModule provides qualified Retrofit instances (@OpenAIRetrofit, @OllamaRetrofit). Settings stored in DataStore.

**Navigation**: Type-safe routes in `Routes` object. NavHost in `SayarNavHost.kt`.

### Tech Stack

- UI: Jetpack Compose + Material3
- DI: Hilt
- Network: Retrofit + Kotlinx Serialization
- Local: Room + DataStore Preferences
- Auth: Google Identity Services (androidx.credentials)
- OCR: ML Kit Text Recognition
- Async: Coroutines + Flow

## API Endpoints

**OpenAI**: `POST https://api.openai.com/v1/chat/completions`

**Ollama**: `POST http://<local_ip>:11434/api/generate`

**Google Drive**: Scopes `drive.file`, `drive.appdata`

## Setup Requirements

1. Set Google OAuth Web Client ID in `res/values/strings.xml`:
   ```xml
   <string name="default_web_client_id">YOUR_CLIENT_ID.apps.googleusercontent.com</string>
   ```

2. Configure Google Cloud Console with Drive API enabled

## Localization

- English: `res/values/strings.xml`
- Myanmar (Burmese): `res/values-my/strings.xml`

Use Unicode-compliant rendering for Myanmar script.
