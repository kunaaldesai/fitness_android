# FitnessTracker (Android)

A Jetpack Compose Android app for tracking workouts, exercises, and workout plans. The app uses a repository + ViewModel layer to orchestrate API calls and UI state, with a single-screen Compose navigation shell that renders the main experiences (home, explore, workout creation, workout detail, history, etc.).

## Folder Structure

```
FitnessTracker/
├─ app/
│  ├─ src/
│  │  ├─ main/
│  │  │  ├─ AndroidManifest.xml
│  │  │  ├─ java/com/example/fitnesstracker/
│  │  │  │  ├─ AppConfig.kt                # App constants (base URLs, defaults)
│  │  │  │  ├─ MainActivity.kt             # App entry point
│  │  │  │  ├─ data/
│  │  │  │  │  ├─ FitnessRepository.kt     # API orchestration + business logic
│  │  │  │  │  └─ remote/
│  │  │  │  │     ├─ ApiModels.kt          # API models + request/response DTOs
│  │  │  │  │     ├─ NetworkModule.kt      # Retrofit/OkHttp wiring
│  │  │  │  │     ├─ UsersApi.kt           # User endpoints
│  │  │  │  │     └─ WorkoutsApi.kt        # Workout endpoints
│  │  │  │  └─ ui/
│  │  │  │     ├─ FitnessScreen.kt
│  │  │  │     ├─ ProfileScreen.kt         # Compose UI + navigation shell
│  │  │  │     ├─ MainViewModel.kt         # UI state + actions
│  │  │  │     ├─ SharedComponents.kt      # Reusable UI components
│  │  │  │     └─ theme/                   # Compose theme + colors + typography
│  │  │  └─ res/                           # Android resources (strings, icons, etc.)
│  │  ├─ test/                             # JVM unit tests (JUnit/MockK)
│  │  └─ androidTest/                      # Instrumented + Compose UI tests
│  └─ build.gradle.kts
├─ build.gradle.kts
├─ gradle/
│  └─ libs.versions.toml                   # Centralized dependency versions
├─ gradlew / gradlew.bat
├─ settings.gradle.kts
├─ users.py                    # Backend: User endpoints
├─ workouts.py                 # Backend: Workout endpoints
└─ workouts_helpers.py         # Backend: Helper logic
```

## Setup

1. Open the project in Android Studio.
2. Ensure the Android SDK is installed (SDK 36 / Android 14+).
3. Sync Gradle.
4. Run the app from `MainActivity` or via the default run configuration.

Optional (CLI):

```
./gradlew test
```

For UI tests (requires a running emulator or device):

```
./gradlew connectedAndroidTest
```
