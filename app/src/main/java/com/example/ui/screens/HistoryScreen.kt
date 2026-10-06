package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.entity.MeasurementLogEntity
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.CyanPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
  logs: List<MeasurementLogEntity>,
  totalOdometerCm: Double?,
  onDeleteLog: (Long) -> Unit,
  onClearAllLogs: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var showClearConfirmDialog by remember { mutableStateOf(false) }

  fun copyLogToClipboard(log: MeasurementLogEntity) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    val text = "${log.title}\n" +
      "Mechanism: ${log.mechanismName}\n" +
      "Rotation: ${String.format(Locale.US, "%.2f°", log.degrees)} (${String.format(Locale.US, "%.3f", log.revolutions)} rev)\n" +
      "Distance: ${String.format(Locale.US, "%.4f", log.distanceCm)} cm (${String.format(Locale.US, "%.4f", log.distanceInches)} in / ${log.fractionalInches})\n" +
      if (log.notes.isNotBlank()) "Notes: ${log.notes}" else ""

    val clip = ClipData.newPlainText("Measurement Log", text)
    clipboard?.setPrimaryClip(clip)
    Toast.makeText(context, "Log copied to clipboard", Toast.LENGTH_SHORT).show()
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // 1. Cumulative Odometer Banner
    val totalCm = totalOdometerCm ?: 0.0
    val totalInches = totalCm / 2.54
    val totalMeters = totalCm / 100.0

    Card(
      modifier = Modifier
        .fillMaxWidth()
        .testTag("odometer_banner_card"),
      colors = CardDefaults.cardColors(containerColor = Color(0xFF131924)),
      shape = RoundedCornerShape(14.dp)
    ) {
      Column(
        modifier = Modifier.padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "CUMULATIVE MEASUREMENT ODOMETER",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF8B949E),
            letterSpacing = 1.sp
          )
          if (logs.isNotEmpty()) {
            IconButton(
              onClick = { showClearConfirmDialog = true },
              modifier = Modifier.size(28.dp).testTag("clear_all_logs_button")
            ) {
              Icon(
                imageVector = Icons.Default.DeleteSweep,
                contentDescription = "Clear All Logs",
                tint = Color(0xFFFF5252),
                modifier = Modifier.size(18.dp)
              )
            }
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column {
            Text(
              text = String.format(Locale.US, "%.3f cm", totalCm),
              style = MaterialTheme.typography.headlineSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
              ),
              color = CyanPrimary
            )
            Text(
              text = "(${String.format(Locale.US, "%.4f", totalMeters)} m)",
              style = MaterialTheme.typography.bodySmall,
              color = Color(0xFF8B949E)
            )
          }

          Column(horizontalAlignment = Alignment.End) {
            Text(
              text = String.format(Locale.US, "%.3f in", totalInches),
              style = MaterialTheme.typography.headlineSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
              ),
              color = AmberSecondary
            )
            Text(
              text = "(${String.format(Locale.US, "%.3f", totalInches / 12.0)} ft)",
              style = MaterialTheme.typography.bodySmall,
              color = Color(0xFF8B949E)
            )
          }
        }
      }
    }

    // 2. Log Count & Subheader
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "SAVED RECORDS (${logs.size})",
        style = MaterialTheme.typography.labelMedium,
        color = Color(0xFF8B949E),
        fontWeight = FontWeight.SemiBold
      )
    }

    // 3. List or Empty State
    if (logs.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(32.dp)
          .testTag("logs_empty_state"),
        contentAlignment = Alignment.Center
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(
            imageVector = Icons.Default.History,
            contentDescription = "No Logs",
            tint = Color(0xFF3E4C5E),
            modifier = Modifier.size(48.dp)
          )
          Text(
            text = "No saved measurements yet",
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFF8B949E)
          )
          Text(
            text = "Use 'Save Measurement' on the converter screen to log precision passes and track total accumulated distance.",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF5A6678),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
          )
        }
      }
    } else {
      val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US) }

      LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("logs_lazy_column"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        items(logs, key = { it.id }) { log ->
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .testTag("log_card_${log.id}"),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131924)),
            shape = RoundedCornerShape(12.dp)
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = log.title,
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                  color = Color(0xFFF0F6FC)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                  IconButton(
                    onClick = { copyLogToClipboard(log) },
                    modifier = Modifier.size(28.dp).testTag("copy_log_${log.id}")
                  ) {
                    Icon(
                      imageVector = Icons.Default.ContentCopy,
                      contentDescription = "Copy Log",
                      tint = CyanPrimary,
                      modifier = Modifier.size(16.dp)
                    )
                  }
                  IconButton(
                    onClick = { onDeleteLog(log.id) },
                    modifier = Modifier.size(28.dp).testTag("delete_log_${log.id}")
                  ) {
                    Icon(
                      imageVector = Icons.Default.Delete,
                      contentDescription = "Delete Log",
                      tint = Color(0xFF8B949E),
                      modifier = Modifier.size(16.dp)
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.height(4.dp))

              Text(
                text = "${log.mechanismName} • ${String.format(Locale.US, "%.2f°", log.degrees)} (${String.format(Locale.US, "%.3f", log.revolutions)} rev)",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF8B949E)
              )

              Spacer(modifier = Modifier.height(6.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(
                  text = "${String.format(Locale.US, "%.4f", log.distanceCm)} cm",
                  style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                  ),
                  color = CyanPrimary
                )
                Text(
                  text = "${String.format(Locale.US, "%.4f", log.distanceInches)}\" (${log.fractionalInches})",
                  style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                  ),
                  color = AmberSecondary
                )
              }

              if (log.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "Notes: ${log.notes}",
                  style = MaterialTheme.typography.bodySmall,
                  color = Color(0xFF8B949E)
                )
              }

              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = dateFormat.format(Date(log.timestamp)),
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF5A6678)
              )
            }
          }
        }
      }
    }
  }

  // Clear All Confirmation Dialog
  if (showClearConfirmDialog) {
    AlertDialog(
      onDismissRequest = { showClearConfirmDialog = false },
      title = { Text("Clear All Measurement Logs?") },
      text = { Text("This will permanently remove all saved measurements and reset the cumulative odometer.") },
      confirmButton = {
        Button(
          onClick = {
            onClearAllLogs()
            showClearConfirmDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))
        ) {
          Text("Clear All", color = Color.White)
        }
      },
      dismissButton = {
        TextButton(onClick = { showClearConfirmDialog = false }) {
          Text("Cancel", color = Color(0xFF8B949E))
        }
      },
      containerColor = Color(0xFF131A26)
    )
  }
}
