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

### D. No-Fly Zone (NFZ) Warning System
The ViewModel maintains a list of `NoFlyZone` objects and constantly monitors the drone's proximity to these areas.
*   **Haversine Formula:** Proximity is calculated using the Haversine formula to determine the distance between two GPS coordinates in meters.
*   **Proactive Alerting:** If the drone is within 500m of a restricted zone, the system triggers both a TTS alert and a UI warning state (`isNearNfz`).

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
*   **NFZ Visualization:** `Polygon` overlays are used to draw restricted areas directly on the map.
*   **Lifecycle Management:** Since `osmdroid` is View-based, it uses `DisposableEffect` to call `mapView.onDetach()` to prevent memory leaks and "map shutdown" errors in Compose.

### Camera (CameraX) & Advanced HUD
The `CameraScreen` leverages the **CameraX** library with a high-performance `Canvas` overlay.
*   **Vertical Tapes:** Speed and Altitude are displayed using "scrolling tapes" animated via `animateFloatAsState`. This provides a standard aviation interface familiar to drone pilots.
*   **Artificial Horizon:** A central attitude indicator represents the drone's spatial orientation.

### Replay System
The `ReplayScreen` iterates through `MissionLog` objects.
*   **How it works:** It uses a `LaunchedEffect` loop with a variable `delay` (playback speed) to increment the current frame index. This simulates the flight path on the map while updating a HUD overlay.

---

## 5. Testing Strategy

### Unit Testing (`app/src/test`)
Focuses on the `TelemetryViewModel`.
*   **Main Dispatcher Mocking:** Since the ViewModel uses `viewModelScope`, we use `kotlinx-coroutines-test` to swap the Main dispatcher for a `TestDispatcher`.
*   **InstantTaskExecutorRule:** Used to ensure that background tasks happen immediately during tests.

### UI Testing (`app/src/androidTest`)
Focuses on user interaction and state-dependent UI.
*   **Real Context:** Unlike unit tests, UI tests use `ApplicationProvider.getApplicationContext()` to ensure Android system services (like the ContentResolver) are available for the components.

---

## 6. Key Implementation Decisions

*   **JSON Logging:** Mission logs are stored as data classes but exported as JSON for compatibility with external analysis tools.
*   **Night Vision Mode:** A custom theme wrapper that shifts the palette to high-contrast greens and blacks to preserve the operator's night vision.
*   **Internal Map Cache:** To support modern Android (API 30+) and emulators, the map cache is redirected to `filesDir`, bypassing complex external storage permission issues.
