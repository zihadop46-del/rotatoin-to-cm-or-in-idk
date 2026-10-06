package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.ChangeCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.ExpressionEvaluator
import com.example.domain.MechanismConfig
import com.example.domain.PrecisionCalculator
import com.example.domain.PrecisionResult
import com.example.ui.components.PrecisionRotaryDial
import com.example.ui.components.SamsungKeypad
import com.example.ui.theme.SamsungDarkBackground
import com.example.ui.theme.SamsungDivider
import com.example.ui.theme.SamsungGreenAccent
import com.example.ui.theme.SamsungRedAccent
import com.example.ui.theme.SamsungSurface
import com.example.ui.theme.SamsungTextPrimary
import com.example.ui.theme.SamsungTextSecondary
import java.util.Locale
import kotlinx.coroutines.launch

enum class DisplayUnit(val label: String, val suffix: String) {
  CM("Centimeters", "cm"),
  INCH("Inches", "in"),
  MM("Millimeters", "mm"),
  MICRON("Microns", "μm"),
  THOU("Thou (mils)", "thou"),
  FRACTION("Fractional", "")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SamsungCalculatorScreen(
  currentDegrees: Double,
  activeMechanism: MechanismConfig,
  allMechanisms: List<MechanismConfig>,
  precisionResult: PrecisionResult,
  onDegreesChanged: (Double) -> Unit,
  onMechanismSelected: (MechanismConfig) -> Unit,
  onOpenHistory: () -> Unit,
  onOpenUnitConverter: () -> Unit,
  onOpenProtractor: () -> Unit,
  onOpenStepper: () -> Unit,
  onOpenPresets: () -> Unit,
  onSaveMeasurement: (String, String) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()

  var expressionText by remember { mutableStateOf(String.format(Locale.US, "%.1f", currentDegrees)) }
  var selectedUnit by remember { mutableStateOf(DisplayUnit.CM) }
  var showMechanismMenu by remember { mutableStateOf(false) }
  var showDialBottomSheet by remember { mutableStateOf(false) }
  val dialSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  // Sync expression when degrees change from external sources (e.g. dial or sensor)
  LaunchedEffect(currentDegrees) {
    val evaluated = ExpressionEvaluator.evaluate(expressionText)
    if (evaluated == null || kotlin.math.abs(evaluated - currentDegrees) > 0.001) {
      expressionText = if (currentDegrees == currentDegrees.toLong().toDouble()) {
        "${currentDegrees.toLong()}"
      } else {
        String.format(Locale.US, "%.2f", currentDegrees)
      }
    }
  }

  fun updateExpression(newExpr: String) {
    expressionText = newExpr
    val evaluated = ExpressionEvaluator.evaluate(newExpr)
    if (evaluated != null) {
      onDegreesChanged(evaluated)
    }
  }

  fun appendDigit(digit: String) {
    if (expressionText == "0" && digit != ".") {
      updateExpression(digit)
    } else {
      updateExpression(expressionText + digit)
    }
  }

  fun appendOperator(op: String) {
    if (expressionText.isEmpty()) {
      updateExpression("0 $op ")
    } else {
      updateExpression("$expressionText $op ")
    }
  }

  fun backspace() {
    if (expressionText.isNotEmpty()) {
      val trimmed = expressionText.trimEnd()
      val updated = if (trimmed.length > 1) trimmed.substring(0, trimmed.length - 1).trimEnd() else "0"
      updateExpression(updated)
    }
  }

  fun clearAll() {
    updateExpression("0")
  }

  fun togglePlusMinus() {
    val evaluated = ExpressionEvaluator.evaluate(expressionText)
    if (evaluated != null) {
      val negated = -evaluated
      updateExpression(if (negated == negated.toLong().toDouble()) "${negated.toLong()}" else String.format(Locale.US, "%.2f", negated))
    }
  }

  fun appendParentheses() {
    val openCount = expressionText.count { it == '(' }
    val closeCount = expressionText.count { it == ')' }
    if (openCount > closeCount && expressionText.lastOrNull()?.isDigit() == true) {
      updateExpression("$expressionText)")
    } else {
      val prefix = if (expressionText.lastOrNull()?.isDigit() == true) "$expressionText × (" else "$expressionText("
      updateExpression(prefix)
    }
  }

  fun onEquals() {
    val evaluated = ExpressionEvaluator.evaluate(expressionText)
    if (evaluated != null) {
      val formatted = if (evaluated == evaluated.toLong().toDouble()) "${evaluated.toLong()}" else String.format(Locale.US, "%.4f", evaluated)
      expressionText = formatted
      onDegreesChanged(evaluated)
      // Save to history automatically on explicit equals calculation
      onSaveMeasurement(
        "${activeMechanism.name} @ $formatted°",
        "Calculated on Samsung Keypad: $formatted° = ${String.format(Locale.US, "%.4f", precisionResult.distanceCm)} cm (${String.format(Locale.US, "%.4f", precisionResult.distanceInches)} in)"
      )
      Toast.makeText(context, "Calculated & logged: $formatted°", Toast.LENGTH_SHORT).show()
    }
  }

  fun copyResult(value: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    clipboard?.setPrimaryClip(ClipData.newPlainText("Rotary Result", value))
    Toast.makeText(context, "Copied: $value", Toast.LENGTH_SHORT).show()
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(SamsungDarkBackground)
  ) {
    // Top Bar: Mechanism selector pill
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box {
        Surface(
          onClick = { showMechanismMenu = true },
          shape = RoundedCornerShape(16.dp),
          color = Color(0xFF1C1C1E),
          modifier = Modifier.testTag("samsung_mechanism_pill")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = activeMechanism.name,
              color = SamsungGreenAccent,
              fontSize = 13.sp,
              fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
              imageVector = Icons.Default.ExpandMore,
              contentDescription = "Select Mechanism",
              tint = SamsungGreenAccent,
              modifier = Modifier.size(16.dp)
            )
          }
        }

        DropdownMenu(
          expanded = showMechanismMenu,
          onDismissRequest = { showMechanismMenu = false },
          modifier = Modifier
            .background(Color(0xFF242426))
            .testTag("samsung_mechanism_dropdown")
        ) {
          allMechanisms.forEach { mech ->
            DropdownMenuItem(
              text = {
                Column {
                  Text(
                    text = mech.name,
                    color = if (mech.id == activeMechanism.id) SamsungGreenAccent else Color.White,
                    fontWeight = FontWeight.SemiBold
                  )
                  Text(
                    text = "${mech.type.displayName} • ${String.format(Locale.US, "%.2f", mech.mmPerRevolution)} mm/rev",
                    color = SamsungTextSecondary,
                    fontSize = 11.sp
                  )
                }
              },
              onClick = {
                onMechanismSelected(mech)
                showMechanismMenu = false
              },
              trailingIcon = {
                if (mech.id == activeMechanism.id) {
                  Icon(Icons.Default.Check, contentDescription = "Active", tint = SamsungGreenAccent)
                }
              }
            )
          }
        }
      }

