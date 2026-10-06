package com.example.domain

object MechanismPresets {
  val defaultPresets: List<MechanismConfig> = listOf(
    // Wheels & Rollers
    MechanismConfig(
      id = 1,
      name = "Measuring Road Wheel (1m Circ.)",
      type = MechanismType.WHEEL,
      diameterMm = 318.309886,
      gearRatio = 1.0,
      notes = "100 cm linear travel per 360° turn"
    ),
    MechanismConfig(
      id = 2,
      name = "Precision Roller (100 mm Dia)",
      type = MechanismType.WHEEL,
      diameterMm = 100.0,
      gearRatio = 1.0,
      notes = "π × 100 mm ≈ 31.416 cm per rev"
    ),
    MechanismConfig(
      id = 3,
      name = "Surveyor Wheel (12.0\" / 304.8 mm)",
      type = MechanismType.WHEEL,
      diameterMm = 304.8,
      gearRatio = 1.0,
      notes = "37.70\" (95.76 cm) per revolution"
    ),
    MechanismConfig(
      id = 4,
      name = "Optical Encoder Wheel (50 mm Dia)",
      type = MechanismType.WHEEL,
      diameterMm = 50.0,
      gearRatio = 1.0,
      notes = "15.708 cm per revolution"
    ),

    // Lead Screws & Threaded Rods
    MechanismConfig(
      id = 5,
      name = "T8x8 Lead Screw (3D Printer Z-Axis)",
      type = MechanismType.LEAD_SCREW,
      leadMm = 8.0,
      gearRatio = 1.0,
      notes = "8 mm (0.8 cm) travel per 360° rotation (4-start)"
    ),
    MechanismConfig(
      id = 6,
      name = "T8x2 Lead Screw (Precision CNC)",
      type = MechanismType.LEAD_SCREW,
      leadMm = 2.0,
      gearRatio = 1.0,
      notes = "2 mm (0.2 cm) travel per 360° rotation (1-start)"
    ),
    MechanismConfig(
      id = 7,
      name = "T8x4 Lead Screw (Dual-Start)",
      type = MechanismType.LEAD_SCREW,
      leadMm = 4.0,
      gearRatio = 1.0,
      notes = "4 mm (0.4 cm) travel per 360° rotation"
    ),
    MechanismConfig(
      id = 8,
      name = "Micrometer Spindle (0.5 mm Lead)",
      type = MechanismType.LEAD_SCREW,
      leadMm = 0.5,
      gearRatio = 1.0,
      notes = "500 μm (0.05 cm) per 360° rotation"
    ),
    MechanismConfig(
      id = 9,
      name = "M8x1.25 Metric Threaded Rod",
      type = MechanismType.LEAD_SCREW,
      leadMm = 1.25,
      gearRatio = 1.0,
      notes = "1.25 mm pitch per revolution"
    ),
    MechanismConfig(
      id = 10,
      name = "1/4\"-20 UNC Threaded Rod (20 TPI)",
      type = MechanismType.LEAD_SCREW,
      leadMm = 1.27, // 25.4 / 20
      gearRatio = 1.0,
      notes = "0.050\" (1.27 mm) per revolution"
    ),

    // Belts & Pulleys
    MechanismConfig(
      id = 11,
      name = "GT2 Belt - 20 Tooth Pulley",
      type = MechanismType.BELT_PULLEY,
      teethCount = 20,
      toothPitchMm = 2.0,
      gearRatio = 1.0,
      notes = "40 mm (4.0 cm) linear travel per revolution"
    ),
    MechanismConfig(
      id = 12,
      name = "GT2 Belt - 16 Tooth Pulley",
      type = MechanismType.BELT_PULLEY,
      teethCount = 16,
      toothPitchMm = 2.0,
      gearRatio = 1.0,
      notes = "32 mm (3.2 cm) linear travel per revolution"
    ),

    // Arcs
    MechanismConfig(
      id = 13,
      name = "Circular Arc (R = 100 mm)",
      type = MechanismType.ARC,
      arcRadiusMm = 100.0,
      gearRatio = 1.0,
      notes = "1.745 mm per 1.0° angle"
    ),
    MechanismConfig(
      id = 14,
      name = "Circular Arc (R = 6.0\" / 152.4 mm)",
      type = MechanismType.ARC,
      arcRadiusMm = 152.4,
      gearRatio = 1.0,
      notes = "2.659 mm (0.1047\") per 1.0° angle"
    )
  )
}
