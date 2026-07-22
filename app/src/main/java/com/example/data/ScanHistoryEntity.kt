package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_history")
data class ScanHistoryEntity(
    @PrimaryKey val frequencyKhz: Int,
    val signalRssi: Int,
    val stationName: String,
    val ptyName: String,
    val lastSeenTime: Long = System.currentTimeMillis()
)
