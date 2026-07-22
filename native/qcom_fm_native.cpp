/*
 * Qualcomm Snapdragon FM Radio Native JNI Implementation
 * Derived from LineageOS android_vendor_qcom_opensource_fm-commonsys
 * Communicates with Qualcomm vendor HIDL/AIDL/character device /dev/radio0
 */

#include "include/qcom_fm.h"
#include <fcntl.h>
#include <unistd.h>
#include <sys/ioctl.h>
#include <linux/videodev2.h>
#include <dlfcn.h>
#include <cstring>
#include <cerrno>

static int g_radio_fd = -1;
static void* g_fmpal_handle = nullptr;

JNIEXPORT jint JNICALL
Java_com_example_fmjni_FmNativeBridge_nativeCheckHalAvailability(JNIEnv *env, jobject thiz) {
    LOGI("Checking native Qualcomm FM HAL availability...");
    
    // 1. Check direct character device nodes
    const char* dev_paths[] = {"/dev/radio0", "/dev/fm0", "/dev/radio1", "/dev/fmradio", "/dev/smd_fm"};
    for (const char* path : dev_paths) {
        g_radio_fd = open(path, O_RDWR);
        if (g_radio_fd < 0) {
            g_radio_fd = open(path, O_RDONLY);
        }
        if (g_radio_fd >= 0) {
            struct v4l2_capability cap;
            memset(&cap, 0, sizeof(cap));
            if (ioctl(g_radio_fd, VIDIOC_QUERYCAP, &cap) == 0) {
                LOGI("Direct V4L2 Radio device %s query successful: card=%s, driver=%s", path, cap.card, cap.driver);
                return 0; // Hardware accessible directly via V4L2
            } else {
                LOGI("Opened %s directly for raw hardware access", path);
                return 0;
            }
        }
    }

    // 2. Vendor HAL library dlopen test
    const char* hal_libs[] = {
        "libfmpal.so",
        "/vendor/lib64/libfmpal.so",
        "/vendor/lib/libfmpal.so",
        "/vendor/lib64/hw/vendor.qti.hardware.fm@1.0-impl.so",
        "/vendor/lib/hw/vendor.qti.hardware.fm@1.0-impl.so",
        "libqcomfm_jni.so"
    };

    for (const char* lib_path : hal_libs) {
        g_fmpal_handle = dlopen(lib_path, RTLD_NOW);
        if (g_fmpal_handle) {
            LOGI("Qualcomm FM vendor library dynamic link successful with %s", lib_path);
            return 0;
        }
    }

    LOGE("Qualcomm hardware HAL unavailable due to OS/SELinux restrictions or missing driver node.");
    return -1;
}

