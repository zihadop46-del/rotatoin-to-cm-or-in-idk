package com.example.domain

import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.roundToInt

enum class MechanismType(val displayName: String, val description: String) {
  WHEEL("Wheel / Roller", "Rolling wheel, disc, drum, or cylindrical shaft"),
  LEAD_SCREW("Lead Screw / Bolt", "Threaded rod, ball screw, or micrometer spindle"),
  BELT_PULLEY("Belt & Pulley / Gear", "Timing belt (GT2, etc.) or rack & pinion teeth"),
  ARC("Circular Arc", "Curved arc length along radius of curvature")
}

data class MechanismConfig(
  val id: Long = 0,
  val name: String,
  val type: MechanismType,
  val diameterMm: Double = 0.0,
  val leadMm: Double = 0.0,
  val teethCount: Int = 0,
  val toothPitchMm: Double = 0.0,
  val arcRadiusMm: Double = 0.0,
  val gearRatio: Double = 1.0, // Driven turns per driver turn (1.0 = direct drive)
  val isCustom: Boolean = false,
  val notes: String = ""
) {
  /**
   * Linear travel in millimeters per 1 full rotation (360 degrees) of the primary input,
   * taking into account gear ratio.
   */
  val mmPerRevolution: Double
    get() = when (type) {
      MechanismType.WHEEL -> (PI * diameterMm) * gearRatio
      MechanismType.LEAD_SCREW -> leadMm * gearRatio
      MechanismType.BELT_PULLEY -> (teethCount * toothPitchMm) * gearRatio
      MechanismType.ARC -> (2.0 * PI * arcRadiusMm) * gearRatio
    }
}

data class PrecisionResult(
  val degrees: Double,
  val revolutions: Double,
  val distanceCm: Double,
  val distanceMm: Double,
  val distanceMicrons: Double,
  val distanceMeters: Double,
  val distanceInches: Double,
  val distanceMilsThou: Double,
  val distanceFeet: Double,
  val fractionalInch: String,
  val fractionalErrorThou: Double,
  val formulaSummary: String
)

data class StepperMotionResult(
  val stepAngleDeg: Double,
  val microstepping: Int,
  val totalStepsPerRev: Int,
  val stepsPerMm: Double,
  val stepsPerCm: Double,
  val stepsPerInch: Double,
  val mmPerFullStep: Double,
  val micronsPerMicrostep: Double
)

object PrecisionCalculator {

  private const val MM_TO_INCH = 1.0 / 25.4
  private const val INCH_TO_MM = 25.4

  /**
   * Calculates linear distance in centimeters, inches, millimeters, etc.
   * given a rotational angle in degrees.
   */
  fun calculateFromDegrees(degrees: Double, config: MechanismConfig): PrecisionResult {
    val revs = (degrees / 360.0) * config.gearRatio
    val mmPerRev = config.mmPerRevolution
    // Total distance traveled in millimeters
    val totalMm = (degrees / 360.0) * mmPerRev

    val totalCm = totalMm / 10.0
    val totalMeters = totalMm / 1000.0
    val totalMicrons = totalMm * 1000.0

    val totalInches = totalMm * MM_TO_INCH
    val totalMilsThou = totalInches * 1000.0
    val totalFeet = totalInches / 12.0

    val (fractionStr, errorThou) = formatFractionalInches(totalInches)

    val formula = when (config.type) {
      MechanismType.WHEEL -> "d = (θ / 360°) × π × ${formatNum(config.diameterMm)} mm"
      MechanismType.LEAD_SCREW -> "d = (θ / 360°) × ${formatNum(config.leadMm)} mm lead"
      MechanismType.BELT_PULLEY -> "d = (θ / 360°) × (${config.teethCount} teeth × ${formatNum(config.toothPitchMm)} mm)"
      MechanismType.ARC -> "s = (θ / 360°) × 2π × ${formatNum(config.arcRadiusMm)} mm radius"
    }

    return PrecisionResult(
      degrees = degrees,
      revolutions = revs,
      distanceCm = totalCm,
      distanceMm = totalMm,
      distanceMicrons = totalMicrons,
      distanceMeters = totalMeters,
      distanceInches = totalInches,
      distanceMilsThou = totalMilsThou,
      distanceFeet = totalFeet,
      fractionalInch = fractionStr,
      fractionalErrorThou = errorThou,
      formulaSummary = formula
    )
  }

