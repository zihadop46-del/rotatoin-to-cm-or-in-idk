package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.MechanismConfig
import com.example.domain.MechanismType
import com.example.domain.PrecisionCalculator
import com.example.domain.PrecisionResult
import com.example.ui.components.PrecisionRotaryDial
import com.example.ui.components.UnitResultCards
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldTertiary
import java.util.Locale

@Composable
fun ConverterScreen(
  currentDegrees: Double,
  activeMechanism: MechanismConfig,
  allMechanisms: List<MechanismConfig>,
  result: PrecisionResult,
  onDegreesChanged: (Double) -> Unit,
  onMechanismSelected: (MechanismConfig) -> Unit,
  onTargetDistanceEntered: (Double, Boolean) -> Unit,
  onSaveMeasurement: (String, String) -> Unit,
  modifier: Modifier = Modifier
) {
  val focusManager = LocalFocusManager.current
  var showSaveDialog by remember { mutableStateOf(false) }
  var showTargetDistanceDialog by remember { mutableStateOf(false) }
  var mechanismDropdownExpanded by remember { mutableStateOf(false) }

  // Manual degree input dialog/expansion state
  var isManualDegreeEdit by remember { mutableStateOf(false) }
  var degreeInputText by remember(currentDegrees) {
    mutableStateOf(String.format(Locale.US, "%.2f", currentDegrees))
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Mechanism Profile Selector Header
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .testTag("mechanism_selector_card"),
      colors = CardDefaults.cardColors(containerColor = Color(0xFF131924)),
      shape = RoundedCornerShape(14.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(12.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "MECHANISM PROFILE",
              style = MaterialTheme.typography.labelSmall,
              color = Color(0xFF8B949E),
              letterSpacing = 1.sp
            )
            Text(
              text = activeMechanism.name,
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = CyanPrimary
            )
          }

          Box {
            Button(
              onClick = { mechanismDropdownExpanded = true },
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C2433)),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.testTag("select_mechanism_dropdown_button")
            ) {
              Text("Switch", color = Color(0xFFE6EDF3), style = MaterialTheme.typography.labelMedium)
              Spacer(modifier = Modifier.width(4.dp))
              Icon(
                imageVector = Icons.Default.ExpandMore,
                contentDescription = "Expand Mechanisms",
                tint = CyanPrimary,
                modifier = Modifier.size(18.dp)
              )
            }

            DropdownMenu(
              expanded = mechanismDropdownExpanded,
              onDismissRequest = { mechanismDropdownExpanded = false },
              modifier = Modifier
                .background(Color(0xFF161E2C))
                .testTag("mechanism_dropdown_menu")
            ) {
              allMechanisms.forEach { mech ->
                DropdownMenuItem(
                  text = {
                    Column {
                      Text(
                        text = mech.name,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = if (mech.id == activeMechanism.id) CyanPrimary else Color(0xFFE6EDF3)
                      )
                      Text(
                        text = "${mech.type.displayName} • ${mech.notes}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF8B949E)
                      )
                    }
                  },
                  onClick = {
                    onMechanismSelected(mech)
                    mechanismDropdownExpanded = false
                  },
                  trailingIcon = {
                    if (mech.id == activeMechanism.id) {
                      Icon(Icons.Default.Check, contentDescription = "Active", tint = CyanPrimary)
                    }
                  }
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Quick Category Chips
        val mechanismTypes = MechanismType.values()
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          mechanismTypes.forEach { mType ->
            val isSelected = activeMechanism.type == mType
            FilterChip(
              selected = isSelected,
              onClick = {
                val firstOfType = allMechanisms.firstOrNull { it.type == mType }
                if (firstOfType != null) onMechanismSelected(firstOfType)
              },
              label = {
                Text(
                  text = mType.displayName,
                  style = MaterialTheme.typography.labelSmall
                )
              },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = CyanPrimary.copy(alpha = 0.2f),
                selectedLabelColor = CyanPrimary,
                containerColor = Color(0xFF0F141F),
                labelColor = Color(0xFF8B949E)
              )
            )
          }
        }
      }
    }

    // 2. Direct Degree / Inverse Calculation Action Row
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Direct Degree Input Field
      OutlinedTextField(
        value = degreeInputText,
        onValueChange = {
          degreeInputText = it
          val parsed = it.toDoubleOrNull()
          if (parsed != null) {
            onDegreesChanged(parsed)
          }
        },
        label = { Text("Rotational Degrees (°)") },
        modifier = Modifier
          .weight(1f)
          .testTag("degree_input_field"),
        singleLine = true,
        keyboardOptions = KeyboardOptions(
          keyboardType = KeyboardType.Decimal,
          imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = CyanPrimary,
          unfocusedBorderColor = Color(0xFF2E384D),
          focusedLabelColor = CyanPrimary,
          unfocusedLabelColor = Color(0xFF8B949E),
          focusedTextColor = Color(0xFFF0F6FC),
          unfocusedTextColor = Color(0xFFF0F6FC)
        )
      )

      // Inverse Calculation Button (Target cm/in -> Degrees)
      Button(
        onClick = { showTargetDistanceDialog = true },
        colors = ButtonDefaults.buttonColors(containerColor = AmberSecondary),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
          .height(56.dp)
          .testTag("inverse_calc_button")
      ) {
        Icon(
          imageVector = Icons.Default.SwapVert,
          contentDescription = "Inverse Conversion",
          tint = Color(0xFF1E1400)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = "Target Dist",
          style = MaterialTheme.typography.labelMedium,
          color = Color(0xFF1E1400),
          fontWeight = FontWeight.Bold
        )
      }
    }

    // 3. Interactive Precision Rotary Dial with Vernier ticks
    PrecisionRotaryDial(
      currentDegrees = currentDegrees,
      onDegreesChanged = { newDeg ->
        degreeInputText = String.format(Locale.US, "%.2f", newDeg)
        onDegreesChanged(newDeg)
      }
    )

    // 4. Primary Results (Centimeters & Inches) & Precision Breakdown Cards
    UnitResultCards(result = result)

    // 5. Save to Log Action Button
    Button(
      onClick = { showSaveDialog = true },
      colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF182333)),
      shape = RoundedCornerShape(12.dp),
      modifier = Modifier
        .fillMaxWidth()
        .height(50.dp)
        .testTag("save_to_log_button")
    ) {
      Icon(
        imageVector = Icons.Default.BookmarkAdd,
        contentDescription = "Save Measurement",
        tint = CyanPrimary
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = "Save Measurement to Log",
        style = MaterialTheme.typography.titleSmall,
        color = CyanPrimary,
        fontWeight = FontWeight.SemiBold
      )
    }

    Spacer(modifier = Modifier.height(16.dp))
  }

  // Dialog: Inverse Conversion (Target Distance to Degrees)
  if (showTargetDistanceDialog) {
    TargetDistanceDialog(
      activeMechanism = activeMechanism,
      onDismiss = { showTargetDistanceDialog = false },
      onCalculate = { dist, isCm ->
        onTargetDistanceEntered(dist, isCm)
        showTargetDistanceDialog = false
      }
    )
  }

  // Dialog: Save Current Measurement
  if (showSaveDialog) {
    SaveMeasurementDialog(
      defaultTitle = "${activeMechanism.name} (${String.format(Locale.US, "%.1f", currentDegrees)}°)",
      onDismiss = { showSaveDialog = false },
      onSave = { title, notes ->
        onSaveMeasurement(title, notes)
        showSaveDialog = false
      }
    )
  }
}

