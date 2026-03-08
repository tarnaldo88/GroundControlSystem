# Ground Control System

A professional Android application designed for the command, control, and monitoring of unmanned aerial vehicles. This system integrates real-time telemetry, mission planning, and data analysis tools into a unified Jetpack Compose interface.

## Key Features

*   **Real-time Telemetry:** Monitor critical flight data including battery levels, signal strength, altitude, and speed via a dedicated dashboard.
*   **Live Camera Integration:** Access high-performance video feeds using the Android CameraX library with support for video recording.
*   **GPS and Mapping:** Integrated mapping services provided by osmdroid for real-time position tracking, path visualization, and waypoint management.
*   **Mission Planning:** Interactive mission creation tool allowing users to define waypoints, altitudes, speeds, and specific actions (Hover, Photo, Land) with a comprehensive pre-flight checklist.
*   **Mission Replay:** Review previous flights with a synchronized frame-by-frame replay system featuring a data HUD overlay and path rendering.
*   **Data Export:** Export mission logs to industry-standard formats including CSV, KML, and JSON for external analysis and sharing.
*   **Night Vision Mode:** Specialized UI theme designed for low-light operational environments.
*   **Robust Navigation:** Persistent navigation rail and top command bar providing system status and Return-to-Home (RTH) control across all screens.

## Tech Stack

*   **Language:** Kotlin
*   **UI Framework:** Jetpack Compose with Material Design 3
*   **Navigation:** Jetpack Navigation Compose
*   **Camera:** CameraX (Core, Camera2, Lifecycle, View)
*   **Maps:** osmdroid-android
*   **Concurrency:** Kotlin Coroutines and Flow
*   **Architecture:** MVVM (Model-View-ViewModel)
*   **Testing:** JUnit 4, Mockito, Espresso, Compose UI Testing

## Project Structure

The project follows a modular Android architecture:

*   **ui.screens:** Contains the Compose implementations for functional areas (Dashboard, Camera, Replay, Mission Plan, etc.).
*   **ui.components:** Reusable UI elements such as the Navigation Rail, Top Command Bar, and Telemetry Cards.
*   **ui.navigation:** Management of the application's navigation graph and route definitions.
*   **ui.viewmodel:** Business logic, state management, and UDP telemetry parsing.
*   **ui.theme:** Application-wide styling including the specialized Night Vision implementation.

## Testing

The application includes comprehensive test suites covering both business logic and user interface interactions.

### Unit Tests
Located in `app/src/test/java/`, these tests verify the core logic of the `TelemetryViewModel`.
*   **State Management:** Verifies initial states and state transitions for battery, connection, and missions.
*   **Mission Logic:** Validates waypoint handling and mission lifecycle.
*   **Logging:** Ensures system and mission logs are correctly generated.

### Instrumented UI Tests
Located in `app/src/androidTest/java/`, these tests run on physical devices or emulators to verify UI behavior.
*   **Dashboard Screen:** Validates telemetry displays, connection toggles, and post-flight report interactions.
*   **Mission Plan Screen:** Verifies waypoint list rendering and execution button constraints.

### Running Tests
To execute the test suites, use the following Gradle commands:

*   Run Unit Tests: `./gradlew :app:testDebugUnitTest`
*   Run UI Tests: `./gradlew :app:connectedDebugAndroidTest`

## Getting Started

### Prerequisites

*   Android Studio Ladybug or newer
*   Android SDK 35
*   JDK 17 or higher

### Installation

1.  Clone the repository to your local machine.
2.  Open the project in Android Studio.
3.  Allow Gradle to synchronize and download necessary dependencies.
4.  Build the project using `./gradlew assembleDebug`.
5.  Deploy to a physical Android device or emulator.

## License

This project is licensed under the MIT License.