  /**
   * Calculates rotational degrees required to travel a target distance in centimeters.
   */
  fun calculateDegreesFromCm(targetCm: Double, config: MechanismConfig): Double {
    val targetMm = targetCm * 10.0
    val mmPerRev = config.mmPerRevolution
    if (abs(mmPerRev) < 1e-9) return 0.0
    return (targetMm / mmPerRev) * 360.0
  }

  /**
   * Calculates rotational degrees required to travel a target distance in inches.
   */
  fun calculateDegreesFromInches(targetInches: Double, config: MechanismConfig): Double {
    val targetMm = targetInches * INCH_TO_MM
    val mmPerRev = config.mmPerRevolution
    if (abs(mmPerRev) < 1e-9) return 0.0
    return (targetMm / mmPerRev) * 360.0
  }

  /**
   * Converts decimal inches to a fractional representation (e.g., 3 7/16")
   * up to 1/64-inch precision, returning both the fractional string and the residual error in mils (thou).
   */
  fun formatFractionalInches(inches: Double): Pair<String, Double> {
    if (inches.isNaN() || inches.isInfinite()) return Pair("0\"", 0.0)
    val sign = if (inches < 0) "-" else ""
    val absInches = abs(inches)
    val whole = absInches.toInt()
    val remainder = absInches - whole

    val denominator = 64
    var numerator = (remainder * denominator).roundToInt()

    var effectiveWhole = whole
    if (numerator == denominator) {
      effectiveWhole += 1
      numerator = 0
    }

    val exactRepresented = effectiveWhole + (numerator.toDouble() / denominator)
    val errorThou = (absInches - exactRepresented) * 1000.0 * (if (inches < 0) -1.0 else 1.0)

    if (numerator == 0) {
      return Pair("$sign$effectiveWhole\"", errorThou)
    }

    // Reduce fraction
    var num = numerator
    var den = denominator
    while (num % 2 == 0 && den % 2 == 0) {
      num /= 2
      den /= 2
    }

    val result = if (effectiveWhole > 0) {
      "$sign$effectiveWhole $num/$den\""
    } else {
      "$sign$num/$den\""
    }

    return Pair(result, errorThou)
  }

  /**
   * Calculates stepper motor & CNC motion resolution: steps/mm, steps/inch, and resolution per microstep.
   */
  fun calculateStepperMotion(
    stepAngleDeg: Double,
    microstepping: Int,
    config: MechanismConfig
  ): StepperMotionResult {
    val safeStepAngle = if (stepAngleDeg <= 0.0) 1.8 else stepAngleDeg
    val safeMicrostepping = if (microstepping <= 0) 1 else microstepping
    val baseStepsPerRev = (360.0 / safeStepAngle).roundToInt()
    val totalStepsPerRev = baseStepsPerRev * safeMicrostepping

    val mmPerRev = config.mmPerRevolution
    val stepsPerMm = if (mmPerRev != 0.0) totalStepsPerRev / mmPerRev else 0.0
    val stepsPerCm = stepsPerMm * 10.0
    val stepsPerInch = stepsPerMm * INCH_TO_MM

    val mmPerFullStep = if (baseStepsPerRev != 0) mmPerRev / baseStepsPerRev else 0.0
    val micronsPerMicrostep = if (totalStepsPerRev != 0) (mmPerRev / totalStepsPerRev) * 1000.0 else 0.0

    return StepperMotionResult(
      stepAngleDeg = safeStepAngle,
      microstepping = safeMicrostepping,
      totalStepsPerRev = totalStepsPerRev,
      stepsPerMm = stepsPerMm,
      stepsPerCm = stepsPerCm,
      stepsPerInch = stepsPerInch,
      mmPerFullStep = mmPerFullStep,
      micronsPerMicrostep = micronsPerMicrostep
    )
  }

  fun formatNum(value: Double, decimals: Int = 4): String {
    return String.format(Locale.US, "%.${decimals}f", value)
  }
}
