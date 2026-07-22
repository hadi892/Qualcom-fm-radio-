package com.example.fmjni

enum class HalStatusCode(val description: String) {
    AVAILABLE("Qualcomm FM Vendor HAL is fully functional and accessible"),
    DEV_NOT_FOUND("Qualcomm FM character device (/dev/radio0) not found"),
    SELINUX_DENIED("SELinux policy restricts access to Qualcomm FM character device"),
    BINDER_RESTRICTED("Qualcomm vendor HAL service (vendor.qti.hardware.fm@1.0) restricted on stock ROM"),
    NO_HEADSET("Wired headset is unplugged. Antenna required for hardware reception."),
    UNKNOWN("Unknown HAL status")
}

data class HalStatus(
    val code: HalStatusCode,
    val isHalAvailable: Boolean,
    val devRadio0Exists: Boolean,
    val libfmpalExists: Boolean,
    val vendorHalImplExists: Boolean,
    val isHeadsetPluggedIn: Boolean,
    val isSimulationActive: Boolean,
    val detailedMessage: String
)
