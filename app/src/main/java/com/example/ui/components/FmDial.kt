package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fmservice.FmBand
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.CyanAccent
import kotlin.math.roundToInt

@Composable
fun FmDial(
    currentFreqKhz: Int,
    band: FmBand,
    onTuneFreqKhz: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentMhz = currentFreqKhz / 1000.0f
    val mhzStr = String.format("%.1f", currentMhz)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(18.dp)
            .testTag("fm_dial_container")
    ) {
        // Frequency Main Display
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Text(
                text = mhzStr,
                fontSize = 54.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = AmberPrimary,
                letterSpacing = (-1).sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "MHz",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        // Ticking Frequency Wheel Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF020617))
                .pointerInput(currentFreqKhz) {
                    detectHorizontalDragGestures { change, dragAmount ->
                        change.consume()
                        val deltaKhz = (-dragAmount * 10).roundToInt()
                        var newFreq = currentFreqKhz + deltaKhz
                        newFreq = (newFreq / band.stepKhz) * band.stepKhz
                        newFreq = newFreq.coerceIn(band.minFreqKhz, band.maxFreqKhz)
                        onTuneFreqKhz(newFreq)
                    }
                }
                .testTag("freq_canvas_wheel")
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val width = size.width
                val height = size.height
                val centerX = width / 2f

                val rangeMhz = 4.0f
                val pxPerMhz = width / rangeMhz

                val startMhz = (currentMhz - rangeMhz / 2f).coerceAtLeast(band.minFreqKhz / 1000.0f)
                val endMhz = (currentMhz + rangeMhz / 2f).coerceAtMost(band.maxFreqKhz / 1000.0f)

                var mhz = (startMhz * 10).roundToInt() / 10.0f
                while (mhz <= endMhz) {
                    val x = centerX + (mhz - currentMhz) * pxPerMhz
                    if (x in 0f..width) {
                        val isMajor = (mhz * 10).roundToInt() % 10 == 0
                        val isMid = (mhz * 10).roundToInt() % 5 == 0 && !isMajor

                        val lineLength = if (isMajor) 28.dp.toPx() else if (isMid) 18.dp.toPx() else 10.dp.toPx()
                        val lineColor = if (isMajor) AmberPrimary else if (isMid) CyanAccent else Color.DarkGray

                        drawLine(
                            color = lineColor,
                            start = Offset(x, height / 2f - lineLength / 2f),
                            end = Offset(x, height / 2f + lineLength / 2f),
                            strokeWidth = if (isMajor) 2.5.dp.toPx() else 1.5.dp.toPx()
                        )
                    }
                    mhz += (band.stepKhz / 1000.0f)
                }

                // Center Red Tuning Needle Indicator
                drawLine(
                    color = Color.Red,
                    start = Offset(centerX, 0f),
                    end = Offset(centerX, height),
                    strokeWidth = 3.dp.toPx()
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Fine Tune Buttons (+/- Step) and Smooth Slider
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            FilledTonalIconButton(
                onClick = {
                    val newFreq = (currentFreqKhz - band.stepKhz).coerceAtLeast(band.minFreqKhz)
                    onTuneFreqKhz(newFreq)
                },
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.testTag("fine_tune_minus")
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Tune Down"
                )
            }

            Slider(
                value = currentFreqKhz.toFloat(),
                onValueChange = { newValue ->
                    val rounded = ((newValue / band.stepKhz).roundToInt() * band.stepKhz)
                    onTuneFreqKhz(rounded.coerceIn(band.minFreqKhz, band.maxFreqKhz))
                },
                valueRange = band.minFreqKhz.toFloat()..band.maxFreqKhz.toFloat(),
                colors = SliderDefaults.colors(
                    thumbColor = AmberPrimary,
                    activeTrackColor = AmberPrimary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 10.dp)
                    .testTag("freq_slider")
            )

            FilledTonalIconButton(
                onClick = {
                    val newFreq = (currentFreqKhz + band.stepKhz).coerceAtMost(band.maxFreqKhz)
                    onTuneFreqKhz(newFreq)
                },
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.testTag("fine_tune_plus")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Tune Up"
                )
            }
        }
    }
}
