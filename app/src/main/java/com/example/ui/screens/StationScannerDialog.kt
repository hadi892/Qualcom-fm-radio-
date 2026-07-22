package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PresetEntity
import com.example.fmservice.FmBand
import com.example.ui.theme.AmberPrimary
import kotlinx.coroutines.delay

@Composable
fun StationScannerDialog(
    band: FmBand,
    onDismiss: () -> Unit,
    onSaveScanResults: (List<PresetEntity>) -> Unit
) {
    var isScanning by remember { mutableStateOf(true) }
    var currentProgressFreqKhz by remember { mutableStateOf(band.minFreqKhz) }
    val foundStations = remember { mutableStateListOf<PresetEntity>() }

    val progressFraction = animateFloatAsState(
        targetValue = (currentProgressFreqKhz - band.minFreqKhz).toFloat() / (band.maxFreqKhz - band.minFreqKhz),
        label = "scan_progress"
    )

    LaunchedEffect(Unit) {
        var freq = band.minFreqKhz
        while (freq <= band.maxFreqKhz) {
            currentProgressFreqKhz = freq
            delay(40) // Simulating hardware spectrum sweep
            // Sample active channels
            if (freq in listOf(88100, 91500, 94700, 98100, 100700, 104300, 107900)) {
                val mhzStr = String.format("%.1f", freq / 1000.0f)
                val preset = PresetEntity(
                    frequencyKhz = freq,
                    stationName = "Station $mhzStr FM",
                    tag = if (freq > 100000) "Pop" else "Rock"
                )
                if (!foundStations.any { it.frequencyKhz == freq }) {
                    foundStations.add(preset)
                }
            }
            freq += band.stepKhz
        }
        isScanning = false
    }

    AlertDialog(
        onDismissRequest = { if (!isScanning) onDismiss() },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Radio,
                    contentDescription = null,
                    tint = AmberPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isScanning) "Scanning FM Band..." else "Scan Complete",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Frequency: ${String.format("%.1f MHz", currentProgressFreqKhz / 1000.0f)}",
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    color = AmberPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { progressFraction.value },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = AmberPrimary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Discovered Stations (${foundStations.size}):",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    items(foundStations) { station ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = station.formatMhz() + " MHz",
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = AmberPrimary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = station.stationName,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSaveScanResults(foundStations)
                    onDismiss()
                },
                enabled = !isScanning,
                modifier = Modifier.testTag("scan_save_button")
            ) {
                Text("Save All Presets (${foundStations.size})", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
