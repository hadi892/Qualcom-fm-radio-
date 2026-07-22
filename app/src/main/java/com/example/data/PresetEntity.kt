package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fm_presets")
data class PresetEntity(
    @PrimaryKey val frequencyKhz: Int,
    val stationName: String,
    val tag: String = "General",
    val isFavorite: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun formatMhz(): String = String.format("%.1f", frequencyKhz / 1000.0f)
}
