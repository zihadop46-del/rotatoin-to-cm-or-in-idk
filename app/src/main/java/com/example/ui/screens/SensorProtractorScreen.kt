package com.example.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Input
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.MechanismConfig
import com.example.domain.PrecisionCalculator
import com.example.sensor.RotationSensorManager
import com.example.sensor.SensorRotationState
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkOutline
import com.example.ui.theme.EmeraldTertiary
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SensorProtractorScreen(
  sensorManager: RotationSensorManager,
  sensorState: SensorRotationState,
  activeMechanism: MechanismConfig,
  onApplyToMainDial: (Double) -> Unit,
  onQuickSave: (Double, String) -> Unit,
  modifier: Modifier = Modifier
) {
  // Automatically start sensor listener on entering this screen, stop on leave
  DisposableEffect(Unit) {
    sensorManager.start()
    onDispose {
      sensorManager.stop()
    }
  }

  val currentDegrees = sensorState.currentDegrees
  val liveResult = PrecisionCalculator.calculateFromDegrees(currentDegrees, activeMechanism)

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Header & Status Card
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .testTag("sensor_status_card"),
      colors = CardDefaults.cardColors(containerColor = Color(0xFF131924)),
      shape = RoundedCornerShape(14.dp)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "DIGITAL ROTATION SENSOR",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF8B949E),
            letterSpacing = 1.sp
          )
          Text(
            text = if (sensorState.isAvailable) "Hardware Gyro Active" else "Sensor Unavailable",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = if (sensorState.isAvailable) EmeraldTertiary else Color(0xFFFF5252)
          )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          if (sensorState.isListening) {
            OutlinedButton(
              onClick = { sensorManager.stop() },
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.testTag("sensor_stop_button")
            ) {
              Icon(Icons.Default.Stop, contentDescription = "Stop", tint = Color(0xFFFF5252))
              Spacer(Modifier.width(4.dp))
              Text("Stop", color = Color(0xFFFF5252))
            }
          } else {
            Button(
              onClick = { sensorManager.start() },
              colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.testTag("sensor_start_button")
            ) {
              Icon(Icons.Default.PlayArrow, contentDescription = "Start", tint = Color(0xFF00363D))
              Spacer(Modifier.width(4.dp))
              Text("Start", color = Color(0xFF00363D))
            }
          }
        }
      }
    }

    // 2. Interactive Protractor / Gyro Dial
    Box(
      modifier = Modifier
        .size(260.dp)
        .testTag("sensor_dial_box"),
      contentAlignment = Alignment.Center
    ) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val outerRadius = (size.minDimension / 2f) - 6.dp.toPx()

        // Background gauge plate
        drawCircle(
          color = Color(0xFF101520),
          radius = outerRadius,
          center = center
        )
        drawCircle(
          color = DarkOutline,
          radius = outerRadius,
          center = center,
          style = Stroke(width = 2.dp.toPx())
        )

        // Radial protractor scale ticks
        for (i in 0 until 360 step 15) {
          val angleRad = Math.toRadians(i.toDouble() - 90.0)
          val isMajor = i % 45 == 0
          val tickLen = if (isMajor) 16.dp.toPx() else 8.dp.toPx()
          val tickColor = if (isMajor) CyanPrimary else Color(0xFF4A5568)
          val tickWidth = if (isMajor) 2.dp.toPx() else 1.dp.toPx()

          val startX = center.x + ((outerRadius - tickLen) * cos(angleRad)).toFloat()
          val startY = center.y + ((outerRadius - tickLen) * sin(angleRad)).toFloat()
          val endX = center.x + (outerRadius * cos(angleRad)).toFloat()
          val endY = center.y + (outerRadius * sin(angleRad)).toFloat()

          drawLine(
            color = tickColor,
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = tickWidth
          )
        }

        // Crosshairs
        drawLine(
          color = Color(0xFF1F2937),
          start = Offset(center.x, center.y - outerRadius + 20.dp.toPx()),
          end = Offset(center.x, center.y + outerRadius - 20.dp.toPx()),
          strokeWidth = 1.dp.toPx()
        )
        drawLine(
          color = Color(0xFF1F2937),
          start = Offset(center.x - outerRadius + 20.dp.toPx(), center.y),
          end = Offset(center.x + outerRadius - 20.dp.toPx(), center.y),
          strokeWidth = 1.dp.toPx()
        )

        // Rotating Protractor Needle
        val needleRad = Math.toRadians(currentDegrees - 90.0)
        val needleLen = outerRadius - 8.dp.toPx()
        val needleX = center.x + (needleLen * cos(needleRad)).toFloat()
        val needleY = center.y + (needleLen * sin(needleRad)).toFloat()

        drawLine(
          color = CyanPrimary,
          start = center,
          end = Offset(needleX, needleY),
          strokeWidth = 3.dp.toPx(),
          cap = StrokeCap.Round
        )

        drawCircle(
          color = Color.White,
          radius = 4.dp.toPx(),
          center = Offset(needleX, needleY)
        )
      }

      // Center Readout Display
      Surface(
        modifier = Modifier
          .size(136.dp)
          .clip(CircleShape),
        color = Color(0xFF0C1017),
        shape = CircleShape,
        border = CardDefaults.outlinedCardBorder()
      ) {
        Column(
          modifier = Modifier.fillMaxSize(),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
        ) {
          Text(
            text = if (sensorState.isHeld) "HOLD" else "ANGLE",
            style = MaterialTheme.typography.labelSmall,
            color = if (sensorState.isHeld) AmberSecondary else Color(0xFF8B949E),
            letterSpacing = 1.5.sp
          )
          Text(
            text = String.format(Locale.US, "%.2f°", currentDegrees),
            style = MaterialTheme.typography.headlineSmall.copy(
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold
            ),
            color = CyanPrimary
          )
          Text(
            text = String.format(Locale.US, "%.3f rev", liveResult.revolutions),
            style = MaterialTheme.typography.bodySmall.copy(
              fontFamily = FontFamily.Monospace
            ),
            color = AmberSecondary
          )
        }
      }
    }

    // 3. Sensor Control Action Buttons: TARE & HOLD
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Button(
        onClick = { sensorManager.tare() },
        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
          .weight(1f)
          .height(48.dp)
          .testTag("tare_sensor_button")
      ) {
        Icon(Icons.Default.Refresh, contentDescription = "Tare / Zero", tint = Color(0xFF00363D))
        Spacer(Modifier.width(6.dp))
        Text("Tare / Zero 0°", color = Color(0xFF00363D), fontWeight = FontWeight.Bold)
      }

      OutlinedButton(
        onClick = { sensorManager.toggleHold() },
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
          .weight(1f)
          .height(48.dp)
          .testTag("hold_sensor_button")
      ) {
        Icon(
          imageVector = if (sensorState.isHeld) Icons.Default.LockOpen else Icons.Default.Lock,
          contentDescription = "Hold / Freeze Angle",
          tint = AmberSecondary
        )
        Spacer(Modifier.width(6.dp))
        Text(
          text = if (sensorState.isHeld) "Resume" else "Hold Angle",
          color = AmberSecondary,
          fontWeight = FontWeight.SemiBold
        )
      }
    }

    // 4. Live Converted Distance Cards
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Card(
        modifier = Modifier
          .weight(1f)
          .testTag("sensor_live_cm_card"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131924)),
        shape = RoundedCornerShape(12.dp)
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Text(
            text = "LIVE DISTANCE (CM)",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF8B949E)
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = String.format(Locale.US, "%.4f", liveResult.distanceCm),
            style = MaterialTheme.typography.titleLarge.copy(
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold
            ),
            color = CyanPrimary
          )
          Text(
            text = "${String.format(Locale.US, "%.2f", liveResult.distanceMm)} mm",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF8B949E)
          )
        }
      }

      Card(
        modifier = Modifier
          .weight(1f)
          .testTag("sensor_live_inches_card"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131924)),
        shape = RoundedCornerShape(12.dp)
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Text(
            text = "LIVE DISTANCE (IN)",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF8B949E)
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = String.format(Locale.US, "%.4f\"", liveResult.distanceInches),
            style = MaterialTheme.typography.titleLarge.copy(
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold
            ),
            color = AmberSecondary
          )
          Text(
            text = liveResult.fractionalInch,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF8B949E)
          )
        }
      }
    }

    // 5. Actions: Apply to Main Dial & Quick Save to Log
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      OutlinedButton(
        onClick = { onApplyToMainDial(currentDegrees) },
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
          .weight(1f)
          .testTag("apply_sensor_to_dial_button")
      ) {
        Icon(Icons.Default.Input, contentDescription = "Apply to Main Dial", tint = CyanPrimary)
        Spacer(Modifier.width(6.dp))
        Text("Send to Dial", color = Color(0xFFE6EDF3))
      }

      Button(
        onClick = {
          onQuickSave(currentDegrees, "Live Sensor Measurement (${String.format(Locale.US, "%.1f°", currentDegrees)})")
        },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F2A3D)),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
          .weight(1f)
          .testTag("sensor_quick_save_button")
      ) {
        Icon(Icons.Default.BookmarkAdd, contentDescription = "Quick Save", tint = CyanPrimary)
        Spacer(Modifier.width(6.dp))
        Text("Quick Log", color = CyanPrimary)
      }
    }

    // 6. Practical Setup Guide
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = Color(0xFF0F141F)),
      shape = RoundedCornerShape(12.dp)
    ) {
      Column(
        modifier = Modifier.padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        Text(
          text = "HOW TO USE GYRO PROTRACTOR",
          style = MaterialTheme.typography.labelSmall,
          color = AmberSecondary,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "1. Place phone flat against the rotating surface, rotary table, or lathe chuck.\n" +
            "2. Tap 'Tare / Zero' to set initial orientation to 0.00°.\n" +
            "3. Rotate the mechanism. Real-time degrees and resulting travel in centimeters/inches update continuously.",
          style = MaterialTheme.typography.bodySmall,
          color = Color(0xFF8B949E),
          lineHeight = 18.sp
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))
  }
}
