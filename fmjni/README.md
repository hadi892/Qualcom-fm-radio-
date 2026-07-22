# Qualcomm FM Radio JNI Module

This directory contains C/C++ JNI native sources and HAL wrapper header definitions for interfacing with Qualcomm Snapdragon FM Radio chipsets.

## Files
- `fm_jni.cpp`: JNI implementation bridging Kotlin/Java calls (`FmNativeBridge`) to native hardware calls.
- `fm_hal_wrapper.h`: Constant definitions for `/dev/radio0`, `vendor.qti.hardware.fm@1.0`, and RSSI/RDS data structures.
- `CMakeLists.txt`: CMake build script for standalone Android NDK compilation.
