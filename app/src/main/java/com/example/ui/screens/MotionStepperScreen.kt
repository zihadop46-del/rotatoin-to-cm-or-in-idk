package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.MechanismConfig
import com.example.domain.StepperMotionResult
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldTertiary
import java.util.Locale

@Composable
fun MotionStepperScreen(
  activeMechanism: MechanismConfig,
  stepperAngle: Double,
  microstepping: Int,
  stepperResult: StepperMotionResult,
  onStepperAngleChanged: (Double) -> Unit,
  onMicrosteppingChanged: (Int) -> Unit,
  modifier: Modifier = Modifier
) {
  var customTpiInput by remember { mutableStateOf("20") }
  val parsedTpi = customTpiInput.toDoubleOrNull() ?: 20.0
  val calculatedPitchMm = if (parsedTpi > 0) 25.4 / parsedTpi else 0.0

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Header Card
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .testTag("stepper_header_card"),
      colors = CardDefaults.cardColors(containerColor = Color(0xFF131924)),
      shape = RoundedCornerShape(14.dp)
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Text(
          text = "CNC & STEPPER MOTION RESOLUTION",
          style = MaterialTheme.typography.labelSmall,
          color = Color(0xFF8B949E),
          letterSpacing = 1.sp
        )
        Text(
          text = "Steps / Distance Calibration",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = CyanPrimary
        )
        Text(
          text = "Active Mechanism: ${activeMechanism.name} (${String.format(Locale.US, "%.3f", activeMechanism.mmPerRevolution)} mm/rev)",
          style = MaterialTheme.typography.bodySmall,
          color = Color(0xFF8B949E)
        )
      }
    }

    // 2. Stepper Motor Angle Selection
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .testTag("motor_angle_card"),
      colors = CardDefaults.cardColors(containerColor = Color(0xFF131924)),
      shape = RoundedCornerShape(12.dp)
    ) {
      Column(
        modifier = Modifier.padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Text(
          text = "MOTOR STEP ANGLE",
          style = MaterialTheme.typography.labelSmall,
          color = Color(0xFF7E8B9B)
        )

        val angleOptions = listOf(
          1.8 to "1.8° (200 steps/rev)",
          0.9 to "0.9° (400 steps/rev)",
          7.5 to "7.5° (48 steps/rev)"
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          angleOptions.forEach { (angle, label) ->
            FilterChip(
              selected = kotlin.math.abs(stepperAngle - angle) < 0.01,
              onClick = { onStepperAngleChanged(angle) },
              label = { Text(label, fontSize = 11.sp) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = CyanPrimary.copy(alpha = 0.2f),
                selectedLabelColor = CyanPrimary
              ),
              modifier = Modifier.testTag("chip_angle_${angle}")
            )
          }
        }
      }
    }

    // 3. Microstepping Driver Selector
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .testTag("microstepping_card"),
      colors = CardDefaults.cardColors(containerColor = Color(0xFF131924)),
      shape = RoundedCornerShape(12.dp)
    ) {
      Column(
        modifier = Modifier.padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Text(
          text = "MICROSTEPPING DRIVER SETTING",
          style = MaterialTheme.typography.labelSmall,
          color = Color(0xFF7E8B9B)
        )

        val microOptions = listOf(1, 2, 4, 8, 16, 32, 64)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          microOptions.forEach { micro ->
            FilterChip(
              selected = microstepping == micro,
              onClick = { onMicrosteppingChanged(micro) },
              label = { Text(if (micro == 1) "1x (Full)" else "1/${micro}") },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = AmberSecondary.copy(alpha = 0.2f),
                selectedLabelColor = AmberSecondary
              ),
              modifier = Modifier.testTag("chip_micro_${micro}")
            )
          }
        }
      }
    }

    // 4. Motion Resolution & Steps Calibration Results
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .testTag("motion_results_card"),
      colors = CardDefaults.cardColors(containerColor = Color(0xFF111722)),
      shape = RoundedCornerShape(14.dp)
    ) {
      Column(
        modifier = Modifier.padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Text(
          text = "CALIBRATION OUTPUT (STEPS PER UNIT)",
          style = MaterialTheme.typography.labelSmall,
          color = Color(0xFF7E8B9B),
          letterSpacing = 1.sp
        )

        // Steps per Centimeter & Steps per Inch
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column {
            Text("Steps per Centimeter", style = MaterialTheme.typography.bodySmall, color = Color(0xFF8B949E))
            Text(
              text = String.format(Locale.US, "%.3f steps/cm", stepperResult.stepsPerCm),
              style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
              ),
              color = CyanPrimary
            )
          }

          Column(horizontalAlignment = Alignment.End) {
            Text("Steps per Inch", style = MaterialTheme.typography.bodySmall, color = Color(0xFF8B949E))
            Text(
              text = String.format(Locale.US, "%.3f steps/in", stepperResult.stepsPerInch),
              style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
              ),
              color = AmberSecondary
            )
          }
        }

        // Steps per Millimeter & Microstep Resolution
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column {
            Text("Steps per mm (CNC/3D)", style = MaterialTheme.typography.bodySmall, color = Color(0xFF8B949E))
            Text(
              text = String.format(Locale.US, "%.4f steps/mm", stepperResult.stepsPerMm),
              style = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold
              ),
              color = Color(0xFFE6EDF3)
            )
          }

          Column(horizontalAlignment = Alignment.End) {
            Text("Resolution per Microstep", style = MaterialTheme.typography.bodySmall, color = Color(0xFF8B949E))
            Text(
              text = String.format(Locale.US, "%.2f μm / step", stepperResult.micronsPerMicrostep),
              style = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold
              ),
              color = EmeraldTertiary
            )
          }
        }

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF090D14))
            .padding(8.dp)
        ) {
          Text(
            text = "Total ${stepperResult.totalStepsPerRev} microsteps per 360° turn • Full step: ${String.format(Locale.US, "%.4f", stepperResult.mmPerFullStep)} mm",
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            color = Color(0xFF8B949E)
          )
        }
      }
    }

    // 5. Bonus Machinist Utility: TPI to Pitch / Lead Converter
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .testTag("tpi_converter_card"),
      colors = CardDefaults.cardColors(containerColor = Color(0xFF131924)),
      shape = RoundedCornerShape(12.dp)
    ) {
      Column(
        modifier = Modifier.padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Text(
          text = "MACHINIST TPI TO PITCH QUICK CONVERTER",
          style = MaterialTheme.typography.labelSmall,
          color = AmberSecondary,
          fontWeight = FontWeight.Bold
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          OutlinedTextField(
            value = customTpiInput,
            onValueChange = { customTpiInput = it },
            label = { Text("Threads Per Inch (TPI)") },
            modifier = Modifier
              .weight(1f)
              .testTag("tpi_input_field"),
            singleLine = true
          )

          Column(modifier = Modifier.weight(1f)) {
            Text("Lead per 360° turn:", style = MaterialTheme.typography.bodySmall, color = Color(0xFF8B949E))
            Text(
              text = String.format(Locale.US, "%.4f mm", calculatedPitchMm),
              style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace),
              color = CyanPrimary
            )
            Text(
              text = String.format(Locale.US, "%.4f inches (%.1f thou)", 1.0 / parsedTpi, (1.0 / parsedTpi) * 1000.0),
              style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
              color = AmberSecondary
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))
  }
}
