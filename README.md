# Ground Control System

A professional Android application designed for the command, control, and monitoring of unmanned aerial vehicles. This system integrates real-time telemetry, mission planning, and data analysis tools into a unified Jetpack Compose interface.

## Key Features

*   **Real-time Telemetry:** Monitor critical flight data including altitude, speed, and system status through a dedicated dashboard.
*   **Live Camera Integration:** Access high-performance video feeds using the Android CameraX library.
*   **GPS and Mapping:** Integrated mapping services provided by osmdroid for real-time position tracking and path visualization.
*   **Mission Replay:** Review previous flights with a frame-by-frame replay system featuring data HUD overlays.
*   **Data Export:** Export mission logs to industry-standard formats including CSV and KML for external analysis.
*   **Mission Planning:** Define and manage flight paths and mission parameters.
*   **Night Vision Mode:** Specialized UI theme designed for low-light operational environments.

## Tech Stack

*   **Language:** Kotlin
*   **UI Framework:** Jetpack Compose with Material Design 3
*   **Navigation:** Jetpack Navigation Compose
*   **Camera:** CameraX (Core, Camera2, Lifecycle, View)
*   **Maps:** osmdroid-android
*   **Concurrency:** Kotlin Coroutines and Flow
*   **Architecture:** MVVM (Model-View-ViewModel)

## Project Structure

The project follows a modular Android architecture:

*   **ui.screens:** Contains the Compose implementations for each functional area (Dashboard, Camera, Replay, etc.).
*   **ui.components:** Reusable UI elements such as the Navigation Rail and Top Command Bar.
*   **ui.navigation:** Management of the application's navigation graph and route definitions.
*   **ui.viewmodel:** Business logic and state management for telemetry and logs.
*   **ui.theme:** Application-wide styling including the Night Vision implementation.

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

## Usage

Navigate through the system using the Left Navigation Rail. The Top Command Bar provides persistent status updates regarding system connectivity and Return-to-Home (RTH) status. For post-flight analysis, use the Replay tab to visualize flight paths and export data for further review.

## License

This project is licensed under the MIT License.
