package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.example.domain.PrecisionCalculator
import com.example.domain.PrecisionResult
import com.example.ui.components.SamsungKeypad
import com.example.ui.theme.SamsungDarkBackground
import com.example.ui.theme.SamsungDivider
import com.example.ui.theme.SamsungGreenAccent
import com.example.ui.theme.SamsungSurface
import com.example.ui.theme.SamsungTextPrimary
import com.example.ui.theme.SamsungTextSecondary
import java.util.Locale

enum class TargetLinearUnit(val label: String, val symbol: String) {
  CM("Centimeter", "cm"),
  INCH("Inch", "in"),
  MM("Millimeter", "mm"),
  MICRON("Micron", "μm"),
  THOU("Thou (0.001\")", "thou"),
  METER("Meter", "m"),
  FEET("Foot", "ft")
}

@Composable
fun SamsungUnitConverterScreen(
  currentDegrees: Double,
  activeMechanism: MechanismConfig,
  allMechanisms: List<MechanismConfig>,
  precisionResult: PrecisionResult,
  onDegreesChanged: (Double) -> Unit,
  onMechanismSelected: (MechanismConfig) -> Unit,
  onBackToCalculator: () -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedTargetUnit by remember { mutableStateOf(TargetLinearUnit.CM) }
  var isEditingDistance by remember { mutableStateOf(false) } // false = typing degrees, true = typing target distance
  var inputDigits by remember { mutableStateOf(if (currentDegrees == currentDegrees.toLong().toDouble()) "${currentDegrees.toLong()}" else String.format(Locale.US, "%.2f", currentDegrees)) }
  var showTargetUnitMenu by remember { mutableStateOf(false) }
  var showMechanismMenu by remember { mutableStateOf(false) }

  fun onInputChanged(newDigits: String) {
    inputDigits = newDigits
    val parsed = newDigits.toDoubleOrNull() ?: 0.0
    if (!isEditingDistance) {
      onDegreesChanged(parsed)
    } else {
      // Inverse: distance -> degrees
      val deg = when (selectedTargetUnit) {
        TargetLinearUnit.CM -> PrecisionCalculator.calculateDegreesFromCm(parsed, activeMechanism)
        TargetLinearUnit.INCH -> PrecisionCalculator.calculateDegreesFromInches(parsed, activeMechanism)
        TargetLinearUnit.MM -> PrecisionCalculator.calculateDegreesFromCm(parsed / 10.0, activeMechanism)
        TargetLinearUnit.MICRON -> PrecisionCalculator.calculateDegreesFromCm(parsed / 10000.0, activeMechanism)
        TargetLinearUnit.THOU -> PrecisionCalculator.calculateDegreesFromInches(parsed / 1000.0, activeMechanism)
        TargetLinearUnit.METER -> PrecisionCalculator.calculateDegreesFromCm(parsed * 100.0, activeMechanism)
        TargetLinearUnit.FEET -> PrecisionCalculator.calculateDegreesFromInches(parsed * 12.0, activeMechanism)
      }
      onDegreesChanged(deg)
    }
  }

  fun appendDigit(digit: String) {
    if (inputDigits == "0" && digit != ".") {
      onInputChanged(digit)
    } else {
      onInputChanged(inputDigits + digit)
    }
  }

  fun backspace() {
    if (inputDigits.isNotEmpty()) {
      val updated = if (inputDigits.length > 1) inputDigits.substring(0, inputDigits.length - 1) else "0"
      onInputChanged(updated)
    }
  }

  fun clear() {
    onInputChanged("0")
  }

  fun swapFields() {
    isEditingDistance = !isEditingDistance
    inputDigits = if (isEditingDistance) {
      val currentVal = when (selectedTargetUnit) {
        TargetLinearUnit.CM -> precisionResult.distanceCm
        TargetLinearUnit.INCH -> precisionResult.distanceInches
        TargetLinearUnit.MM -> precisionResult.distanceMm
        TargetLinearUnit.MICRON -> precisionResult.distanceMicrons
        TargetLinearUnit.THOU -> precisionResult.distanceMilsThou
        TargetLinearUnit.METER -> precisionResult.distanceMeters
        TargetLinearUnit.FEET -> precisionResult.distanceFeet
      }
      String.format(Locale.US, "%.4f", currentVal)
    } else {
      String.format(Locale.US, "%.2f", currentDegrees)
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(SamsungDarkBackground)
  ) {
    // Header
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(
        onClick = onBackToCalculator,
        modifier = Modifier.testTag("converter_back_button")
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = "Back",
          tint = Color.White
        )
      }
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = "Unit converter",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White
      )
      Spacer(modifier = Modifier.weight(1f))
      // Mechanism pill
      Box {
        Surface(
          onClick = { showMechanismMenu = true },
          shape = RoundedCornerShape(12.dp),
          color = Color(0xFF1C1C1E)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = activeMechanism.name,
              color = SamsungGreenAccent,
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold
            )
            Icon(Icons.Default.ExpandMore, contentDescription = null, tint = SamsungGreenAccent, modifier = Modifier.size(16.dp))
          }
        }

        DropdownMenu(
          expanded = showMechanismMenu,
          onDismissRequest = { showMechanismMenu = false },
          modifier = Modifier.background(Color(0xFF242426))
        ) {
          allMechanisms.forEach { mech ->
            DropdownMenuItem(
              text = {
                Text(
                  text = mech.name,
                  color = if (mech.id == activeMechanism.id) SamsungGreenAccent else Color.White
                )
              },
              onClick = {
                onMechanismSelected(mech)
                showMechanismMenu = false
              }
            )
          }
        }
      }
    }

    // Two Dual Conversion Fields (Samsung Style)
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
        .padding(horizontal = 20.dp),
      verticalArrangement = Arrangement.Center
    ) {
      // Top Field (Degree Angle)
      val isTopActive = !isEditingDistance
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { isEditingDistance = false }
          .testTag("converter_box_degree"),
        colors = CardDefaults.cardColors(containerColor = if (isTopActive) Color(0xFF1C1C1E) else Color(0xFF121212)),
        shape = RoundedCornerShape(16.dp),
        border = if (isTopActive) CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SamsungGreenAccent), width = 1.5.dp) else null
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(text = "Rotational Degree", color = SamsungTextSecondary, fontSize = 13.sp)
          Spacer(modifier = Modifier.height(4.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            val topVal = if (!isEditingDistance) inputDigits else String.format(Locale.US, "%.2f", currentDegrees)
            Text(
              text = topVal,
              fontSize = 32.sp,
              fontWeight = FontWeight.Medium,
              color = if (isTopActive) Color.White else SamsungTextSecondary,
              fontFamily = FontFamily.Monospace
            )
            Text(text = "°", fontSize = 28.sp, color = SamsungGreenAccent, fontWeight = FontWeight.Bold)
          }
        }
      }

      // Middle Swap Button
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
      ) {
        Surface(
          onClick = { swapFields() },
          shape = CircleShape,
          color = Color(0xFF2A2A2E),
          modifier = Modifier.size(42.dp).testTag("converter_swap_button")
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(Icons.Default.SwapVert, contentDescription = "Swap conversion", tint = SamsungGreenAccent, modifier = Modifier.size(24.dp))
          }
        }
      }

      // Bottom Field (Target Linear Unit: cm, in, mm, thou, etc.)
      val isBottomActive = isEditingDistance
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { isEditingDistance = true }
          .testTag("converter_box_linear"),
        colors = CardDefaults.cardColors(containerColor = if (isBottomActive) Color(0xFF1C1C1E) else Color(0xFF121212)),
        shape = RoundedCornerShape(16.dp),
        border = if (isBottomActive) CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SamsungGreenAccent), width = 1.5.dp) else null
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Box {
            Row(
              modifier = Modifier.clickable { showTargetUnitMenu = true },
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = selectedTargetUnit.label,
                color = SamsungGreenAccent,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
              )
              Icon(Icons.Default.ExpandMore, contentDescription = null, tint = SamsungGreenAccent, modifier = Modifier.size(16.dp))
            }

            DropdownMenu(
              expanded = showTargetUnitMenu,
              onDismissRequest = { showTargetUnitMenu = false },
              modifier = Modifier.background(Color(0xFF242426))
            ) {
              TargetLinearUnit.values().forEach { u ->
                DropdownMenuItem(
                  text = { Text("${u.label} (${u.symbol})", color = if (u == selectedTargetUnit) SamsungGreenAccent else Color.White) },
                  onClick = {
                    selectedTargetUnit = u
                    showTargetUnitMenu = false
                  }
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(4.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            val bottomVal = if (isEditingDistance) {
              inputDigits
            } else {
              when (selectedTargetUnit) {
                TargetLinearUnit.CM -> String.format(Locale.US, "%.4f", precisionResult.distanceCm)
                TargetLinearUnit.INCH -> String.format(Locale.US, "%.4f", precisionResult.distanceInches)
                TargetLinearUnit.MM -> String.format(Locale.US, "%.3f", precisionResult.distanceMm)
                TargetLinearUnit.MICRON -> String.format(Locale.US, "%,.1f", precisionResult.distanceMicrons)
                TargetLinearUnit.THOU -> String.format(Locale.US, "%,.1f", precisionResult.distanceMilsThou)
                TargetLinearUnit.METER -> String.format(Locale.US, "%.4f", precisionResult.distanceMeters)
                TargetLinearUnit.FEET -> String.format(Locale.US, "%.4f", precisionResult.distanceFeet)
              }
            }
            Text(
              text = bottomVal,
              fontSize = 32.sp,
              fontWeight = FontWeight.Medium,
              color = if (isBottomActive) Color.White else SamsungGreenAccent,
              fontFamily = FontFamily.Monospace
            )
            Text(text = selectedTargetUnit.symbol, fontSize = 20.sp, color = SamsungGreenAccent, fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    // Backspace bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 4.dp),
      horizontalArrangement = Arrangement.End
    ) {
      IconButton(onClick = { backspace() }, modifier = Modifier.size(36.dp)) {
        Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = "Backspace", tint = SamsungGreenAccent, modifier = Modifier.size(20.dp))
      }
    }

    // Samsung Keypad
    SamsungKeypad(
      onDigit = { appendDigit(it) },
      onOperator = { /* In unit conversion mode, operators can be evaluated or no-op */ },
      onClear = { clear() },
      onEquals = { /* Keep value */ },
      onParentheses = { },
      onPlusMinus = {
        val parsed = inputDigits.toDoubleOrNull() ?: 0.0
        onInputChanged(String.format(Locale.US, "%.2f", -parsed))
      },
      modifier = Modifier.padding(bottom = 8.dp)
    )
  }
}
