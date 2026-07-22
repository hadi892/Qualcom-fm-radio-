# C++ CMake Build Configuration for Qualcomm Snapdragon FM Native HAL Bridge

cmake_minimum_required(VERSION 3.22.1)
project("qcom_fm_jni")

add_library(
    qcom_fm_jni
    SHARED
    qcom_fm_native.cpp
)

find_library(
    log-lib
    log
)

target_include_directories(
    qcom_fm_jni
    PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}/include
)

target_link_libraries(
    qcom_fm_jni
    ${log-lib}
    dl
)
