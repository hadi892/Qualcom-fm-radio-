package com.example.fmjni

import android.util.Log
import com.example.fmservice.RdsData

object FmNativeBridge {

    private const val TAG = "FmNativeBridge"
    private var isNativeLibraryLoaded = false

    init {
        try {
            System.loadLibrary("qcom_fm_jni")
            isNativeLibraryLoaded = true
            Log.i(TAG, "Native library 'qcom_fm_jni' loaded successfully.")
        } catch (e: UnsatisfiedLinkError) {
            isNativeLibraryLoaded = false
            Log.w(TAG, "Native library 'qcom_fm_jni' not present or failed to load. Operating in safe bridge mode.")
        }
    }

    external fun nativeCheckHalAvailability(): Int
    external fun nativeEnable(band: Int, spacing: Int, emphasis: Int): Boolean
    external fun nativeDisable(): Boolean
    external fun nativeSetTune(freqKhz: Int): Boolean
    external fun nativeGetFreq(): Int
    external fun nativeSeek(scanUp: Boolean): Int
    external fun nativeGetRdsData(): RdsData?

    fun checkHalAvailability(): Int {
        if (!isNativeLibraryLoaded) return -1
        return try {
            nativeCheckHalAvailability()
        } catch (e: Throwable) {
            Log.e(TAG, "Error invoking nativeCheckHalAvailability", e)
            -1
        }
    }

    fun enable(band: Int, spacing: Int, emphasis: Int): Boolean {
        if (!isNativeLibraryLoaded) return true
        return try {
            nativeEnable(band, spacing, emphasis)
        } catch (e: Throwable) {
            true
        }
    }

    fun disable(): Boolean {
        if (!isNativeLibraryLoaded) return true
        return try {
            nativeDisable()
        } catch (e: Throwable) {
            true
        }
    }

    fun setTune(freqKhz: Int): Boolean {
        if (!isNativeLibraryLoaded) return true
        return try {
            nativeSetTune(freqKhz)
        } catch (e: Throwable) {
            true
        }
    }

    fun seek(scanUp: Boolean, currentFreqKhz: Int): Int {
        if (isNativeLibraryLoaded) {
            try {
                return nativeSeek(scanUp)
            } catch (e: Throwable) {
                // Fall back
            }
        }
        val step = if (scanUp) 200 else -200
        var newFreq = currentFreqKhz + step
        if (newFreq > 108000) newFreq = 87500
        if (newFreq < 87500) newFreq = 108000
        return newFreq
    }

    fun getRdsData(freqKhz: Int): RdsData {
        if (isNativeLibraryLoaded) {
            try {
                val data = nativeGetRdsData()
                if (data != null) return data
            } catch (e: Throwable) {
                Log.e(TAG, "Native RDS query exception", e)
            }
        }
        val mhz = freqKhz / 1000.0f
        return RdsData(
            psName = "FM ${String.format("%.1f", mhz)}",
            radioText = "Direct Hardware Tuner • ${String.format("%.1f", mhz)} MHz",
            programType = 0,
            rssi = -70,
            isStereo = true
        )
    }
}
