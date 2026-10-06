package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.entity.MeasurementLogEntity
import com.example.ui.theme.SamsungDarkBackground
import com.example.ui.theme.SamsungDivider
import com.example.ui.theme.SamsungGreenAccent
import com.example.ui.theme.SamsungRedAccent
import com.example.ui.theme.SamsungTextPrimary
import com.example.ui.theme.SamsungTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SamsungHistoryScreen(
  logs: List<MeasurementLogEntity>,
  totalOdometerCm: Double?,
  onSelectLog: (Double) -> Unit,
  onClearAll: () -> Unit,
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val dateFormat = SimpleDateFormat("MMM d, HH:mm", Locale.US)

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
      IconButton(onClick = onBack, modifier = Modifier.testTag("history_back_button")) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
      }
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = "History",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White
      )
      Spacer(modifier = Modifier.weight(1f))
      if (logs.isNotEmpty()) {
        IconButton(onClick = onClearAll, modifier = Modifier.testTag("history_clear_icon")) {
          Icon(Icons.Default.DeleteSweep, contentDescription = "Clear all", tint = SamsungRedAccent)
        }
      }
    }

    // Cumulative Total Banner
    val totalCm = totalOdometerCm ?: 0.0
    val totalInches = totalCm / 2.54
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "TOTAL LOGGED TRAVEL",
        fontSize = 11.sp,
        color = SamsungTextSecondary,
        letterSpacing = 1.sp
      )
      Text(
        text = "${String.format(Locale.US, "%.3f", totalCm)} cm  •  ${String.format(Locale.US, "%.3f", totalInches)} in",
        fontSize = 12.sp,
        color = SamsungGreenAccent,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace
      )
    }

    HorizontalDivider(color = SamsungDivider, thickness = 0.5.dp)

    if (logs.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .weight(1f),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(Icons.Default.History, contentDescription = null, tint = Color(0xFF3A3A3C), modifier = Modifier.size(54.dp))
          Spacer(modifier = Modifier.height(12.dp))
          Text(text = "No history", color = SamsungTextSecondary, fontSize = 16.sp)
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .testTag("samsung_history_list")
      ) {
        items(logs, key = { it.id }) { log ->
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .clickable {
                onSelectLog(log.degrees)
                Toast.makeText(context, "Loaded ${String.format(Locale.US, "%.2f°", log.degrees)}", Toast.LENGTH_SHORT).show()
                onBack()
              }
              .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.End
          ) {
            // Calculation Expression Line
            Text(
              text = "${String.format(Locale.US, "%.2f°", log.degrees)} (${log.mechanismName})",
              fontSize = 17.sp,
              color = SamsungTextSecondary,
              fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(2.dp))
            // Result Line
            Text(
              text = "= ${String.format(Locale.US, "%.4f", log.distanceCm)} cm  •  ${String.format(Locale.US, "%.4f", log.distanceInches)} in",
              fontSize = 20.sp,
              fontWeight = FontWeight.Medium,
              color = SamsungGreenAccent
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = dateFormat.format(Date(log.timestamp)),
              fontSize = 11.sp,
              color = Color(0xFF636366)
            )
          }
          HorizontalDivider(color = SamsungDivider, thickness = 0.5.dp)
        }
      }

      // Bottom "Clear history" button (Samsung One UI style)
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        contentAlignment = Alignment.Center
      ) {
        Button(
          onClick = onClearAll,
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C1C1E)),
          modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
          Text("Clear history", color = SamsungRedAccent, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
      }
    }
  }
}
