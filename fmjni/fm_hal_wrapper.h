/*
 * Copyright (C) 2026 Qualcomm Snapdragon FM Radio Stack
 * Derived from LineageOS android_vendor_qcom_opensource_fm-commonsys
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

#ifndef FM_HAL_WRAPPER_H
#define FM_HAL_WRAPPER_H

#include <stdint.h>
#include <stdbool.h>

#ifdef __cplusplus
extern "C" {
#endif

// Qualcomm FM Radio Constants
#define QUALCOMM_FM_HAL_SERVICE_NAME "vendor.qti.hardware.fm@1.0::IFmHci"
#define QUALCOMM_FM_DEV_PATH "/dev/radio0"
#define QUALCOMM_FM_VENDOR_LIB_64 "/vendor/lib64/libfmpal.so"
#define QUALCOMM_FM_VENDOR_HAL_64 "/vendor/lib64/hw/vendor.qti.hardware.fm@1.0-impl.so"

// Regional Band Constants
#define FM_BAND_US_EU_875_1080 0
#define FM_BAND_JAPAN_760_900  1
#define FM_BAND_JAPAN_760_1080 2
#define FM_BAND_WORLD_875_1080 3

// Channel Spacing
#define FM_SPACING_200KHZ 0
#define FM_SPACING_100KHZ 1
#define FM_SPACING_50KHZ  2

// Emphasis
#define FM_EMPHASIS_75US 0
#define FM_EMPHASIS_50US 1

typedef struct {
    uint32_t frequency_khz;
    int32_t rssi;
    bool is_stereo;
    char ps_name[9];       // Program Service Name (8 chars + null)
    char radio_text[65];   // Radio Text (64 chars + null)
    uint16_t program_type; // PTY code
} fm_station_info_t;

typedef enum {
    FM_HAL_STATUS_OK = 0,
    FM_HAL_STATUS_DEV_NOT_FOUND = -1,
    FM_HAL_STATUS_SELINUX_DENIED = -2,
    FM_HAL_STATUS_BINDER_FAILED = -3,
    FM_HAL_STATUS_ALREADY_OPEN = -4,
    FM_HAL_STATUS_IO_ERROR = -5
} fm_hal_status_t;

fm_hal_status_t fm_hal_check_availability(void);
fm_hal_status_t fm_hal_enable(uint32_t band, uint32_t spacing, uint32_t emphasis);
fm_hal_status_t fm_hal_disable(void);
fm_hal_status_t fm_hal_set_tune(uint32_t freq_khz);
fm_hal_status_t fm_hal_get_freq(uint32_t* out_freq_khz);
fm_hal_status_t fm_hal_seek(bool scan_up, uint32_t* out_freq_khz);
fm_hal_status_t fm_hal_get_rds(fm_station_info_t* out_info);

#ifdef __cplusplus
}
#endif

#endif // FM_HAL_WRAPPER_H
