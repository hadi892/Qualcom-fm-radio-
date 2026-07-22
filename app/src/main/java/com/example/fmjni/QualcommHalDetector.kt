package com.example.fmjni

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.util.Log
import java.io.File

object QualcommHalDetector {

    private const val TAG = "QualcommHalDetector"

    private const val PATH_DEV_RADIO0 = "/dev/radio0"
    private const val PATH_LIBFMPAL = "/vendor/lib64/libfmpal.so"
    private const val PATH_LIBFMPAL_32 = "/vendor/lib/libfmpal.so"
    private const val PATH_HAL_IMPL = "/vendor/lib64/hw/vendor.qti.hardware.fm@1.0-impl.so"
    private const val PATH_HAL_IMPL_32 = "/vendor/lib/hw/vendor.qti.hardware.fm@1.0-impl.so"

    fun inspectHalStatus(context: Context): HalStatus {
        val devRadio0Exists = File(PATH_DEV_RADIO0).exists()
        val libfmpalExists = File(PATH_LIBFMPAL).exists() || File(PATH_LIBFMPAL_32).exists()
        val vendorHalImplExists = File(PATH_HAL_IMPL).exists() || File(PATH_HAL_IMPL_32).exists()

        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        val isHeadsetPluggedIn = isWiredHeadsetConnected(audioManager)

        Log.i(TAG, "HAL Inspection - devRadio0=$devRadio0Exists, libfmpal=$libfmpalExists, halImpl=$vendorHalImplExists, headset=$isHeadsetPluggedIn")

        val code: HalStatusCode
        val isHalAvailable: Boolean
        val message: String

        val jniNativeStatus = FmNativeBridge.checkHalAvailability()
        val nativeHalAvailable = (jniNativeStatus == 0)

        if (nativeHalAvailable) {
            code = HalStatusCode.AVAILABLE
            isHalAvailable = true
            message = "Hardware Qualcomm FM HAL active and directly bound via native JNI."
        } else if (devRadio0Exists) {
            code = HalStatusCode.SELINUX_DENIED
            isHalAvailable = false
            message = "Qualcomm FM device present at /dev/radio0. Direct execution attempted via JNI."
        } else if (libfmpalExists || vendorHalImplExists) {
            code = HalStatusCode.BINDER_RESTRICTED
            isHalAvailable = false
            message = "Qualcomm vendor FM HAL libraries detected (/vendor/lib64/libfmpal.so)."
        } else {
            code = HalStatusCode.DEV_NOT_FOUND
            isHalAvailable = false
            message = "Qualcomm FM character device not exposed to user space."
        }

        return HalStatus(
            code = code,
            isHalAvailable = isHalAvailable,
            devRadio0Exists = devRadio0Exists,
            libfmpalExists = libfmpalExists,
            vendorHalImplExists = vendorHalImplExists,
            isHeadsetPluggedIn = isHeadsetPluggedIn,
            isSimulationActive = false,
            detailedMessage = message
        )
    }

    private fun isWiredHeadsetConnected(audioManager: AudioManager?): Boolean {
        if (audioManager == null) return false
        val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        for (device in devices) {
            if (device.type == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
                device.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||
                device.type == AudioDeviceInfo.TYPE_USB_HEADSET) {
                return true
            }
        }
        return false
    }
}
