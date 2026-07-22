package com.example.fmservice

enum class FmBand(
    val displayName: String,
    val minFreqKhz: Int,
    val maxFreqKhz: Int,
    val stepKhz: Int
) {
    US_EU("US / Europe (87.5 - 108.0 MHz)", 87500, 108000, 100),
    JAPAN("Japan Wide (76.0 - 95.0 MHz)", 76000, 95000, 100),
    WORLD("World Band (76.0 - 108.0 MHz)", 76000, 108000, 50);

    fun formatKhzToMhz(khz: Int): String {
        val mhz = khz / 1000.0f
        return String.format("%.1f", mhz)
    }
}
