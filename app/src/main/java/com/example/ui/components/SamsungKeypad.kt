package com.example.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SamsungFunctionKeyBg
import com.example.ui.theme.SamsungGreenAccent
import com.example.ui.theme.SamsungNumberKeyBg
import com.example.ui.theme.SamsungRedAccent

enum class KeypadButtonType {
  NUMBER,
  FUNCTION,
  OPERATOR,
  EQUALS,
  CLEAR
}

data class KeypadItem(
  val label: String,
  val type: KeypadButtonType,
  val action: () -> Unit
)

@Composable
fun SamsungKeypad(
  onDigit: (String) -> Unit,
  onOperator: (String) -> Unit,
  onClear: () -> Unit,
  onEquals: () -> Unit,
  onParentheses: () -> Unit,
  onPlusMinus: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val vibrator = remember {
    context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
  }

  fun haptic() {
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator?.vibrate(VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE))
      } else {
        @Suppress("DEPRECATION")
        vibrator?.vibrate(10)
      }
    } catch (_: Exception) {}
  }

  val rows = listOf(
    listOf(
      KeypadItem("C", KeypadButtonType.CLEAR) { haptic(); onClear() },
      KeypadItem("( )", KeypadButtonType.FUNCTION) { haptic(); onParentheses() },
      KeypadItem("±", KeypadButtonType.FUNCTION) { haptic(); onPlusMinus() },
      KeypadItem("÷", KeypadButtonType.OPERATOR) { haptic(); onOperator("÷") }
    ),
    listOf(
      KeypadItem("7", KeypadButtonType.NUMBER) { haptic(); onDigit("7") },
      KeypadItem("8", KeypadButtonType.NUMBER) { haptic(); onDigit("8") },
      KeypadItem("9", KeypadButtonType.NUMBER) { haptic(); onDigit("9") },
      KeypadItem("×", KeypadButtonType.OPERATOR) { haptic(); onOperator("×") }
    ),
    listOf(
      KeypadItem("4", KeypadButtonType.NUMBER) { haptic(); onDigit("4") },
      KeypadItem("5", KeypadButtonType.NUMBER) { haptic(); onDigit("5") },
      KeypadItem("6", KeypadButtonType.NUMBER) { haptic(); onDigit("6") },
      KeypadItem("−", KeypadButtonType.OPERATOR) { haptic(); onOperator("−") }
    ),
    listOf(
      KeypadItem("1", KeypadButtonType.NUMBER) { haptic(); onDigit("1") },
      KeypadItem("2", KeypadButtonType.NUMBER) { haptic(); onDigit("2") },
      KeypadItem("3", KeypadButtonType.NUMBER) { haptic(); onDigit("3") },
      KeypadItem("+", KeypadButtonType.OPERATOR) { haptic(); onOperator("+") }
    ),
    listOf(
      KeypadItem("0", KeypadButtonType.NUMBER) { haptic(); onDigit("0") },
      KeypadItem("00", KeypadButtonType.NUMBER) { haptic(); onDigit("00") },
      KeypadItem(".", KeypadButtonType.NUMBER) { haptic(); onDigit(".") },
      KeypadItem("=", KeypadButtonType.EQUALS) { haptic(); onEquals() }
    )
  )

  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 12.dp, vertical = 6.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    rows.forEach { rowItems ->
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        rowItems.forEach { item ->
          SamsungKeypadButton(
            item = item,
            modifier = Modifier
              .weight(1f)
              .aspectRatio(1.15f)
          )
        }
      }
    }
  }
}

@Composable
fun SamsungKeypadButton(
  item: KeypadItem,
  modifier: Modifier = Modifier
) {
  val (bgColor, textColor, fontSize) = when (item.type) {
    KeypadButtonType.NUMBER -> Triple(SamsungNumberKeyBg, Color.White, 26.sp)
    KeypadButtonType.FUNCTION -> Triple(SamsungFunctionKeyBg, Color(0xFFE5E5EA), 22.sp)
    KeypadButtonType.OPERATOR -> Triple(SamsungFunctionKeyBg, SamsungGreenAccent, 28.sp)
    KeypadButtonType.EQUALS -> Triple(SamsungGreenAccent, Color.White, 28.sp)
    KeypadButtonType.CLEAR -> Triple(SamsungFunctionKeyBg, SamsungRedAccent, 24.sp)
  }

  Box(
    modifier = modifier
      .clip(CircleShape)
      .background(bgColor)
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = ripple(bounded = true, color = Color.White.copy(alpha = 0.2f)),
        onClick = item.action
      )
      .testTag("keypad_btn_${item.label}"),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = item.label,
      fontSize = fontSize,
      fontWeight = FontWeight.Medium,
      color = textColor
    )
  }
}
