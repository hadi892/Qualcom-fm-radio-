# Changelog

All notable changes to the Qualcomm Snapdragon FM Radio project will be documented in this file.

## [1.0.0] - 2026-07-22

### Added
- **LineageOS FM Stack Conversion:** Migrated LineageOS `android_vendor_qcom_opensource_fm-commonsys` into a standalone Gradle Android Studio project targeting Android 16 (SDK 36).
- **Jetpack Compose UI:** Modern, responsive FM Tuner interface with custom rotary dial, frequency slider, RSSI signal meter, RDS live text ticker, and stereo indicator.
- **Hardware HAL Auto-Detection:** Built-in `QualcommHalDetector` inspecting `/dev/radio0`, `vendor.qti.hardware.fm@1.0`, and `/vendor/lib64/libfmpal.so`.
- **Stock ROM Compatibility:** Diagnostic reporting and interactive fallback engine for unrooted devices running stock Samsung One UI where SELinux restricts vendor character device access.
- **Foreground Service (`FmRadioService`):** Background FM audio playback service with notification controls, MediaSession integration, and AudioFocus management.
- **Audio Routing Helper:** Automatic detection of wired headset antenna connection and toggle for loudspeaker routing.
- **Station Presets (Room Persistence):** Persistent storage for favorite FM frequencies, custom station names, and category tags.
- **GitHub Actions Integration:** Automated CI/CD pipeline using JDK 21 and Gradle 9.x to build, sign, and publish release APKs to GitHub Releases.
