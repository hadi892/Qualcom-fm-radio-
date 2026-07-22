package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FmDatabase
import com.example.data.FmRepository
import com.example.data.PresetEntity
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
        _rdsData.value = updateMockRds(clamped)
    }

    fun seek(scanUp: Boolean) {
        val step = if (scanUp) _currentBand.value.stepKhz else -_currentBand.value.stepKhz
        var newFreq = _currentFreqKhz.value + step
        if (newFreq > _currentBand.value.maxFreqKhz) newFreq = _currentBand.value.minFreqKhz
        if (newFreq < _currentBand.value.minFreqKhz) newFreq = _currentBand.value.maxFreqKhz
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
        _isForceSimulation.value = enabled
    }

    private fun updateMockRds(freqKhz: Int): RdsData {
        val mhz = freqKhz / 1000.0f
        return when (freqKhz) {
            88100 -> RdsData("JAZZ-88", "Miles Davis - So What (Live Studio Broadcast)", 15, -62, true)
            91500 -> RdsData("CLASSIC", "Beethoven - Symphony No. 9 in D minor", 14, -58, true)
            94700 -> RdsData("NEWS-94", "Hourly Global Headlines & Local Weather Updates", 1, -50, true)
            98100 -> RdsData("ROCK-FM", "Foo Fighters - The Pretender (98.1 FM)", 11, -55, true)
            100700 -> RdsData("QCOM-FM", "Qualcomm Snapdragon 695 FM HD Receiver Signal", 10, -48, true)
            104300 -> RdsData("POP-104", "Dua Lipa - Levitating (Top 40 Countdown)", 10, -52, true)
            107900 -> RdsData("CHILL-9", "Lo-Fi Ambient Beats for Focus & Relaxation", 9, -68, true)
            else -> RdsData("FM ${String.format("%.1f", mhz)}", "Stereo FM Radio Signal - ${String.format("%.1f", mhz)} MHz", 0, -72, true)
        }
    }
}
