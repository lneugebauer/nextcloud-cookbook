# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Nextcloud Cookbook Android client is a Kotlin-based Android app using Jetpack Compose for UI. The app provides a mobile interface for the Nextcloud Cookbook server app, allowing users to manage and view recipes.

## Development Commands

### Build
- `./gradlew build` - Build the entire project
- `./gradlew assembleDebug` - Build debug APK
- `./gradlew assembleRelease` - Build release APK

### Testing
- `./gradlew test` - Run unit tests
- `./gradlew connectedAndroidTest` - Run instrumented tests
- `./gradlew testDebugUnitTest` - Run debug unit tests

### Code Quality
- `./gradlew ktlintCheck` - Check Kotlin code style
- `./gradlew ktlintFormat` - Format Kotlin code
- `./gradlew lint` - Run Android lint checks

### Documentation
- `npm run docs:dev` - Start VitePress docs development server
- `npm run docs:build` - Build documentation
- `npm run docs:preview` - Preview built documentation

### Clean
- `./gradlew clean` - Clean build artifacts

## Architecture

The app follows Clean Architecture principles with three main layers:

### Presentation Layer (`presentation/`)
- **ViewModels**: Handle UI state and business logic
- **Screens**: Jetpack Compose UI components
- **Components**: Reusable UI components in `core/presentation/components/`

### Domain Layer (`domain/`)
- **Models**: Data models representing business entities
- **Repositories**: Interface definitions for data access
- **Use Cases**: Business logic operations
- **State**: UI state data classes

### Data Layer (`data/`)
- **DTOs**: Data transfer objects for API communication
- **Remote**: Network API definitions and interceptors
- **Repository Implementations**: Concrete repository implementations

### Key Modules
- **Auth**: Authentication and login functionality
- **Recipe**: Core recipe management (CRUD operations, formatting, yield calculation)
- **Category**: Recipe categorization
- **Core**: Shared utilities, preferences, and base components
- **Settings**: App configuration and library information

## Technology Stack

- **UI Framework**: Jetpack Compose with Material 3
- **Navigation**: Compose Destinations
- **Dependency Injection**: Hilt
- **Networking**: Retrofit + OkHttp
- **Image Loading**: Coil
- **Data Storage**: DataStore (Preferences)
- **State Management**: Store5 library
- **Build Flavors**: `full` (F-Droid/GitHub) and `googlePlay`

## Code Style

- Uses ktlint for Kotlin code formatting
- Follow existing patterns in the codebase
- Use Hilt for dependency injection
- Prefer Jetpack Compose over XML layouts
- Use sealed classes for state management

## Testing Strategy

- Unit tests in `app/src/test/` for business logic
- Instrumented tests in `app/src/androidTest/` for UI and integration
- Screenshot testing with Fastlane Screengrab
- Test classes follow naming pattern: `*UnitTest.kt` and `*Test.kt`

## Important Files

- `app/build.gradle` - Main build configuration with dependencies
- `build.gradle` - Project-level build configuration with version definitions
- `app/src/main/AndroidManifest.xml` - App permissions and components
- `app/lint.xml` - Lint configuration
- `fastlane/` - App store deployment and screenshot automation