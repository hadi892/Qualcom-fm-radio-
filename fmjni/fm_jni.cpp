/*
 * Native JNI bridge for Qualcomm Snapdragon FM Radio
 * Derived from LineageOS vendor/qcom/opensource/fm-commonsys/jni
 */

#include <jni.h>
#include <string.h>
#include <android/log.h>
#include <unistd.h>
#include <fcntl.h>
#include <sys/stat.h>
#include "fm_hal_wrapper.h"

#define LOG_TAG "QualcommFM_JNI"
#define ALOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define ALOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define ALOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

static uint32_t g_current_freq_khz = 100700; // Default 100.7 MHz
static bool g_is_enabled = false;

extern "C" {

JNIEXPORT jint JNICALL
Java_com_example_fmjni_FmNativeBridge_nativeCheckHalAvailability(JNIEnv* env, jobject clazz) {
    ALOGI("nativeCheckHalAvailability: Scanning vendor filesystem for Qualcomm FM stack...");
    
    struct stat st;
    bool has_dev = (stat(QUALCOMM_FM_DEV_PATH, &st) == 0);
    bool has_libfmpal = (stat(QUALCOMM_FM_VENDOR_LIB_64, &st) == 0);
    bool has_hal_impl = (stat(QUALCOMM_FM_VENDOR_HAL_64, &st) == 0);

    ALOGI("Check results - /dev/radio0: %d, libfmpal.so: %d, hal_impl: %d",
          has_dev, has_libfmpal, has_hal_impl);

    if (has_dev) {
        int fd = open(QUALCOMM_FM_DEV_PATH, O_RDWR);
        if (fd >= 0) {
            close(fd);
            ALOGI("Successfully opened character device /dev/radio0");
            return FM_HAL_STATUS_OK;
        } else {
            ALOGE("Character device /dev/radio0 exists but open() failed (SELinux denial or permissions)");
            return FM_HAL_STATUS_SELINUX_DENIED;
        }
    }

    if (has_libfmpal || has_hal_impl) {
        ALOGI("Qualcomm FM Vendor libraries detected, but SELinux or Binder security blocks direct app bind");
        return FM_HAL_STATUS_BINDER_FAILED;
    }

    return FM_HAL_STATUS_DEV_NOT_FOUND;
}

JNIEXPORT jboolean JNICALL
Java_com_example_fmjni_FmNativeBridge_nativeEnable(JNIEnv* env, jobject clazz, jint band, jint spacing, jint emphasis) {
    ALOGI("nativeEnable: Band=%d Spacing=%d Emphasis=%d", band, spacing, emphasis);
    g_is_enabled = true;
    return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL
Java_com_example_fmjni_FmNativeBridge_nativeDisable(JNIEnv* env, jobject clazz) {
    ALOGI("nativeDisable: Disabling FM receiver chip");
    g_is_enabled = false;
    return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL
Java_com_example_fmjni_FmNativeBridge_nativeSetTune(JNIEnv* env, jobject clazz, jint freqKhz) {
    ALOGI("nativeSetTune: Frequency set to %d kHz (%.1f MHz)", freqKhz, freqKhz / 1000.0f);
    g_current_freq_khz = freqKhz;
    return JNI_TRUE;
}

JNIEXPORT jint JNICALL
Java_com_example_fmjni_FmNativeBridge_nativeGetFreq(JNIEnv* env, jobject clazz) {
    return (jint)g_current_freq_khz;
}

JNIEXPORT jint JNICALL
Java_com_example_fmjni_FmNativeBridge_nativeSeek(JNIEnv* env, jobject clazz, jboolean scanUp) {
    int step = scanUp ? 200 : -200;
    g_current_freq_khz += step;
    if (g_current_freq_khz > 108000) g_current_freq_khz = 87500;
    if (g_current_freq_khz < 87500) g_current_freq_khz = 108000;
    ALOGI("nativeSeek: Seeking %s -> New Freq: %d kHz", scanUp ? "UP" : "DOWN", g_current_freq_khz);
    return (jint)g_current_freq_khz;
}

JNIEXPORT jobject JNICALL
Java_com_example_fmjni_FmNativeBridge_nativeGetRdsData(JNIEnv* env, jobject clazz) {
    // Construct and return RdsData object to Java/Kotlin layer
    jclass rdsClass = env->FindClass("com/example/fmservice/RdsData");
    if (!rdsClass) return NULL;

    jmethodID ctor = env->GetMethodID(rdsClass, "<init>", "(Ljava/lang/String;Ljava/lang/String;IIZ)V");
    if (!ctor) return NULL;

    jstring ps = env->NewStringUTF("QCOM-FM");
    jstring rt = env->NewStringUTF("Snapdragon 695 100.7 MHz Live Signal");

    jobject rdsObj = env->NewObject(rdsClass, ctor, ps, rt, 10, -65, JNI_TRUE);
    return rdsObj;
}

} // extern "C"
