/* DO NOT EDIT THIS FILE - it is machine generated */


#include <jni.h>
#include "com_wwc2_jni_backlight_BacklightNative.h"
#include "common.h"
/* Header for class com_wwc2_jni_backlight_BacklightNative */

/*
 * Class:     com_wwc2_jni_backlight_BacklightNative
 * Method:    BACKLIGHT_open
 * Signature: ()I
 */

#define BKL_LCD_PATH "/sys/class/leds/lcd-backlight/brightness"
#define BKL_LCD_NODE "/sys/class/gpiodrv/gpio_ctrl/bls_ctrl"//YDG

JNIEXPORT jint JNICALL Java_com_wwc2_jni_backlight_BacklightNative_BACKLIGHT_1open(JNIEnv * env, jclass obj){

     jint lcd_brightness = 0;
     jint count = 0;
     //lcd_brightness = get_int_value(BKL_LCD_PATH);
     lcd_brightness = 191;
     LOGD("jni backlight_open: %d",lcd_brightness);

     //count = set_int_value(BKL_LCD_PATH,lcd_brightness);//YDG
     count = set_int_value(BKL_LCD_NODE, 1);
     return count;
}
/*
 * Class:     com_wwc2_jni_backlight_BacklightNative
 * Method:    BACKLIGHT_read
 * Signature: ()I
 */

JNIEXPORT jint JNICALL Java_com_wwc2_jni_backlight_BacklightNative_BACKLIGHT_1read(JNIEnv * env, jclass obj){
     jint lcd_brightness = 0;
     lcd_brightness = get_int_value(BKL_LCD_PATH);
     LOGD("jni backlight_read: %d",lcd_brightness);
     return lcd_brightness;
}

/*
 * Class:     com_wwc2_jni_backlight_BacklightNative
 * Method:    BACKLIGHT_close
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_wwc2_jni_backlight_BacklightNative_BACKLIGHT_1close (JNIEnv * env, jclass obj){
      jint count = 0;
      //count = set_int_value(BKL_LCD_PATH, 0);//YDG
      count = set_int_value(BKL_LCD_NODE, 0);
      LOGD("jni backlight_close, set brightness 0");
      return count;
}

