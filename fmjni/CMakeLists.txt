cmake_minimum_required(VERSION 3.22.1)

project("qcom_fm_jni")

add_library(
    qcom_fm_jni
    SHARED
    fm_jni.cpp
)

find_library(
    log-lib
    log
)

target_link_libraries(
    qcom_fm_jni
    ${log-lib}
)
