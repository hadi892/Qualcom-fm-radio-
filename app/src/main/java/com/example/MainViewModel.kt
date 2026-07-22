package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FmDatabase
import com.example.data.FmRepository
import com.example.data.PresetEntity
import com.example.fmjni.FmNativeBridge
import com.example.fmjni.HalStatus
import com.example.fmjni.QualcommHalDetector
import com.example.fmservice.FmBand
import com.example.fmservice.RdsData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FmRepository
    val presets: StateFlow<List<PresetEntity>>

    private val _halStatus = MutableStateFlow(QualcommHalDetector.inspectHalStatus(application))
    val halStatus: StateFlow<HalStatus> = _halStatus.asStateFlow()

    private val _currentFreqKhz = MutableStateFlow(100700)
    val currentFreqKhz: StateFlow<Int> = _currentFreqKhz.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _rdsData = MutableStateFlow(RdsData("QCOM-FM", "100.7 MHz Live FM", 10, -50, true))
    val rdsData: StateFlow<RdsData> = _rdsData.asStateFlow()

    private val _currentBand = MutableStateFlow(FmBand.US_EU)
    val currentBand: StateFlow<FmBand> = _currentBand.asStateFlow()

    private val _isSpeakerOutput = MutableStateFlow(false)
    val isSpeakerOutput: StateFlow<Boolean> = _isSpeakerOutput.asStateFlow()

    private val _isForceSimulation = MutableStateFlow(false)
    val isForceSimulation: StateFlow<Boolean> = _isForceSimulation.asStateFlow()

    init {
        val database = FmDatabase.getDatabase(application)
        repository = FmRepository(database.presetDao())
        presets = repository.allPresets.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Seed initial default stations if empty
        viewModelScope.launch {
            repository.allPresets.collect { list ->
                if (list.isEmpty()) {
                    val defaults = listOf(
                        PresetEntity(88100, "Jazz 88.1", "Jazz"),
                        PresetEntity(94700, "News 94.7", "News"),
                        PresetEntity(98100, "Rock 98.1", "Rock"),
                        PresetEntity(100700, "Qualcomm FM 100.7", "Pop"),
                        PresetEntity(104300, "Top 40 Pop", "Pop")
                    )
                    defaults.forEach { repository.savePreset(it) }
                }
            }
        }
    }

    fun refreshHalStatus() {
        _halStatus.value = QualcommHalDetector.inspectHalStatus(getApplication())
    }

    fun togglePlay() {
        _isPlaying.value = !_isPlaying.value
    }

    fun tuneTo(freqKhz: Int) {
        val clamped = freqKhz.coerceIn(_currentBand.value.minFreqKhz, _currentBand.value.maxFreqKhz)
        _currentFreqKhz.value = clamped
        FmNativeBridge.setTune(clamped)
        _rdsData.value = FmNativeBridge.getRdsData(clamped)
    }

    fun seek(scanUp: Boolean) {
        val newFreq = FmNativeBridge.seek(scanUp, _currentFreqKhz.value)
        tuneTo(newFreq)
    }

    fun toggleFavoriteCurrent() {
        val freq = _currentFreqKhz.value
        val existing = presets.value.find { it.frequencyKhz == freq }
        viewModelScope.launch {
            if (existing != null) {
                repository.removePreset(freq)
            } else {
                val mhzStr = String.format("%.1f", freq / 1000.0f)
                repository.savePreset(
                    PresetEntity(
                        frequencyKhz = freq,
                        stationName = "Station $mhzStr FM",
                        tag = "User Favorites"
                    )
                )
            }
        }
    }

    fun deletePreset(preset: PresetEntity) {
        viewModelScope.launch {
            repository.removePreset(preset.frequencyKhz)
        }
    }

    fun saveScanResults(results: List<PresetEntity>) {
        viewModelScope.launch {
            results.forEach { repository.savePreset(it) }
        }
    }

    fun setBand(band: FmBand) {
        _currentBand.value = band
        tuneTo(_currentFreqKhz.value)
    }

    fun toggleSpeaker() {
        _isSpeakerOutput.value = !_isSpeakerOutput.value
    }

    fun toggleForceSimulation(enabled: Boolean) {
        _isForceSimulation.value = false
    }
}
