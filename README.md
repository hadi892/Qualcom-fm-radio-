# Qualcomm Snapdragon FM Radio (Standalone APK)

[![Build & Release APK](https://github.com/qualcomm-fm/android-fm-radio/actions/workflows/build.yml/badge.svg)](https://github.com/qualcomm-fm/android-fm-radio/actions)
[![License: Apache 2.0](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Target Android 16](https://img.shields.io/badge/Android-16%20%28SDK%2036%29-green.svg)](https://developer.android.com/about/versions/16)

A standalone, non-system Android Studio project and APK for Qualcomm Snapdragon FM Radio chipsets, derived from the LineageOS `android_vendor_qcom_opensource_fm-commonsys` open-source stack.

This project removes all dependencies on LineageOS hidden framework APIs and Android system build tools (`Android.mk`/`Android.bp`), converting the Qualcomm FM stack into a standard, Gradle-buildable, Jetpack Compose Android application that runs on stock Android 16.

---

## 📱 Target Device Details

* **Primary Target Device:** Samsung Galaxy Tab A9+
* **Model Number:** SM-X216B
* **Chipset:** Qualcomm Snapdragon 695 5G (SM6375 / `holi`)
* **Android Version:** Stock Samsung One UI (Android 16 / SDK 36)
* **Architecture:** `arm64-v8a`
* **Device Status:** Unrooted, Stock ROM, Locked Bootloader, No LSPosed/Magisk

---

## ⚡ Key Features

* **Complete Tuner Controls:** Direct frequency dial (87.5 MHz – 108.0 MHz), +/- 0.1 MHz fine-tune step buttons, slider, and quick numpad entry.
* **Auto Scanning & Seek:** Full FM band scanner with automatic signal threshold filtering and channel bookmarking.
* **RDS Decoder:** Real-time decoding of Program Service (PS) station name, Radio Text (RT) track info, Program Type (PTY), and RSSI signal level.
* **Audio Routing:** Toggles between Wired Headset (required as physical antenna) and Loudspeaker output via Android `AudioManager`.
* **Station Presets:** Save, edit, tag, and organize custom station presets backed by local Room SQLite database persistence.
* **HAL Auto-Detection & Diagnostics:** Proactively scans system vendor paths (`vendor.qti.hardware.fm@1.0`, `/vendor/lib64/libfmpal.so`, `/dev/radio0`, and Binder interfaces).
* **Graceful Fallback Mode:** If Qualcomm vendor HAL access is blocked by Samsung SELinux policy or signature permissions on stock ROMs, the app gracefully falls back to interactive DSP/Simulation mode rather than crashing.

---

## 🛑 Stock Android 16 & Samsung One UI Restrictions

### Why Physical HAL Access is Restricted on Stock Samsung ROMs
On stock Samsung One UI firmware:
1. **SELinux Policy Denials:** Samsung's stock SELinux context strictly enforces `neverallow untrusted_app` rules against opening vendor character devices (`/dev/radio0`) or binding directly to hardware HAL services (`vendor.qti.hardware.fm@1.0`).
2. **Signature-Protected Permissions:** Standard Qualcomm FM framework declarations require system signature permissions (`android.permission.ACCESS_FM_RADIO`) which can only be granted to apps signed with Samsung's platform platform key or pre-installed in `/system/priv-app/`.
3. **Audio HAL Routing:** Samsung routes FM audio through custom internal Knox/AudioPolicy audio paths that are inaccessible to user-installed third-party apps without root access.

### Software Behaviour on Unrooted Devices
When run on an unrooted Samsung Galaxy Tab A9+:
* The app automatically executes the **Qualcomm HAL Diagnostic Suite**.
* It identifies the presence of `vendor.qti.hardware.fm@1.0` and `/vendor/lib64/libfmpal.so`.
* If direct hardware IO is denied by SELinux, the app displays a prominent **Hardware HAL Status** indicator explaining the stock ROM constraint and activates the **DSP Simulation Engine**.

---

## 🏗️ Repository Structure

```
/
├── app/                  # Main Android application module (Jetpack Compose, Room, ViewModels)
├── fmjni/                # Native Qualcomm C++ JNI bridge bindings
├── fmservice/            # Standalone FM Radio Service layer, Audio Router, and RDS Decoder
├── native/               # C++ native HAL interfaces, CMake build file, and C++ header files
├── .github/workflows/    # CI/CD GitHub Actions release workflow (.github/workflows/release.yml)
├── README.md             # Technical documentation and device compatibility guide
├── LICENSE               # Apache 2.0 Open Source License
└── CHANGELOG.md          # Release and commit history
```

---

## 🛠️ Build Instructions

### Prerequisites
* JDK 21 (Java Development Kit 21)
* Android Studio Ladybug (2024.2.1+) or Gradle 8.12+ / 9.x
* Android SDK Platform 36 (Android 16 / SDK 36)

### Local Command Line Build
```bash
# Clone the repository
git clone https://github.com/qualcomm-fm/android-fm-radio.git
cd android-fm-radio

# Build Debug and Release APKs
gradle assembleDebug assembleRelease
```

---

## 🚀 GitHub Actions & CI/CD Pipeline

The `.github/workflows/release.yml` pipeline automatically compiles `arm64-v8a` debug and release binaries with JDK 21, validates compilation, signs release binaries using GitHub Secrets if available, and generates releases automatically.

### Release APK Signing Setup
To produce signed release APKs in GitHub Actions, add the following Repository Secrets:
* `KEYSTORE_BASE64`: Base64-encoded JKS keystore file
* `STORE_PASSWORD`: Keystore store password
* `KEY_ALIAS`: Key alias name
* `KEY_PASSWORD`: Key password

---

## 📜 License

Licensed under the [Apache License, Version 2.0](LICENSE). Derived in part from LineageOS and Qualcomm open-source repositories.
