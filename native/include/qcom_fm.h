/*
 * Copyright (C) 2026 Qualcomm Snapdragon FM Native JNI Interface
 * Derived from LineageOS android_vendor_qcom_opensource_fm-commonsys
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

#ifndef QCOM_FM_H
#define QCOM_FM_H

#include <jni.h>
#include <android/log.h>

#define LOG_TAG "QualcommFmNative"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jint JNICALL
Java_com_example_fmjni_FmNativeBridge_nativeCheckHalAvailability(JNIEnv *env, jobject thiz);

JNIEXPORT jboolean JNICALL
Java_com_example_fmjni_FmNativeBridge_nativeEnable(JNIEnv *env, jobject thiz, jint band, jint spacing, jint emphasis);

JNIEXPORT jboolean JNICALL
Java_com_example_fmjni_FmNativeBridge_nativeDisable(JNIEnv *env, jobject thiz);

JNIEXPORT jboolean JNICALL
Java_com_example_fmjni_FmNativeBridge_nativeSetTune(JNIEnv *env, jobject thiz, jint freqKhz);

JNIEXPORT jint JNICALL
Java_com_example_fmjni_FmNativeBridge_nativeGetFreq(JNIEnv *env, jobject thiz);

JNIEXPORT jint JNICALL
Java_com_example_fmjni_FmNativeBridge_nativeSeek(JNIEnv *env, jobject thiz, jboolean scanUp);

JNIEXPORT jobject JNICALL
Java_com_example_fmjni_FmNativeBridge_nativeGetRdsData(JNIEnv *env, jobject thiz);

#ifdef __cplusplus
}
#endif

#endif // QCOM_FM_H