      // Quick Unit Mode Selector Chips
      Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        listOf(DisplayUnit.CM, DisplayUnit.INCH, DisplayUnit.MM, DisplayUnit.THOU).forEach { unit ->
          val isSelected = selectedUnit == unit
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .background(if (isSelected) SamsungGreenAccent.copy(alpha = 0.2f) else Color(0xFF1C1C1E))
              .clickable { selectedUnit = unit }
              .padding(horizontal = 10.dp, vertical = 5.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = unit.suffix,
              color = if (isSelected) SamsungGreenAccent else SamsungTextSecondary,
              fontSize = 12.sp,
              fontWeight = FontWeight.Medium
            )
          }
        }
      }
    }

    // Samsung Calculator Display Area
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
        .padding(horizontal = 20.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.Bottom,
      horizontalAlignment = Alignment.End
    ) {
      // Primary Angle Input (Expression)
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
      ) {
        Text(
          text = expressionText,
          fontSize = if (expressionText.length > 9) 36.sp else 48.sp,
          fontWeight = FontWeight.Light,
          color = SamsungTextPrimary,
          textAlign = TextAlign.End,
          maxLines = 2
        )
        Text(
          text = "°",
          fontSize = 40.sp,
          fontWeight = FontWeight.Light,
          color = SamsungGreenAccent,
          modifier = Modifier.padding(start = 2.dp, bottom = 4.dp)
        )
      }

      Spacer(modifier = Modifier.height(6.dp))

      // Primary Converted Result in Samsung Calculator Green
      val (primaryDisplayVal, secondaryDisplayVal) = when (selectedUnit) {
        DisplayUnit.CM -> Pair(
          "${String.format(Locale.US, "%.4f", precisionResult.distanceCm)} cm",
          "${String.format(Locale.US, "%.4f", precisionResult.distanceInches)} in  •  ${precisionResult.fractionalInch}"
        )
        DisplayUnit.INCH -> Pair(
          "${String.format(Locale.US, "%.4f", precisionResult.distanceInches)} in",
          "${String.format(Locale.US, "%.4f", precisionResult.distanceCm)} cm  •  ${precisionResult.fractionalInch}"
        )
        DisplayUnit.MM -> Pair(
          "${String.format(Locale.US, "%.3f", precisionResult.distanceMm)} mm",
          "${String.format(Locale.US, "%,.1f", precisionResult.distanceMicrons)} μm  •  ${String.format(Locale.US, "%.4f", precisionResult.distanceInches)} in"
        )
        DisplayUnit.MICRON -> Pair(
          "${String.format(Locale.US, "%,.1f", precisionResult.distanceMicrons)} μm",
          "${String.format(Locale.US, "%.4f", precisionResult.distanceMm)} mm  •  ${String.format(Locale.US, "%.4f", precisionResult.distanceCm)} cm"
        )
        DisplayUnit.THOU -> Pair(
          "${String.format(Locale.US, "%,.1f", precisionResult.distanceMilsThou)} thou",
          "${String.format(Locale.US, "%.4f", precisionResult.distanceInches)} in  •  ${String.format(Locale.US, "%.4f", precisionResult.distanceMm)} mm"
        )
        DisplayUnit.FRACTION -> Pair(
          precisionResult.fractionalInch,
          "${String.format(Locale.US, "%.4f", precisionResult.distanceInches)} in  •  ${String.format(Locale.US, "%.4f", precisionResult.distanceCm)} cm"
        )
      }

      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .clickable { copyResult(primaryDisplayVal) }
          .testTag("samsung_result_text")
      ) {
        Text(
          text = "= $primaryDisplayVal",
          fontSize = 28.sp,
          fontWeight = FontWeight.Normal,
          color = SamsungGreenAccent
        )
      }

      Spacer(modifier = Modifier.height(2.dp))

      // Secondary summary (revolutions & imperial/metric equivalent)
      Text(
        text = "${String.format(Locale.US, "%.3f", precisionResult.revolutions)} rev  •  $secondaryDisplayVal",
        fontSize = 13.sp,
        color = SamsungTextSecondary,
        fontFamily = FontFamily.Monospace,
        textAlign = TextAlign.End
      )
    }

    HorizontalDivider(
      color = SamsungDivider,
      thickness = 1.dp,
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
    )

    // Samsung Calculator Toolbar (Icons Row)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        // 1. History
        IconButton(
          onClick = onOpenHistory,
          modifier = Modifier.size(36.dp).testTag("toolbar_history_button")
        ) {
          Icon(
            imageVector = Icons.Default.History,
            contentDescription = "History",
            tint = SamsungTextSecondary,
            modifier = Modifier.size(20.dp)
          )
        }

        // 2. Samsung Unit Converter Mode
        IconButton(
          onClick = onOpenUnitConverter,
          modifier = Modifier.size(36.dp).testTag("toolbar_converter_button")
        ) {
          Icon(
            imageVector = Icons.Default.Straighten,
            contentDescription = "Unit Converter",
            tint = SamsungTextSecondary,
            modifier = Modifier.size(20.dp)
          )
        }

        // 3. Interactive Precision Dial Bottom Sheet
        IconButton(
          onClick = { showDialBottomSheet = true },
          modifier = Modifier.size(36.dp).testTag("toolbar_dial_sheet_button")
        ) {
          Icon(
            imageVector = Icons.Default.Tune,
            contentDescription = "Rotary Dial",
            tint = SamsungGreenAccent,
            modifier = Modifier.size(20.dp)
          )
        }

        // 3. Hardware Gyro Protractor
        IconButton(
          onClick = onOpenProtractor,
          modifier = Modifier.size(36.dp).testTag("toolbar_protractor_button")
        ) {
          Icon(
            imageVector = Icons.Default.Explore,
            contentDescription = "Gyro Protractor",
            tint = SamsungTextSecondary,
            modifier = Modifier.size(20.dp)
          )
        }

        // 4. CNC / Stepper Motor Motion
        IconButton(
          onClick = onOpenStepper,
          modifier = Modifier.size(36.dp).testTag("toolbar_stepper_button")
        ) {
          Icon(
            imageVector = Icons.Default.PrecisionManufacturing,
            contentDescription = "CNC Stepper",
            tint = SamsungTextSecondary,
            modifier = Modifier.size(20.dp)
          )
        }

        // 5. Presets
        IconButton(
          onClick = onOpenPresets,
          modifier = Modifier.size(36.dp).testTag("toolbar_presets_button")
        ) {
          Icon(
            imageVector = Icons.Default.Straighten,
            contentDescription = "Mechanism Profiles",
            tint = SamsungTextSecondary,
            modifier = Modifier.size(20.dp)
          )
        }
      }

      // Backspace Button
      IconButton(
        onClick = { backspace() },
        modifier = Modifier.size(40.dp).testTag("toolbar_backspace_button")
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.Backspace,
          contentDescription = "Backspace",
          tint = SamsungGreenAccent,
          modifier = Modifier.size(22.dp)
        )
      }
    }

    // Iconic Samsung Calculator Keypad
    SamsungKeypad(
      onDigit = { appendDigit(it) },
      onOperator = { appendOperator(it) },
      onClear = { clearAll() },
      onEquals = { onEquals() },
      onParentheses = { appendParentheses() },
      onPlusMinus = { togglePlusMinus() },
      modifier = Modifier.padding(bottom = 8.dp)
    )
  }

  // Samsung-Styled Modal Bottom Sheet for the Interactive Precision Dial
  if (showDialBottomSheet) {
    ModalBottomSheet(
      onDismissRequest = { showDialBottomSheet = false },
      sheetState = dialSheetState,
      containerColor = Color(0xFF141416),
      contentColor = Color.White
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "PRECISION ROTARY DIAL",
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          color = SamsungGreenAccent,
          letterSpacing = 1.sp
        )
        Text(
          text = "Touch & drag perimeter to rotate dial smoothly",
          fontSize = 11.sp,
          color = SamsungTextSecondary
        )

        Spacer(modifier = Modifier.height(12.dp))

        PrecisionRotaryDial(
          currentDegrees = currentDegrees,
          onDegreesChanged = { newDeg ->
            onDegreesChanged(newDeg)
            expressionText = String.format(Locale.US, "%.2f", newDeg)
          }
        )

        Spacer(modifier = Modifier.height(16.dp))
      }
    }
  }
}
