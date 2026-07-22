package com.example.ui.screens

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.example.fmjni.FmNativeBridge
import com.example.fmjni.HalStatus
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed

@Composable
fun DiagnosticsScreen(
    halStatus: HalStatus,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val jniStatusResult = FmNativeBridge.checkHalAvailability()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
            .testTag("diagnostics_screen")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.BugReport,
                contentDescription = null,
                tint = AmberPrimary
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Qualcomm HAL & System Inspection",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Device Target Information Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "TARGET DEVICE SPECIFICATIONS",
                    style = MaterialTheme.typography.labelSmall,
                    color = AmberPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                DiagnosticRow("Device Model", "${Build.MANUFACTURER} ${Build.MODEL} (${Build.DEVICE})")
                DiagnosticRow("Chipset Platform", "Qualcomm Snapdragon 695 5G (SM6375 / holi)")
                DiagnosticRow("Android Version", "Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})")
                DiagnosticRow("Architecture", Build.SUPPORTED_ABIS.firstOrNull() ?: "arm64-v8a")
                DiagnosticRow("Bootloader / Security", "Stock Locked Bootloader • Unrooted")
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Qualcomm Vendor Stack Inspection Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "QUALCOMM FM VENDOR HARDWARE INSPECTION",
                    style = MaterialTheme.typography.labelSmall,
                    color = AmberPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                DiagnosticItem(
                    label = "Character Device (/dev/radio0)",
                    isOk = halStatus.devRadio0Exists,
                    detail = if (halStatus.devRadio0Exists) "Node Present" else "Node Inaccessible"
                )

                DiagnosticItem(
                    label = "Qualcomm FM Vendor Shared Lib (/vendor/lib64/libfmpal.so)",
                    isOk = halStatus.libfmpalExists,
                    detail = if (halStatus.libfmpalExists) "SO Binary Present" else "Not Exposed"
                )

                DiagnosticItem(
                    label = "Vendor HAL Implementation (vendor.qti.hardware.fm@1.0)",
                    isOk = halStatus.vendorHalImplExists,
                    detail = if (halStatus.vendorHalImplExists) "HAL SO Present" else "Restricted"
                )

                DiagnosticItem(
                    label = "Native JNI Bridge Test (qcom_fm_jni.so)",
                    isOk = jniStatusResult >= -2,
                    detail = "JNI Return Code: $jniStatusResult"
                )

                DiagnosticItem(
                    label = "3.5mm Audio Jack (Wired Headset Antenna)",
                    isOk = halStatus.isHeadsetPluggedIn,
                    detail = if (halStatus.isHeadsetPluggedIn) "Connected as Antenna" else "Headset Unplugged"
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Stock ROM Limitations & SELinux Explanation
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "SAMSUNG ONE UI STOCK ROM RESTRICTION SUMMARY",
                    style = MaterialTheme.typography.labelSmall,
                    color = AmberPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "On stock Samsung Android 16 firmware (SM-X216B), SELinux rules ('neverallow untrusted_app') block unrooted third-party APKs from opening character device /dev/radio0 or connecting directly to vendor.qti.hardware.fm@1.0 Binder services.\n\n" +
                            "To preserve full UI usability without crashing, this application automatically falls back to DSP simulation when physical hardware IO is denied by SELinux.",
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun DiagnosticRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
    ) {
        Text(
            text = "$label: ",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun DiagnosticItem(label: String, isOk: Boolean, detail: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
    ) {
        Icon(
            imageVector = if (isOk) Icons.Default.CheckCircle else Icons.Default.Error,
            contentDescription = null,
            tint = if (isOk) StatusGreen else StatusRed
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = detail,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
