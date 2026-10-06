package com.example.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkOutline
import java.util.Locale
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun PrecisionRotaryDial(
  currentDegrees: Double,
  onDegreesChanged: (Double) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val vibrator = remember {
    context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
  }

  fun triggerTickHaptic() {
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator?.vibrate(VibrationEffect.createOneShot(12, VibrationEffect.DEFAULT_AMPLITUDE))
      } else {
        @Suppress("DEPRECATION")
        vibrator?.vibrate(12)
      }
    } catch (_: Exception) {}
  }

  var lastTouchAngleRad by remember { mutableFloatStateOf(0f) }
  var lastTickSector by remember { mutableDoubleStateOf(currentDegrees) }

  Column(
    modifier = modifier.fillMaxWidth(),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Box(
      modifier = Modifier
        .size(280.dp)
        .testTag("rotary_dial_canvas_box")
        .pointerInput(Unit) {
          detectDragGestures(
            onDragStart = { offset ->
              val centerX = size.width / 2f
              val centerY = size.height / 2f
              lastTouchAngleRad = atan2(offset.y - centerY, offset.x - centerX)
            },
            onDrag = { change, _ ->
              change.consume()
              val centerX = size.width / 2f
              val centerY = size.height / 2f
              val touchX = change.position.x - centerX
              val touchY = change.position.y - centerY
              val newAngleRad = atan2(touchY, touchX)

              var deltaRad = newAngleRad - lastTouchAngleRad
              if (deltaRad > PI) deltaRad -= (2 * PI).toFloat()
              if (deltaRad < -PI) deltaRad += (2 * PI).toFloat()

              val deltaDeg = Math.toDegrees(deltaRad.toDouble())
              val updated = currentDegrees + deltaDeg
              lastTouchAngleRad = newAngleRad

              // Tick haptics on every 15 degrees
              if (kotlin.math.abs(updated - lastTickSector) >= 15.0) {
                lastTickSector = updated
                triggerTickHaptic()
              }

              onDegreesChanged(updated)
            }
          )
        },
      contentAlignment = Alignment.Center
    ) {
      Canvas(
        modifier = Modifier
          .fillMaxSize()
          .testTag("rotary_dial_canvas")
      ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val outerRadius = (size.minDimension / 2f) - 8.dp.toPx()
        val innerRadius = outerRadius - 26.dp.toPx()

        // Outer track background ring
        drawCircle(
          color = Color(0xFF141A24),
          radius = outerRadius,
          center = center,
          style = Stroke(width = 24.dp.toPx())
        )

        // Subtle track border
        drawCircle(
          color = DarkOutline,
          radius = outerRadius + 12.dp.toPx(),
          center = center,
          style = Stroke(width = 1.5.dp.toPx())
        )
        drawCircle(
          color = DarkOutline,
          radius = outerRadius - 12.dp.toPx(),
          center = center,
          style = Stroke(width = 1.5.dp.toPx())
        )

        // Radial ticks around the dial (0° to 360°)
        for (i in 0 until 360 step 5) {
          val angleRad = Math.toRadians(i.toDouble() - 90.0)
          val isMajor = i % 30 == 0
          val isMedium = i % 10 == 0 && !isMajor

          val tickLen = when {
            isMajor -> 18.dp.toPx()
            isMedium -> 12.dp.toPx()
            else -> 6.dp.toPx()
          }

          val tickWidth = when {
            isMajor -> 2.5.dp.toPx()
            isMedium -> 1.5.dp.toPx()
            else -> 1.0.dp.toPx()
          }

          val tickColor = when {
            isMajor -> CyanPrimary
            isMedium -> Color(0xFF8B949E)
            else -> Color(0xFF3B4454)
          }

          val startRadius = outerRadius + 8.dp.toPx() - tickLen
          val endRadius = outerRadius + 8.dp.toPx()

          val startX = center.x + (startRadius * cos(angleRad)).toFloat()
          val startY = center.y + (startRadius * sin(angleRad)).toFloat()
          val endX = center.x + (endRadius * cos(angleRad)).toFloat()
          val endY = center.y + (endRadius * sin(angleRad)).toFloat()

          drawLine(
            color = tickColor,
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = tickWidth,
            cap = StrokeCap.Round
          )
        }

        // Active angle sweep indicator arc (0 to normalized angle within [0, 360))
        val normalizedAngle = ((currentDegrees % 360.0) + 360.0) % 360.0
        val sweepAngle = normalizedAngle.toFloat()

        drawArc(
          brush = Brush.sweepGradient(
            colors = listOf(
              CyanPrimary.copy(alpha = 0.2f),
              CyanPrimary.copy(alpha = 0.8f),
              CyanPrimary
            ),
            center = center
          ),
          startAngle = -90f,
          sweepAngle = sweepAngle,
          useCenter = false,
          topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
          size = Size(outerRadius * 2, outerRadius * 2),
          style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
        )

        // Precision needle pointer
        val needleAngleRad = Math.toRadians(currentDegrees - 90.0)
        val needleLength = outerRadius + 10.dp.toPx()
        val needleStartRadius = innerRadius - 10.dp.toPx()

        val needleX = center.x + (needleLength * cos(needleAngleRad)).toFloat()
        val needleY = center.y + (needleLength * sin(needleAngleRad)).toFloat()
        val needleStartX = center.x + (needleStartRadius * cos(needleAngleRad)).toFloat()
        val needleStartY = center.y + (needleStartRadius * sin(needleAngleRad)).toFloat()

        // Glow behind needle tip
        drawCircle(
          color = CyanPrimary.copy(alpha = 0.5f),
          radius = 7.dp.toPx(),
          center = Offset(needleX, needleY)
        )

        // Needle line
        drawLine(
          color = CyanPrimary,
          start = Offset(needleStartX, needleStartY),
          end = Offset(needleX, needleY),
          strokeWidth = 3.dp.toPx(),
          cap = StrokeCap.Round
        )

        // Pointer tip bead
        drawCircle(
          color = Color.White,
          radius = 3.5.dp.toPx(),
          center = Offset(needleX, needleY)
        )

        // Zero reference mark at the top
        drawCircle(
          color = AmberSecondary,
          radius = 4.dp.toPx(),
          center = Offset(center.x, center.y - outerRadius - 15.dp.toPx())
        )
      }

      // Center Bezel Content (Degrees readout & Revolutions)
      Surface(
        modifier = Modifier
          .size(148.dp)
          .clip(CircleShape),
        color = Color(0xFF0F141D),
        tonalElevation = 6.dp,
        shape = CircleShape,
        border = CardDefaults.outlinedCardBorder().copy(
          brush = Brush.linearGradient(
            listOf(DarkOutline, Color(0xFF1E2636))
          )
        )
      ) {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
        ) {
          Text(
            text = "ROTATION",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF8B949E),
            letterSpacing = 1.5.sp
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = String.format(Locale.US, "%.2f°", currentDegrees),
            style = MaterialTheme.typography.titleLarge.copy(
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold
            ),
            color = CyanPrimary
          )
          Spacer(modifier = Modifier.height(2.dp))
          val revs = currentDegrees / 360.0
          Text(
            text = String.format(Locale.US, "%.3f rev", revs),
            style = MaterialTheme.typography.bodySmall.copy(
              fontFamily = FontFamily.Monospace
            ),
            color = AmberSecondary
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Vernier / Step Adjustment Controls
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp),
      colors = CardDefaults.cardColors(containerColor = Color(0xFF131924)),
      shape = RoundedCornerShape(12.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "PRECISION STEP ADJUSTMENT",
          style = MaterialTheme.typography.labelSmall,
          color = Color(0xFF7E8B9B),
          letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(6.dp))

        // Row of micro adjustment buttons
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly
        ) {
          StepButton(label = "-90°", onClick = { onDegreesChanged(currentDegrees - 90.0) })
          StepButton(label = "-15°", onClick = { onDegreesChanged(currentDegrees - 15.0) })
          StepButton(label = "-1°", onClick = { onDegreesChanged(currentDegrees - 1.0) })
          StepButton(label = "-0.1°", onClick = { onDegreesChanged(currentDegrees - 0.1) })
          IconButton(
            onClick = { onDegreesChanged(0.0) },
            modifier = Modifier.testTag("reset_degrees_button")
          ) {
            Icon(
              imageVector = Icons.Default.RestartAlt,
              contentDescription = "Reset Angle to Zero",
              tint = AmberSecondary
            )
          }
          StepButton(label = "+0.1°", onClick = { onDegreesChanged(currentDegrees + 0.1) })
          StepButton(label = "+1°", onClick = { onDegreesChanged(currentDegrees + 1.0) })
          StepButton(label = "+15°", onClick = { onDegreesChanged(currentDegrees + 15.0) })
          StepButton(label = "+90°", onClick = { onDegreesChanged(currentDegrees + 90.0) })
        }
      }
    }
  }
}

@Composable
private fun StepButton(
  label: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  OutlinedButton(
    onClick = onClick,
    modifier = modifier.testTag("step_button_$label"),
    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 4.dp),
    shape = RoundedCornerShape(6.dp)
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall.copy(
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium
      ),
      color = Color(0xFFE6EDF3)
    )
  }
}
