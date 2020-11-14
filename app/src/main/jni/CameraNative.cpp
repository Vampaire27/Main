
#include <jni.h>
#include "com_wwc2_jni_camera_CameraNative.h"
#include "common.h"
/*
 * Class:     com_wwc2_jni_camera_CameraNative
 * Method:    CAMERA_read
 * Signature: ()I
 */

#define BACKCAMERA_SIGNAL_PATH "/sys/devices/virtual/switch/backcamera_signal/state"

JNIEXPORT jint JNICALL Java_com_wwc2_jni_camera_CameraNative_CAMERA_1read (JNIEnv * env, jclass cls){

    jint ret = 1;
    ret = get_int_value(BACKCAMERA_SIGNAL_PATH);
    LOGD("jni backcamera_signal: %d", ret);
    return ret;
}


