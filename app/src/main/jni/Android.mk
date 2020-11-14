LOCAL_PATH := $(call my-dir)
LOCAL_CPP_FLAGS := -fno-rtti


#声明一个预编译库的模块：共享库
include $(CLEAR_VARS)
LOCAL_MODULE := jni_avinsignal
LOCAL_SRC_FILES :=AvinSignalNative.cpp
include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := jni_backlight
LOCAL_LDLIBS := -llog
LOCAL_SRC_FILES := BacklightNative.cpp common.cpp
include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := jni_camera
LOCAL_LDLIBS := -llog
LOCAL_SRC_FILES := CameraNative.cpp common.cpp
include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := jni_deepsleep
LOCAL_SRC_FILES := DeepSleepNative.cpp
include $(BUILD_SHARED_LIBRARY)

#include $(CLEAR_VARS)
#LOCAL_MODULE := jni_mcuserial
#LOCAL_SRC_FILES := McuSerialNative.cpp
#include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := jni_micswitch
LOCAL_SRC_FILES := MicSwitchNative.cpp
include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := jni_simulate_key
LOCAL_SRC_FILES := SimulateKeyNative.cpp
include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := jni_tp_touch
LOCAL_SRC_FILES := TPTouchNative.cpp
include $(BUILD_SHARED_LIBRARY)



#begin =============zhongyang.hu add for uart0 20170103
include $(CLEAR_VARS)
LOCAL_MODULE    := lib_serial_port
LOCAL_SRC_FILES := SerialPort.c
LOCAL_LDLIBS    := -llog

# Also need the JNI headers.
LOCAL_C_INCLUDES += \
	$(JNI_H_INCLUDE)

include $(BUILD_SHARED_LIBRARY)
#end ==============zhongyang.hu add for uart0 20170103