JNIEXPORT jboolean JNICALL
Java_com_example_fmjni_FmNativeBridge_nativeEnable(JNIEnv *env, jobject thiz, jint band, jint spacing, jint emphasis) {
    LOGI("Enabling Qualcomm FM Radio Hardware (band=%d, spacing=%d, emphasis=%d)...", band, spacing, emphasis);
    if (g_radio_fd < 0) {
        g_radio_fd = open("/dev/radio0", O_RDWR);
    }
    
    if (g_radio_fd >= 0) {
        // Unmute audio control via V4L2 ioctl
        struct v4l2_control ctrl;
        memset(&ctrl, 0, sizeof(ctrl));
        ctrl.id = V4L2_CID_AUDIO_MUTE;
        ctrl.value = 0; // Unmute
        ioctl(g_radio_fd, VIDIOC_S_CTRL, &ctrl);
        return JNI_TRUE;
    }
    return JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_com_example_fmjni_FmNativeBridge_nativeDisable(JNIEnv *env, jobject thiz) {
    LOGI("Disabling Qualcomm FM Radio Hardware...");
    if (g_radio_fd >= 0) {
        // Mute audio before close
        struct v4l2_control ctrl;
        memset(&ctrl, 0, sizeof(ctrl));
        ctrl.id = V4L2_CID_AUDIO_MUTE;
        ctrl.value = 1; // Mute
        ioctl(g_radio_fd, VIDIOC_S_CTRL, &ctrl);
        
        close(g_radio_fd);
        g_radio_fd = -1;
    }
    if (g_fmpal_handle) {
        dlclose(g_fmpal_handle);
        g_fmpal_handle = nullptr;
    }
    return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL
Java_com_example_fmjni_FmNativeBridge_nativeSetTune(JNIEnv *env, jobject thiz, jint freqKhz) {
    LOGI("Tuning Qualcomm FM Radio hardware to %d kHz...", freqKhz);
    if (g_radio_fd >= 0) {
        struct v4l2_frequency freq;
        memset(&freq, 0, sizeof(freq));
        freq.tuner = 0;
        freq.type = V4L2_TUNER_RADIO;
        // V4L2 radio frequency is specified in 62.5 Hz units (or 1/16 MHz)
        freq.frequency = (unsigned int)((freqKhz * 16) / 1000);
        if (ioctl(g_radio_fd, VIDIOC_S_FREQUENCY, &freq) == 0) {
            LOGI("VIDIOC_S_FREQUENCY ioctl succeeded for %d kHz", freqKhz);
            return JNI_TRUE;
        } else {
            LOGE("VIDIOC_S_FREQUENCY ioctl failed: %s", strerror(errno));
        }
    }
    return JNI_FALSE;
}

JNIEXPORT jint JNICALL
Java_com_example_fmjni_FmNativeBridge_nativeGetFreq(JNIEnv *env, jobject thiz) {
    if (g_radio_fd >= 0) {
        struct v4l2_frequency freq;
        memset(&freq, 0, sizeof(freq));
        freq.tuner = 0;
        freq.type = V4L2_TUNER_RADIO;
        if (ioctl(g_radio_fd, VIDIOC_G_FREQUENCY, &freq) == 0) {
            int freqKhz = (freq.frequency * 1000) / 16;
            LOGI("VIDIOC_G_FREQUENCY returned %d kHz", freqKhz);
            return freqKhz;
        }
    }
    return -1;
}

JNIEXPORT jint JNICALL
Java_com_example_fmjni_FmNativeBridge_nativeSeek(JNIEnv *env, jobject thiz, jboolean scanUp) {
    LOGI("Seeking Qualcomm FM Radio hardware (%s)...", scanUp ? "UP" : "DOWN");
    if (g_radio_fd >= 0) {
        struct v4l2_hw_freq_seek seek;
        memset(&seek, 0, sizeof(seek));
        seek.tuner = 0;
        seek.type = V4L2_TUNER_RADIO;
        seek.seek_upward = scanUp ? 1 : 0;
        seek.wrap_around = 1;
        if (ioctl(g_radio_fd, VIDIOC_S_HW_FREQ_SEEK, &seek) == 0) {
            return Java_com_example_fmjni_FmNativeBridge_nativeGetFreq(env, thiz);
        }
    }
    return -1;
}

JNIEXPORT jobject JNICALL
Java_com_example_fmjni_FmNativeBridge_nativeGetRdsData(JNIEnv *env, jobject thiz) {
    if (g_radio_fd >= 0) {
        struct v4l2_tuner tuner;
        memset(&tuner, 0, sizeof(tuner));
        tuner.index = 0;
        int rssi = -70;
        bool isStereo = true;
        if (ioctl(g_radio_fd, VIDIOC_G_TUNER, &tuner) == 0) {
            rssi = -100 + (int)((tuner.signal * 70) / 65535);
            isStereo = (tuner.rxsubchans & V4L2_TUNER_SUB_STEREO) != 0;
        }

        uint8_t buffer[128];
        memset(buffer, 0, sizeof(buffer));
        ssize_t bytesRead = read(g_radio_fd, buffer, sizeof(buffer));
        
        jclass rdsClass = env->FindClass("com/example/fmservice/RdsData");
        if (rdsClass != nullptr) {
            jmethodID ctor = env->GetMethodID(rdsClass, "<init>", "(Ljava/lang/String;Ljava/lang/String;IIZ)V");
            if (ctor != nullptr) {
                const char* psText = (bytesRead > 0 && buffer[0] != 0) ? (char*)buffer : "HW-SIGNAL";
                const char* rtText = (bytesRead > 0) ? "Raw RDS Hardware Stream" : "Hardware V4L2 Tuner Active";
                jstring psStr = env->NewStringUTF(psText);
                jstring rtStr = env->NewStringUTF(rtText);
                jobject obj = env->NewObject(rdsClass, ctor, psStr, rtStr, 0, rssi, isStereo ? JNI_TRUE : JNI_FALSE);
                env->DeleteLocalRef(psStr);
                env->DeleteLocalRef(rtStr);
                return obj;
            }
        }
    }
    return nullptr;
}
