package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.PrecisionResult
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldTertiary
import java.util.Locale

@Composable
fun UnitResultCards(
  result: PrecisionResult,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current

  fun copyToClipboard(label: String, value: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    val clip = ClipData.newPlainText(label, value)
    clipboard?.setPrimaryClip(clip)
    Toast.makeText(context, "Copied $value", Toast.LENGTH_SHORT).show()
  }

  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    // Top Row: Primary Centimeters vs Primary Inches Cards
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // Metric (Centimeters)
      val formattedCm = String.format(Locale.US, "%.4f", result.distanceCm)
      HeroUnitCard(
        label = "CENTIMETERS",
        unit = "cm",
        value = formattedCm,
        accentColor = CyanPrimary,
        badge = String.format(Locale.US, "%.3f mm", result.distanceMm),
        modifier = Modifier.weight(1f).testTag("result_card_cm"),
        onCopy = { copyToClipboard("Centimeters", "$formattedCm cm") }
      )

      // Imperial (Inches)
      val formattedInches = String.format(Locale.US, "%.4f", result.distanceInches)
      HeroUnitCard(
        label = "INCHES",
        unit = "in",
        value = formattedInches,
        accentColor = AmberSecondary,
        badge = result.fractionalInch,
        modifier = Modifier.weight(1f).testTag("result_card_inches"),
        onCopy = { copyToClipboard("Inches", "$formattedInches in") }
      )
    }

    // Extended High-Precision Breakdown Card
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .testTag("breakdown_precision_card"),
      colors = CardDefaults.cardColors(containerColor = Color(0xFF121722)),
      shape = RoundedCornerShape(12.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Text(
          text = "PRECISION SCALE BREAKDOWN",
          style = MaterialTheme.typography.labelSmall,
          color = Color(0xFF7E8B9B),
          letterSpacing = 1.sp
        )

        // Millimeters & Microns
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          DetailUnitItem(
            label = "Millimeters (mm)",
            value = String.format(Locale.US, "%.4f mm", result.distanceMm),
            color = CyanPrimary
          )
          DetailUnitItem(
            label = "Microns (μm)",
            value = String.format(Locale.US, "%,.1f μm", result.distanceMicrons),
            color = EmeraldTertiary
          )
        }

        // Machinist Mils/Thou & Fractional Inches
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          DetailUnitItem(
            label = "Thousandths (thou / mil)",
            value = String.format(Locale.US, "%,.1f thou", result.distanceMilsThou),
            color = AmberSecondary
          )
          val errorSign = if (result.fractionalErrorThou >= 0) "+" else ""
          val errorText = String.format(Locale.US, " (%s%.1f thou)", errorSign, result.fractionalErrorThou)
          DetailUnitItem(
            label = "Fractional (±thou error)",
            value = "${result.fractionalInch}$errorText",
            color = Color(0xFFE6EDF3)
          )
        }

        // Formula Calculation Indicator
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF0A0E15))
            .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
          Text(
            text = "Formula: ${result.formulaSummary}",
            style = MaterialTheme.typography.bodySmall.copy(
              fontFamily = FontFamily.Monospace,
              fontSize = 11.sp
            ),
            color = Color(0xFF8B949E)
          )
        }
      }
    }
  }
}

@Composable
private fun HeroUnitCard(
  label: String,
  unit: String,
  value: String,
  accentColor: Color,
  badge: String,
  modifier: Modifier = Modifier,
  onCopy: () -> Unit
) {
  Card(
    modifier = modifier,
    colors = CardDefaults.cardColors(containerColor = Color(0xFF141A25)),
    shape = RoundedCornerShape(14.dp),
    border = CardDefaults.outlinedCardBorder().copy(
      brush = Brush.verticalGradient(
        listOf(accentColor.copy(alpha = 0.5f), Color(0xFF202938))
      )
    )
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
        Text(
          text = label,
          style = MaterialTheme.typography.labelSmall,
          color = Color(0xFF8B949E),
          letterSpacing = 1.sp
        )
        IconButton(
          onClick = onCopy,
          modifier = Modifier.size(28.dp).testTag("copy_button_$unit")
        ) {
          Icon(
            imageVector = Icons.Default.ContentCopy,
            contentDescription = "Copy $label",
            tint = accentColor.copy(alpha = 0.8f),
            modifier = Modifier.size(16.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(4.dp))

      Row(
        verticalAlignment = Alignment.Bottom
      ) {
        Text(
          text = value,
          style = MaterialTheme.typography.headlineSmall.copy(
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
          ),
          color = Color(0xFFF0F6FC)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = unit,
          style = MaterialTheme.typography.titleMedium,
          color = accentColor,
          modifier = Modifier.padding(bottom = 2.dp)
        )
      }

      Spacer(modifier = Modifier.height(6.dp))

      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(4.dp))
          .background(accentColor.copy(alpha = 0.12f))
          .padding(horizontal = 6.dp, vertical = 2.dp)
      ) {
        Text(
          text = badge,
          style = MaterialTheme.typography.labelSmall.copy(
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp
          ),
          color = accentColor
        )
      }
    }
  }
}

@Composable
private fun DetailUnitItem(
  label: String,
  value: String,
  color: Color,
  modifier: Modifier = Modifier
) {
  Column(modifier = modifier) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = Color(0xFF7E8B9B),
      fontSize = 10.sp
    )
    Text(
      text = value,
      style = MaterialTheme.typography.bodyMedium.copy(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.SemiBold
      ),
      color = color
    )
  }
}
