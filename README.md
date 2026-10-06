# Rotational-to-Linear Precision Measurement Converter

A lightweight, high-precision developer utility and calculator that converts **rotational degrees into linear measurements (centimeters or inches)**. Designed for mechanical engineers, robotics hobbyists, 3D printing enthusiasts, and machinists who require exact linear travel calculations based on a known radius, diameter, or screw pitch.

## 🚀 Features

- **Dual Conversion Modes**: 
  - **Wheel / Component Radius**: Convert rotation to distance based on the component's radius or diameter.
  - **Leadscrew / Pitch**: Convert rotation to distance based on screw pitch or threads per inch (TPI).
- **Precision Units**: Supports output in centimeters (cm), millimeters (mm), inches (in), and thousandths of an inch (thou/mil).
- **Arc Length & Multi-turn Calculations**: Handles small fractional angles (e.g., 0.5°) up to multi-rotation values (e.g., 1440°).
- **Reverse Calculation**: Input a target linear distance to find the required rotational degrees.

## 📐 Formulas Used

### 1. Circular / Wheel Travel (Arc Length)
$$d = \theta \times \left( \frac{\pi}{180} \right) \times r$$
Where:
- $d$ = Linear distance
- $\theta$ = Rotational angle in degrees
- $r$ = Radius of the rotating element

### 2. Leadscrew Travel
$$d = \left( \frac{\theta}{360} \right) \times P$$
Where:
- $P$ = Pitch (linear distance traveled per $360^\circ$ rotation)

## 🛠️ Installation & Setup

1. **Clone the repository**:
   ```bash
   git clone https://github.com/yourusername/rotational-measurement-converter.git
   cd rotational-measurement-converter
   ```

2. **Install dependencies** (if using the GUI/Web version):
   ```bash
   pip install -r requirements.txt
   ```

3. **Run the application**:
   ```bash
   python main.py
   ```

## 💻 Usage Example

### Python API Snippet

```python
from converter import RotationalConverter

# Initialize converter with a wheel radius of 5 cm
calc = RotationalConverter(radius=5.0, unit="cm")

# Calculate linear distance for a 90-degree turn
distance_cm = calc.degrees_to_distance(degrees=90)
print(f"Linear Distance: {distance_cm:.4f} cm") 
# Output: Linear Distance: 7.8540 cm
```

## 📋 Roadmap
- [ ] Add pre-configured presets for standard GT2 belts and common CNC leadscrews.
- [ ] Implement mobile-responsive web interface.
- [ ] Add support for step-count conversions based on stepper motor configurations (e.g., 1.8° per step).

## 📄 License
Distributed under the MIT License. See `LICENSE` for more information.
