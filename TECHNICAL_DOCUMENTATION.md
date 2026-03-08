# Technical Documentation: Ground Control System (GCS)

This document provides an in-depth explanation of the application's architecture, feature implementation, and the reasoning behind specific design decisions. It is intended for developers and stakeholders to understand the codebase and the "why" behind the code.

---

## 1. Architectural Overview

The application follows the **MVVM (Model-View-ViewModel)** architectural pattern, combined with **Unidirectional Data Flow (UDF)** principles.

### Why MVVM?
*   **Separation of Concerns:** By decoupling UI logic (Compose) from business logic (ViewModel), the app becomes easier to test and maintain.
*   **Lifecycle Awareness:** The `TelemetryViewModel` survives configuration changes (like screen rotations), ensuring that critical drone telemetry and mission states are not lost.
*   **Reactive UI:** Jetpack Compose observes the `State` objects in the ViewModel, automatically re-composing only the parts of the UI that need to change when telemetry data arrives.

---

## 2. The Core Engine: `TelemetryViewModel`

The `TelemetryViewModel` is the "brain" of the application. It handles three primary responsibilities:

### A. Telemetry Acquisition (UDP & Simulation)
The drone sends data via UDP packets (simulating industry standards like MAVLink). 
*   **UDP Listener:** Uses a `DatagramSocket` running in a `viewModelScope` on `Dispatchers.IO`. This prevents blocking the main thread while waiting for network packets.
*   **Simulated Flight:** To allow for development without hardware, a `simulateFlight()` method calculates smooth transitions between coordinates using linear interpolation (`lerp` style logic).

### B. Mission State Management
The ViewModel manages the `activeWaypoints` and `currentWaypointIndex`.
*   **Reasoning:** Keeping the mission state in the ViewModel allows the `MissionPlanScreen` to define the mission, while the `DashboardScreen` or `GpsScreen` can simultaneously monitor progress.

### C. Safety & Feedback (TTS)
The system uses **Text-To-Speech (TTS)** for critical alerts (e.g., "Low Battery").
*   **Why TTS?** In a real-world GCS scenario, the operator's eyes are often on the drone or the video feed. Audio alerts provide a secondary channel for critical safety information without requiring the user to look at specific telemetry numbers.

---

## 3. Navigation and UI Layout

### `AppShell` and `LeftNavRail`
The UI uses a persistent **Navigation Rail** on the left and a **Top Command Bar**.
*   **Reasoning:** This layout mimics professional tactical displays. It maximizes vertical space for map and camera views while providing high-level system status (Connectivity, RTH, Battery) regardless of which screen is active.

### State Hoisting
Navigation is handled by `AppNavHost`. The `navController` and `telemetryViewModel` are hoisted to the `AppShell` level.
*   **Why?** This allows the `AppShell` to pass the same instance of the ViewModel to every screen, ensuring a "Single Source of Truth."

---

## 4. Feature Implementation Details

### Mapping (osmdroid)
The app uses **osmdroid** instead of Google Maps.
*   **Reasoning:** `osmdroid` is open-source and supports offline tile providers and custom tile sources (like OpenTopoMap), which are critical for drone operations in remote areas with limited internet connectivity.
*   **Integration:** Since `osmdroid` is a traditional View-based library, it is integrated into Compose using `AndroidView`.

### Camera (CameraX)
The `CameraScreen` leverages the **CameraX** Jetpack library.
*   **Reasoning:** CameraX handles the complexities of different device hardware and aspect ratios automatically. It provides a lifecycle-aware `PreviewView`, ensuring the camera is released immediately when the user navigates away to save battery.

### Replay System
The `ReplayScreen` iterates through `MissionLog` objects.
*   **How it works:** It uses a `LaunchedEffect` loop with a variable `delay` (playback speed) to increment the current frame index. This simulates the flight path on the map while updating a HUD overlay.

---

## 5. Testing Strategy

### Unit Testing (`app/src/test`)
Focuses on the `TelemetryViewModel`.
*   **Main Dispatcher Mocking:** Since the ViewModel uses `viewModelScope`, we use `kotlinx-coroutines-test` to swap the Main dispatcher for a `TestDispatcher`. This allows us to test asynchronous logic synchronously.
*   **InstantTaskExecutorRule:** Used to ensure that background tasks (like updating Compose State) happen immediately during tests.

### UI Testing (`app/src/androidTest`)
Focuses on user interaction and state-dependent UI.
*   **ComposeTestRule:** Used to find nodes by text or content description and perform clicks.
*   **Mocking Context:** We use `ApplicationProvider.getApplicationContext()` because the `TelemetryViewModel` requires an `Application` instance to initialize the TTS engine.

---

## 6. Key Implementation Decisions

*   **JSON Logging:** Mission logs are stored as a list of data classes but exported as JSON. JSON was chosen for its balance of readability and compatibility with web-based analysis tools.
*   **Night Vision Mode:** Implemented as a custom `MaterialTheme` wrapper. It doesn't just "turn things dark"; it shifts the color palette towards high-contrast greens and blacks to minimize eye strain and light pollution during night operations.
*   **Pre-Flight Checklist:** This is a hard-coded safety gate. The "Launch" button is disabled in the UI unless the ViewModel reports `isConnected` and the user manually checks the NFZ (No-Fly Zone) and Battery requirements. This enforces "Safety First" coding.
