package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PresetEntity
import com.example.fmjni.HalStatus
import com.example.fmservice.FmBand
import com.example.fmservice.RdsData
import com.example.ui.components.FmDial
import com.example.ui.components.HalStatusCard
import com.example.ui.components.RdsVisualizer
import com.example.ui.components.SignalMeter
import com.example.ui.theme.AmberPrimary

@Composable
fun FmHomeScreen(
    halStatus: HalStatus,
    currentFreqKhz: Int,
    isPlaying: Boolean,
    rdsData: RdsData,
    band: FmBand,
    presets: List<PresetEntity>,
    isSpeakerOutput: Boolean,
    onTogglePlay: () -> Unit,
    onTuneFreqKhz: (Int) -> Unit,
    onSeek: (Boolean) -> Unit,
    onToggleSpeaker: () -> Unit,
    onToggleFavorite: () -> Unit,
    onOpenNumpad: () -> Unit,
    onOpenScan: () -> Unit,
    onOpenDiagnostics: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val isCurrentPresetSaved = presets.any { it.frequencyKhz == currentFreqKhz }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .verticalScroll(scrollState)
            .testTag("fm_home_screen")
    ) {
        // Qualcomm Snapdragon HAL Status Banner
        HalStatusCard(
            halStatus = halStatus,
            onOpenDiagnostics = onOpenDiagnostics
        )

        Spacer(modifier = Modifier.height(12.dp))

        // RDS Live Display Box
        RdsVisualizer(
            rdsData = rdsData,
            freqKhz = currentFreqKhz
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Signal Level Bar & Stereo Indicator
        SignalMeter(
            rssiDbm = rdsData.rssi,
            isStereo = rdsData.isStereo,
            isHeadsetPluggedIn = halStatus.isHeadsetPluggedIn
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Rotary Dial & Frequency Slider
        FmDial(
            currentFreqKhz = currentFreqKhz,
            band = band,
            onTuneFreqKhz = onTuneFreqKhz
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Primary Playback & Control Panel
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(vertical = 12.dp, horizontal = 8.dp)
                .testTag("control_panel")
        ) {
            // Seek Down
            IconButton(
                onClick = { onSeek(false) },
                modifier = Modifier.testTag("seek_down_button")
            ) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Seek Down",
                    modifier = Modifier.size(28.dp)
                )
            }

            // Direct Dial Numpad
            IconButton(
                onClick = onOpenNumpad,
                modifier = Modifier.testTag("numpad_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Dialpad,
                    contentDescription = "Direct Frequency Dial",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Main Play/Pause Button
            FilledIconButton(
                onClick = onTogglePlay,
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = AmberPrimary),
                modifier = Modifier
                    .size(64.dp)
                    .testTag("play_pause_button")
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause FM" else "Play FM",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(36.dp)
                )
            }

            // Audio Output Toggle (Headset / Speaker)
            OutlinedIconButton(
                onClick = onToggleSpeaker,
                modifier = Modifier.testTag("speaker_toggle_button")
            ) {
                Icon(
                    imageVector = if (isSpeakerOutput) Icons.Default.Speaker else Icons.Default.Headset,
                    contentDescription = "Audio Routing Output",
                    tint = if (isSpeakerOutput) AmberPrimary else MaterialTheme.colorScheme.onSurface
                )
            }

            // Seek Up
            IconButton(
                onClick = { onSeek(true) },
                modifier = Modifier.testTag("seek_up_button")
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Seek Up",
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Quick Action Bar (Scan & Bookmark)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier.testTag("bookmark_star_button")
            ) {
                Icon(
                    imageVector = if (isCurrentPresetSaved) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = "Favorite Station",
                    tint = AmberPrimary
                )
            }

            Text(
                text = if (isCurrentPresetSaved) "Bookmarked" else "Bookmark Station",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(AmberPrimary.copy(alpha = 0.2f))
                    .clickable { onOpenScan() }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .testTag("open_scan_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Auto Scan",
                        tint = AmberPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Auto Scan Band",
                        color = AmberPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Presets Horizontal Carousel
        if (presets.isNotEmpty()) {
            Text(
                text = "Quick Station Presets",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(presets) { preset ->
                    val isSelected = preset.frequencyKhz == currentFreqKhz
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) AmberPrimary else MaterialTheme.colorScheme.surface)
                            .clickable { onTuneFreqKhz(preset.frequencyKhz) }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Column {
                            Text(
                                text = preset.formatMhz() + " MHz",
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp
                            )
                            Text(
                                text = preset.stationName,
                                fontSize = 11.sp,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
