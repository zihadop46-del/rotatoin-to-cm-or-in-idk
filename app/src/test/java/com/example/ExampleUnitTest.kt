package com.example

import com.example.domain.MechanismConfig
import com.example.domain.MechanismType
import com.example.domain.PrecisionCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI

class ExampleUnitTest {

  @Test
  fun testWheelRotationToCentimeters() {
    // A roller with diameter 100 mm (10 cm)
    val roller = MechanismConfig(
      name = "100mm Roller",
      type = MechanismType.WHEEL,
      diameterMm = 100.0
    )

    // 360 degrees = 1 full revolution = PI * 100mm = 314.159265 mm = 31.4159265 cm
    val result360 = PrecisionCalculator.calculateFromDegrees(360.0, roller)
    assertEquals(1.0, result360.revolutions, 0.0001)
    assertEquals(PI * 10.0, result360.distanceCm, 0.0001)

    // 90 degrees = 1/4 revolution = 7.85398 cm
    val result90 = PrecisionCalculator.calculateFromDegrees(90.0, roller)
    assertEquals(0.25, result90.revolutions, 0.0001)
    assertEquals((PI * 10.0) / 4.0, result90.distanceCm, 0.0001)

    // Inches check (1 cm = 1 / 2.54 inches)
    val expectedInches = result90.distanceCm / 2.54
    assertEquals(expectedInches, result90.distanceInches, 0.0001)
  }

  @Test
  fun testLeadScrewRotation() {
    // T8x8 Lead Screw (8 mm lead per revolution)
    val leadScrew = MechanismConfig(
      name = "T8x8",
      type = MechanismType.LEAD_SCREW,
      leadMm = 8.0
    )

    // 720 degrees = 2 revolutions = 16 mm = 1.6 cm
    val result720 = PrecisionCalculator.calculateFromDegrees(720.0, leadScrew)
    assertEquals(2.0, result720.revolutions, 0.0001)
    assertEquals(1.6, result720.distanceCm, 0.0001)
    assertEquals(16.0, result720.distanceMm, 0.0001)
    assertEquals(16.0 / 25.4, result720.distanceInches, 0.0001)
  }

  @Test
  fun testBeltPulleyRotation() {
    // GT2 20-tooth pulley = 20 * 2mm = 40mm / rev = 4.0 cm
    val pulley = MechanismConfig(
      name = "GT2-20T",
      type = MechanismType.BELT_PULLEY,
      teethCount = 20,
      toothPitchMm = 2.0
    )

    val result180 = PrecisionCalculator.calculateFromDegrees(180.0, pulley)
    assertEquals(0.5, result180.revolutions, 0.0001)
    assertEquals(2.0, result180.distanceCm, 0.0001)
    assertEquals(20.0, result180.distanceMm, 0.0001)
  }

  @Test
  fun testInverseDistanceToDegrees() {
    val roller = MechanismConfig(
      name = "100mm Roller",
      type = MechanismType.WHEEL,
      diameterMm = 100.0
    )

    val targetCm = 15.0
    val calculatedDeg = PrecisionCalculator.calculateDegreesFromCm(targetCm, roller)
    val verifyResult = PrecisionCalculator.calculateFromDegrees(calculatedDeg, roller)
    assertEquals(targetCm, verifyResult.distanceCm, 0.0001)
  }

  @Test
  fun testFractionalInchesFormatting() {
    val (fractionStr, error) = PrecisionCalculator.formatFractionalInches(2.5)
    assertTrue("Should contain 1/2", fractionStr.contains("1/2\""))
  }

  @Test
  fun testStepperMotionCalculations() {
    // 1.8 deg motor (200 steps), 1/16 microstepping = 3200 steps/rev
    // Lead screw T8x8 = 8mm/rev
    val leadScrew = MechanismConfig(
      name = "T8x8",
      type = MechanismType.LEAD_SCREW,
      leadMm = 8.0
    )

    val stepper = PrecisionCalculator.calculateStepperMotion(1.8, 16, leadScrew)
    assertEquals(3200, stepper.totalStepsPerRev)
    // 3200 steps / 8mm = 400 steps/mm
    assertEquals(400.0, stepper.stepsPerMm, 0.0001)
    assertEquals(4000.0, stepper.stepsPerCm, 0.0001)
  }
}