@Composable
private fun TargetDistanceDialog(
  activeMechanism: MechanismConfig,
  onDismiss: () -> Unit,
  onCalculate: (Double, Boolean) -> Unit
) {
  var distText by remember { mutableStateOf("10.0") }
  var isCentimeters by remember { mutableStateOf(true) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = "Calculate Angle from Target Distance",
        style = MaterialTheme.typography.titleMedium,
        color = Color(0xFFF0F6FC)
      )
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
          text = "Specify desired linear travel for ${activeMechanism.name}:",
          style = MaterialTheme.typography.bodySmall,
          color = Color(0xFF8B949E)
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          FilterChip(
            selected = isCentimeters,
            onClick = { isCentimeters = true },
            label = { Text("Centimeters (cm)") },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = CyanPrimary.copy(alpha = 0.2f),
              selectedLabelColor = CyanPrimary
            )
          )
          FilterChip(
            selected = !isCentimeters,
            onClick = { isCentimeters = false },
            label = { Text("Inches (in)") },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = AmberSecondary.copy(alpha = 0.2f),
              selectedLabelColor = AmberSecondary
            )
          )
        }

        OutlinedTextField(
          value = distText,
          onValueChange = { distText = it },
          label = { Text(if (isCentimeters) "Target Distance (cm)" else "Target Distance (inches)") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("target_distance_input")
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val parsed = distText.toDoubleOrNull() ?: 0.0
          onCalculate(parsed, isCentimeters)
        },
        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
        modifier = Modifier.testTag("apply_target_distance_button")
      ) {
        Text("Calculate Degrees", color = Color(0xFF00363D), fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel", color = Color(0xFF8B949E))
      }
    },
    containerColor = Color(0xFF131A26)
  )
}

@Composable
private fun SaveMeasurementDialog(
  defaultTitle: String,
  onDismiss: () -> Unit,
  onSave: (String, String) -> Unit
) {
  var title by remember { mutableStateOf(defaultTitle) }
  var notes by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = "Save Measurement",
        style = MaterialTheme.typography.titleMedium,
        color = Color(0xFFF0F6FC)
      )
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Title / Label") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("save_title_input")
        )
        OutlinedTextField(
          value = notes,
          onValueChange = { notes = it },
          label = { Text("Notes (e.g. Pass 1, 3D printer calibration)") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("save_notes_input")
        )
      }
    },
    confirmButton = {
      Button(
        onClick = { onSave(title, notes) },
        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
        modifier = Modifier.testTag("confirm_save_measurement_button")
      ) {
        Text("Save", color = Color(0xFF00363D), fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel", color = Color(0xFF8B949E))
      }
    },
    containerColor = Color(0xFF131A26)
  )
}
