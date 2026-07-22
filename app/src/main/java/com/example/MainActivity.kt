package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.NumpadDialog
import com.example.ui.screens.DiagnosticsScreen
import com.example.ui.screens.FmHomeScreen
import com.example.ui.screens.PresetListScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StationScannerDialog
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.QualcommFmTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val requestPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            viewModel.refreshHalStatus()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        checkAndRequestPermissions()

        setContent {
            QualcommFmTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshHalStatus()
    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.RECORD_AUDIO)
        }
        if (permissions.isNotEmpty()) {
            requestPermissionsLauncher.launch(permissions.toTypedArray())
        }
    }
}

enum class NavigationTab(val label: String) {
    TUNER("Tuner"),
    PRESETS("Presets"),
    DIAGNOSTICS("Diagnostics"),
    SETTINGS("Settings")
}

@Composable
fun MainAppScreen(viewModel: MainViewModel) {
    var selectedTab by remember { mutableStateOf(NavigationTab.TUNER) }
    var showNumpadDialog by remember { mutableStateOf(false) }
    var showScanDialog by remember { mutableStateOf(false) }

    val halStatus by viewModel.halStatus.collectAsStateWithLifecycle()
    val currentFreqKhz by viewModel.currentFreqKhz.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val rdsData by viewModel.rdsData.collectAsStateWithLifecycle()
    val currentBand by viewModel.currentBand.collectAsStateWithLifecycle()
    val presets by viewModel.presets.collectAsStateWithLifecycle()
    val isSpeakerOutput by viewModel.isSpeakerOutput.collectAsStateWithLifecycle()
    val isForceSimulation by viewModel.isForceSimulation.collectAsStateWithLifecycle()

    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_navigation_bar")
            ) {
                NavigationBarItem(
                    selected = selectedTab == NavigationTab.TUNER,
                    onClick = { selectedTab = NavigationTab.TUNER },
                    icon = { Icon(Icons.Default.Radio, contentDescription = "FM Tuner") },
                    label = { Text("Tuner") },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = AmberPrimary.copy(alpha = 0.2f)),
                    modifier = Modifier.testTag("nav_tab_tuner")
                )
                NavigationBarItem(
                    selected = selectedTab == NavigationTab.PRESETS,
                    onClick = { selectedTab = NavigationTab.PRESETS },
                    icon = { Icon(Icons.Default.List, contentDescription = "Presets") },
                    label = { Text("Presets") },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = AmberPrimary.copy(alpha = 0.2f)),
                    modifier = Modifier.testTag("nav_tab_presets")
                )
                NavigationBarItem(
                    selected = selectedTab == NavigationTab.DIAGNOSTICS,
                    onClick = { selectedTab = NavigationTab.DIAGNOSTICS },
                    icon = { Icon(Icons.Default.BugReport, contentDescription = "Diagnostics") },
                    label = { Text("Diagnostics") },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = AmberPrimary.copy(alpha = 0.2f)),
                    modifier = Modifier.testTag("nav_tab_diagnostics")
                )
                NavigationBarItem(
                    selected = selectedTab == NavigationTab.SETTINGS,
                    onClick = { selectedTab = NavigationTab.SETTINGS },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = AmberPrimary.copy(alpha = 0.2f)),
                    modifier = Modifier.testTag("nav_tab_settings")
                )
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        val modifier = Modifier.padding(innerPadding)

        when (selectedTab) {
            NavigationTab.TUNER -> {
                FmHomeScreen(
                    halStatus = halStatus,
                    currentFreqKhz = currentFreqKhz,
                    isPlaying = isPlaying,
                    rdsData = rdsData,
                    band = currentBand,
                    presets = presets,
                    isSpeakerOutput = isSpeakerOutput,
                    onTogglePlay = { viewModel.togglePlay() },
                    onTuneFreqKhz = { freq -> viewModel.tuneTo(freq) },
                    onSeek = { scanUp -> viewModel.seek(scanUp) },
                    onToggleSpeaker = { viewModel.toggleSpeaker() },
                    onToggleFavorite = { viewModel.toggleFavoriteCurrent() },
                    onOpenNumpad = { showNumpadDialog = true },
                    onOpenScan = { showScanDialog = true },
                    onOpenDiagnostics = { selectedTab = NavigationTab.DIAGNOSTICS },
                    modifier = modifier
                )
            }

            NavigationTab.PRESETS -> {
                PresetListScreen(
                    presets = presets,
                    currentFreqKhz = currentFreqKhz,
                    onSelectPreset = { preset ->
                        viewModel.tuneTo(preset.frequencyKhz)
                        selectedTab = NavigationTab.TUNER
                    },
                    onDeletePreset = { preset -> viewModel.deletePreset(preset) },
                    modifier = modifier
                )
            }

            NavigationTab.DIAGNOSTICS -> {
                DiagnosticsScreen(
                    halStatus = halStatus,
                    modifier = modifier
                )
            }

            NavigationTab.SETTINGS -> {
                SettingsScreen(
                    currentBand = currentBand,
                    onSelectBand = { band -> viewModel.setBand(band) },
                    isForceSimulation = isForceSimulation,
                    onToggleForceSimulation = { viewModel.toggleForceSimulation(it) },
                    modifier = modifier
                )
            }
        }

        if (showNumpadDialog) {
            NumpadDialog(
                band = currentBand,
                onDismiss = { showNumpadDialog = false },
                onConfirmTuneKhz = { freq -> viewModel.tuneTo(freq) }
            )
        }

        if (showScanDialog) {
            StationScannerDialog(
                band = currentBand,
                onDismiss = { showScanDialog = false },
                onSaveScanResults = { scanned -> viewModel.saveScanResults(scanned) }
            )
        }
    }
}
