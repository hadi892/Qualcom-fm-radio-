package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.StatusGreen

@Composable
fun SignalMeter(
    rssiDbm: Int,
    isStereo: Boolean,
    isHeadsetPluggedIn: Boolean,
    modifier: Modifier = Modifier
) {
    // Map RSSI (-100 dBm to -30 dBm) to 5 visual bars
    val activeBars = when {
        rssiDbm > -50 -> 5
        rssiDbm > -65 -> 4
        rssiDbm > -78 -> 3
        rssiDbm > -90 -> 2
        else -> 1
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("signal_meter_row")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "RSSI: ",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.padding(horizontal = 6.dp)
            ) {
                for (i in 1..5) {
                    val barHeight = (6 + i * 3).dp
                    val isActive = i <= activeBars
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 1.5.dp)
                            .width(4.dp)
                            .height(barHeight)
                            .clip(RoundedCornerShape(1.dp))
                            .background(if (isActive) AmberPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = "$rssiDbm dBm",
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isStereo) StatusGreen.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (isStereo) "STEREO" else "MONO",
                    color = if (isStereo) StatusGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isHeadsetPluggedIn) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (isHeadsetPluggedIn) "ANTENNA OK" else "NO ANTENNA",
                    color = if (isHeadsetPluggedIn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
