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

        if (devRadio0Exists) {
            val canReadDev = try {
                val f = File(PATH_DEV_RADIO0)
                f.canRead() || f.canWrite()
            } catch (e: Exception) {
                false
            }

            if (canReadDev) {
                code = HalStatusCode.AVAILABLE
                isHalAvailable = true
                message = "Hardware Qualcomm FM chip accessible via /dev/radio0."
            } else {
                code = HalStatusCode.SELINUX_DENIED
                isHalAvailable = false
                message = "Qualcomm FM chip present at /dev/radio0, but SELinux stock policy blocks access without root."
            }
        } else if (libfmpalExists || vendorHalImplExists) {
            code = HalStatusCode.BINDER_RESTRICTED
            isHalAvailable = false
            message = "Qualcomm vendor FM HAL libraries exist on SM-X216B, but Samsung One UI restricts third-party app binding."
        } else {
            code = HalStatusCode.DEV_NOT_FOUND
            isHalAvailable = false
            message = "Qualcomm FM character device not directly exposed to user space."
        }

        val isSimulationActive = !isHalAvailable

        return HalStatus(
            code = code,
            isHalAvailable = isHalAvailable,
            devRadio0Exists = devRadio0Exists,
            libfmpalExists = libfmpalExists,
            vendorHalImplExists = vendorHalImplExists,
            isHeadsetPluggedIn = isHeadsetPluggedIn,
            isSimulationActive = isSimulationActive,
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
