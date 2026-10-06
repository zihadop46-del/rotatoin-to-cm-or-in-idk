package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.MechanismConfig
import com.example.domain.MechanismType
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.CyanPrimary
import java.util.Locale

@Composable
fun PresetsScreen(
  activeMechanism: MechanismConfig,
  allMechanisms: List<MechanismConfig>,
  onSelectMechanism: (MechanismConfig) -> Unit,
  onSaveCustomMechanism: (
    name: String,
    type: MechanismType,
    diameterMm: Double,
    leadMm: Double,
    teethCount: Int,
    toothPitchMm: Double,
    arcRadiusMm: Double,
    gearRatio: Double,
    notes: String
  ) -> Unit,
  onDeleteCustomMechanism: (Long) -> Unit,
  modifier: Modifier = Modifier
) {
  var showAddDialog by remember { mutableStateOf(false) }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    containerColor = Color.Transparent,
    floatingActionButton = {
      FloatingActionButton(
        onClick = { showAddDialog = true },
        containerColor = CyanPrimary,
        contentColor = Color(0xFF00363D),
        modifier = Modifier.testTag("add_custom_mechanism_fab")
      ) {
        Icon(Icons.Default.Add, contentDescription = "Add Custom Mechanism")
      }
    }
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .padding(horizontal = 16.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Text(
        text = "MECHANISM PRESETS & PROFILES",
        style = MaterialTheme.typography.labelSmall,
        color = Color(0xFF8B949E),
        letterSpacing = 1.sp
      )

      LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("presets_lazy_column"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        items(allMechanisms, key = { "${it.isCustom}_${it.id}_${it.name}" }) { mech ->
          val isActive = mech.id == activeMechanism.id && mech.isCustom == activeMechanism.isCustom
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .testTag("preset_card_${mech.name.replace(" ", "_")}"),
            colors = CardDefaults.cardColors(
              containerColor = if (isActive) Color(0xFF192230) else Color(0xFF131924)
            ),
            shape = RoundedCornerShape(12.dp),
            border = if (isActive) CardDefaults.outlinedCardBorder().copy(width = 1.5.dp) else null
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = mech.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (isActive) CyanPrimary else Color(0xFFF0F6FC)
                  )
                  if (mech.isCustom) {
                    Spacer(Modifier.width(6.dp))
                    Text(
                      text = "CUSTOM",
                      style = MaterialTheme.typography.labelSmall,
                      color = AmberSecondary,
                      fontSize = 9.sp
                    )
                  }
                }

                Spacer(Modifier.height(2.dp))
                Text(
                  text = "${mech.type.displayName} • ${mech.notes}",
                  style = MaterialTheme.typography.bodySmall,
                  color = Color(0xFF8B949E)
                )

                Spacer(Modifier.height(4.dp))
                val mmRev = mech.mmPerRevolution
                Text(
                  text = "Travel/rev: ${String.format(Locale.US, "%.3f", mmRev)} mm (${String.format(Locale.US, "%.4f", mmRev / 25.4)}\")",
                  style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                  ),
                  color = Color(0xFFE6EDF3)
                )
              }

              Row(verticalAlignment = Alignment.CenterVertically) {
                if (isActive) {
                  Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Active",
                    tint = CyanPrimary,
                    modifier = Modifier.padding(end = 8.dp)
                  )
                } else {
                  OutlinedButton(
                    onClick = { onSelectMechanism(mech) },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("select_preset_${mech.id}")
                  ) {
                    Text("Select", color = Color(0xFFE6EDF3), style = MaterialTheme.typography.labelMedium)
                  }
                }

                if (mech.isCustom) {
                  IconButton(
                    onClick = { onDeleteCustomMechanism(mech.id) },
                    modifier = Modifier.size(32.dp).testTag("delete_custom_${mech.id}")
                  ) {
                    Icon(
                      imageVector = Icons.Default.Delete,
                      contentDescription = "Delete Custom Mechanism",
                      tint = Color(0xFFFF5252),
                      modifier = Modifier.size(18.dp)
                    )
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  if (showAddDialog) {
    AddCustomMechanismDialog(
      onDismiss = { showAddDialog = false },
      onSave = { name, type, dia, lead, teeth, pitch, arcR, gear, notes ->
        onSaveCustomMechanism(name, type, dia, lead, teeth, pitch, arcR, gear, notes)
        showAddDialog = false
      }
    )
  }
}

@Composable
private fun AddCustomMechanismDialog(
  onDismiss: () -> Unit,
  onSave: (
    name: String,
    type: MechanismType,
    diameterMm: Double,
    leadMm: Double,
    teethCount: Int,
    toothPitchMm: Double,
    arcRadiusMm: Double,
    gearRatio: Double,
    notes: String
  ) -> Unit
) {
  var name by remember { mutableStateOf("") }
  var selectedType by remember { mutableStateOf(MechanismType.WHEEL) }
  var diameterMmStr by remember { mutableStateOf("100.0") }
  var leadMmStr by remember { mutableStateOf("8.0") }
  var teethCountStr by remember { mutableStateOf("20") }
  var toothPitchMmStr by remember { mutableStateOf("2.0") }
  var arcRadiusMmStr by remember { mutableStateOf("100.0") }
  var gearRatioStr by remember { mutableStateOf("1.0") }
  var notes by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Add Custom Mechanism", color = Color(0xFFF0F6FC)) },
    text = {
      Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Profile Name") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("custom_name_input")
        )

        // Type selection chips
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          MechanismType.values().forEach { t ->
            FilterChip(
              selected = selectedType == t,
              onClick = { selectedType = t },
              label = { Text(t.displayName.split(" ")[0], fontSize = 10.sp) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = CyanPrimary.copy(alpha = 0.2f),
                selectedLabelColor = CyanPrimary
              )
            )
          }
        }

        when (selectedType) {
          MechanismType.WHEEL -> {
            OutlinedTextField(
              value = diameterMmStr,
              onValueChange = { diameterMmStr = it },
              label = { Text("Wheel / Roller Diameter (mm)") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
              singleLine = true,
              modifier = Modifier.fillMaxWidth().testTag("custom_param_diameter")
            )
          }
          MechanismType.LEAD_SCREW -> {
            OutlinedTextField(
              value = leadMmStr,
              onValueChange = { leadMmStr = it },
              label = { Text("Lead / Pitch per rev (mm)") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
              singleLine = true,
              modifier = Modifier.fillMaxWidth().testTag("custom_param_lead")
            )
          }
          MechanismType.BELT_PULLEY -> {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              OutlinedTextField(
                value = teethCountStr,
                onValueChange = { teethCountStr = it },
                label = { Text("Teeth Count") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1f).testTag("custom_param_teeth")
              )
              OutlinedTextField(
                value = toothPitchMmStr,
                onValueChange = { toothPitchMmStr = it },
                label = { Text("Pitch (mm)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.weight(1f).testTag("custom_param_pitch")
              )
            }
          }
          MechanismType.ARC -> {
            OutlinedTextField(
              value = arcRadiusMmStr,
              onValueChange = { arcRadiusMmStr = it },
              label = { Text("Arc Radius (mm)") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
              singleLine = true,
              modifier = Modifier.fillMaxWidth().testTag("custom_param_arc")
            )
          }
        }

        OutlinedTextField(
          value = gearRatioStr,
          onValueChange = { gearRatioStr = it },
          label = { Text("Gear Ratio (e.g. 1.0 = direct, 3.0 = 3:1)") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("custom_param_gearratio")
        )

        OutlinedTextField(
          value = notes,
          onValueChange = { notes = it },
          label = { Text("Notes / Specification") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("custom_param_notes")
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val validName = if (name.isNotBlank()) name else "${selectedType.displayName} Custom"
          onSave(
            validName,
            selectedType,
            diameterMmStr.toDoubleOrNull() ?: 100.0,
            leadMmStr.toDoubleOrNull() ?: 8.0,
            teethCountStr.toIntOrNull() ?: 20,
            toothPitchMmStr.toDoubleOrNull() ?: 2.0,
            arcRadiusMmStr.toDoubleOrNull() ?: 100.0,
            gearRatioStr.toDoubleOrNull() ?: 1.0,
            notes
          )
        },
        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
        modifier = Modifier.testTag("save_custom_mechanism_button")
      ) {
        Text("Save Preset", color = Color(0xFF00363D), fontWeight = FontWeight.Bold)
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
