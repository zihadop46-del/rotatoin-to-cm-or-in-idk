# RotaryMeasure 📐⚙️

**RotaryMeasure** is a high-precision engineering and machining Android application designed to convert rotational motion into exact linear distances (and vice versa). Built for machinists, CNC operators, 3D printing enthusiasts, and mechanical engineers, it bridges the gap between angular rotation and physical movement across multiple mechanical systems.

---

## 🚀 Key Capabilities & Architecture

### 1. Precision Multi-Mechanism Conversion Engine
The core physics and math engine calculates exact values for four major mechanical movement profiles:
*   **Wheels, Drums & Rollers:** Computes standard circumference rollout (C = π ⋅ d).
*   **Lead Screws & Threaded Rods:** Calculates linear axial travel based on pitch and starts. Includes factory presets for:
    *   3D printer T8x8, CNC T8x4, T8x2 lead screws.
    *   Metric bolts (M8x1.25, M6x1.0).
    *   Imperial UNC threads (1/4"-20 / 20 TPI).
*   **Timing Belts & Pulleys:** Computes high-precision travel for standard **GT2** (16T, 20T, 36T) and **GT3** timing pulleys (\(Travel = Pitch \cdot Teeth \cdot \frac{Degrees}{360}\)).
*   **Circular Arcs:** Computes exact arc lengths along a given curvature radius (s = r ⋅ θ).

### 2. High-Precision Multi-Unit Readouts
*   **Bidirectional Conversion:** Input rotational degrees to calculate target linear distance, or input a target distance to find the exact required rotation angle.
*   **Metric System:** Centimeters (cm), Millimeters (mm), Micrometers (μ m), and Meters (m).
*   **Imperial System:** Decimal Inches (in), Thousandths of an inch / Mils (mil), Feet (ft), and Fractional Inches (e.g., 1/64", 1/32", 1/16") complete with **residual error tracking** for ultra-precise machining.
*   **Utility:** One-tap clipboard copy functionality implemented across all measurement output blocks.

### 3. Interactive Precision Rotary Dial (UI/UX)
*   **Touch-Draggable Dial:** Custom view featuring radial graduation ticks and a sweep indicator that tracks physical drag gestures.
*   **Vernier Step Buttons:** Dedicated buttons for fine-tuning adjustment (+0.01°, -0.01°, +0.1°, -0.1°) alongside a quick zero-reset option.
*   **Tactile Haptics:** Generates subtle haptic feedback intervals as the user sweeps across rotational tick marks to simulate a physical dial indicator.

### 4. Live Digital Gyro Protractor
*   **Sensor Fusion:** Leverages the Android hardware rotation vector and gyroscope sensors to measure physical, real-time rotation when the mobile device is resting on a lathe chuck, rotary table, or wheel.
*   **Control Toolkit:** Features a `Tare / Zero 0.00°` calibrator, a `Hold / Freeze Angle` toggle for awkward visual placements, and a `Send to Main Dial` pipeline.

### 5. CNC & Stepper Motion Resolution
*   **Calibration Calculator:** Directly computes steps/mm, steps/cm, steps/inch, and microns per microstep.
*   **Motor Configurations:** Supports standard 1.8° (200 steps/rev) and 0.9° (400 steps/rev) stepper motors.
*   **Driver Configuration:** Accounts for microstepping indexes from full-step (1/1) up to 1/256 microstepping.
*   **Pitch Tool:** Features a built-in machinist TPI (Threads Per Inch)-to-pitch converter.

### 6. Room Database Persistence Layer
*   **Measurement Logs:** Locally archives measurement passes including custom user notes, timestamps, mechanism configurations, and calculated distances.
*   **Cumulative Odometer:** Automatically tracks total accumulated linear distance across multiple compound steps or shifts.
*   **Custom Presets:** Let users create, save, and manage custom profiles for non-standard wheels, proprietary lead screw pitches, or specific arc curvatures.

---

## 🛠 Tech Stack

*   **Platform:** Android (API 26+)
*   **Language:** Kotlin
*   **UI Framework:** Jetpack Compose (for the state-driven Dial and Protractor UI elements)
*   **Architecture:** MVVM (Model-View-ViewModel) with Clean Architecture patterns
*   **Local Storage:** Room Database (SQLite abstraction)
*   **Hardware Integration:** Android `SensorManager` (Rotation Vector / Gyroscope) & `Vibrator` API (Haptic Feedback)

---

## 📊 Conversion Formulas Implemented

| Mechanism Type | Core Equation Implemented |
| :--- | :--- |
| **Wheel Rollout** | \(\text{Distance} = (\pi \times D) \times (\frac{\theta}{360})\) |
| **Lead Screw Travel** | \(\text{Distance} = \text{Lead} \times (\frac{\theta}{360})\) where Lead = Pitch × Starts |
| **Belt Pulley Travel** | \(\text{Distance} = (\text{Teeth} \times \text{Pitch}) \times (\frac{\theta}{360})\) |
| **Arc Curvature** | \(\text{Arc Length} = R \times (\theta \times \frac{\pi}{180})\) |

---

## 🏗 Installation & Setup

1. Clone this repository to your local machine:
   ```bash
   git clone https://github.com
   ```
2. Open the project folder in **Android Studio (Ladybug or newer)**.
3. Sync the project with the Gradle files.
4. Select an active emulator configuration or connect a physical Android device (recommended for testing the Live Gyro Protractor).
5. Press `Run` (`Shift + F10`). All tests and builds pass out-of-the-box in the emulator preview environment.

