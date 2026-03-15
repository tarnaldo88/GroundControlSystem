# Ground Control System (GCS)

An advanced Android application engineered for the professional command, control, and real-time monitoring of Unmanned Aerial Vehicles (UAVs). This project demonstrates a high-performance, safety-critical mobile interface built using modern Android development standards.

## 🚀 Architectural Overview

The GCS follows the **MVVM (Model-View-ViewModel)** architectural pattern, ensuring a clean separation of concerns and a reactive data flow.

*   **View Layer (Jetpack Compose):** A fully declarative UI with Material 3 components. It reacts to state changes emitted by the ViewModel.
*   **ViewModel (TelemetryViewModel):** Orchestrates the business logic, manages the asynchronous telemetry stream using **Kotlin Coroutines and StateFlow/MutableState**, and handles state persistence for multi-drone operations.
*   **State Management:** Utilizes Compose's `mutableStateOf` and `mutableStateListOf` for efficient, thread-safe UI updates in a high-frequency telemetry environment.

## 🛠 Tech Stack & Key Libraries

*   **Language:** Kotlin (100%)
*   **UI Framework:** Jetpack Compose (Material 3)
*   **Navigation:** Navigation Compose (Single Activity Architecture)
*   **Mapping:** [osmdroid](https://github.com/osmdroid/osmdroid) for offline-capable, highly customizable geospatial visualization.
*   **Camera:** [CameraX](https://developer.android.com/training/camerax) (Camera2 integration) for low-latency video feeds and media capture.
*   **Networking:** Retrofit 2 for Weather API integration; DatagramSocket (UDP) simulation for telemetry.
*   **Asynchrony:** Kotlin Coroutines & Flow for non-blocking I/O and periodic background tasks (weather updates, telemetry simulation).
*   **Notifications:** Text-To-Speech (TTS) for eyes-free safety alerts.

## 🌟 Detailed Feature Breakdown

### 1. Real-Time Telemetry & HUD
*   **Dynamic Dashboard:** High-frequency updates for Airspeed (KM/H), Altitude (M), Battery (%), and Signal Strength.
*   **Visual HUD:** Overlay on camera feed providing orientation, heading, and critical flight vectors using the `Canvas` API for high-performance drawing.
*   **Safety Alerts:** Intelligent monitoring for low battery, high wind speeds (>15m/s), and restricted airspace proximity with both visual and audio (TTS) cues.

### 2. Advanced Mapping & Geospatial Intelligence
*   **Multi-Layer Support:** Toggle between Mapnik, Topographic, and Satellite imagery providers.
*   **No-Fly Zone (NFZ) Management:** Real-time distance calculation using the Haversine formula to detect proximity to restricted airports or government zones.
*   **Offline Capability:** `CacheManager` integration allows operators to pre-download map tiles for remote locations without cellular connectivity.

### 3. Professional Mission Planning
*   **Waypoint Sequencing:** Interactive map-based waypoint placement with per-point configuration:
    *   **Actions:** Navigate, Hover, Take Photo, Land.
    *   **Parameters:** Targeted altitude and cruising speed for each leg of the flight.
*   **Pre-Flight Logic:** Automated safety checklist verification before mission upload.
*   **Return-to-Home (RTH):** One-tap automated recovery sequence.

### 4. Computer Vision & Media (CameraX)
*   **Object Tracking UI:** Custom `pointerInput` gestures allow users to drag-to-select objects on the live feed, simulating target lock-on.
*   **HUD Rendering:** Real-time Attitude Indicator (Artificial Horizon) and Vertical Tape gauges rendered directly on the video preview.

### 5. Multi-Drone Fleet Management
*   **Scalable Architecture:** The `TelemetryViewModel` supports multiple `DroneState` objects in a `mutableStateMapOf`, allowing seamless switching between different aircraft in the field.

### 6. Mission Replay & Analytics
*   **Telemetry Replay:** Frame-accurate replay of historical flight data synced with GPS path visualization.
*   **Data Trends:** Interactive charts (Altitude/Speed vs Time) using custom Compose Canvas drawing with touch-based value inspection.

## 📂 Project Structure

```
com.example.groundcontrolsystem
├── ui
│   ├── screens
│   │   ├── dashboard      # Telemetry overview & active drone status
│   │   ├── camera         # Video feed, HUD overlay, and Object Tracking
│   │   ├── missionPlan    # Interactive waypoint planning and NFZ display
│   │   ├── gps            # Full-screen map tracking and path history
│   │   ├── statistics     # Historical data analysis & trend charts
│   │   ├── replay         # Post-mission flight review system
│   │   └── settings       # App configurations & fleet management
│   ├── components         # Reusable UI (Navigation Rail, Top Bar, Gauges)
│   ├── navigation         # Route definitions and NavHost configuration
│   ├── viewmodel          # TelemetryViewModel (The heart of the app)
│   └── theme              # M3 Color schemes (including Night Vision)
└── model                  # Data classes (Waypoint, DroneState, SystemLog)
```

## 🧪 Testing Strategy

*   **Unit Testing (JUnit/Mockito):** Focused on the `TelemetryViewModel`. Tests include flight simulation logic, distance calculations, and state transition validation.
*   **UI Testing (Espresso/Compose):** End-to-end testing of critical user flows:
    *   Waypoint addition and mission start sequence.
    *   Dashboard responsiveness to simulated telemetry shifts.

## 🏁 How to Run

1.  **Clone:** `git clone https://github.com/your-repo/GroundControlSystem.git`
2.  **SDK:** Requires Android SDK 35 (VanillaIceCream).
3.  **Permissions:** Ensure Camera and Location permissions are granted.
4.  **Weather API:** (Optional) Add your OpenWeatherMap key in `TelemetryViewModel.kt` to enable live wind monitoring.

---
*Developed for Android Dev Practice - High-Reliability UAV Control Interface.*
