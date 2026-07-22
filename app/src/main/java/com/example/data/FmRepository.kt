package com.example.data

import kotlinx.coroutines.flow.Flow

class FmRepository(private val presetDao: PresetDao) {

    val allPresets: Flow<List<PresetEntity>> = presetDao.getAllPresets()
    val scanHistory: Flow<List<ScanHistoryEntity>> = presetDao.getScanHistory()

    suspend fun savePreset(preset: PresetEntity) {
        presetDao.insertPreset(preset)
    }

    suspend fun removePreset(frequencyKhz: Int) {
        presetDao.deletePresetByFreq(frequencyKhz)
    }

    suspend fun saveScanResults(results: List<ScanHistoryEntity>) {
        presetDao.clearScanHistory()
        for (res in results) {
            presetDao.insertScanResult(res)
        }
    }
}
