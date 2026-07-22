package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PresetDao {

    @Query("SELECT * FROM fm_presets ORDER BY frequencyKhz ASC")
    fun getAllPresets(): Flow<List<PresetEntity>>

    @Query("SELECT * FROM fm_presets WHERE tag = :tag ORDER BY frequencyKhz ASC")
    fun getPresetsByTag(tag: String): Flow<List<PresetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: PresetEntity)

    @Delete
    suspend fun deletePreset(preset: PresetEntity)

    @Query("DELETE FROM fm_presets WHERE frequencyKhz = :frequencyKhz")
    suspend fun deletePresetByFreq(frequencyKhz: Int)

    @Query("SELECT * FROM scan_history ORDER BY frequencyKhz ASC")
    fun getScanHistory(): Flow<List<ScanHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScanResult(scanResult: ScanHistoryEntity)

    @Query("DELETE FROM scan_history")
    suspend fun clearScanHistory()
}
