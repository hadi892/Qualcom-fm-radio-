package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [PresetEntity::class, ScanHistoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class FmDatabase : RoomDatabase() {

    abstract fun presetDao(): PresetDao

    companion object {
        @Volatile
        private var INSTANCE: FmDatabase? = null

        fun getDatabase(context: Context): FmDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FmDatabase::class.java,
                    "qcom_fm_radio.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
