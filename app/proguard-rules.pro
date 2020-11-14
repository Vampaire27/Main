# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /local/tools/adt-bundle-linux-x86_64-20130917/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

#--------------------------------自定义区域--------------------------
#自行添加需要过滤的内容

-keep class com.txznet.** { *;}
-dontwarn com.txznet.**

-keep class com.mediatek.** { *;}
-dontwarn com.mediatek.**

-keep class com.jiongbull.jlog.** { *; }
-dontwarn com.jiongbull.jlog.**

-keep class com.wwc2.corelib.** { *; }
-dontwarn com.wwc2.corelib.**

-keep class com.wwc2.common_interface.** { *; }
-dontwarn com.wwc2.common_interface.**
-keep class com.wwc2.radio_interface.** { *; }
-dontwarn com.wwc2.radio_interface.**
-keep class com.wwc2.bluetooth_interface.** { *; }
-dontwarn com.wwc2.bluetooth_interface.**
-keep class com.wwc2.avin_interface.** { *; }
-dontwarn com.wwc2.avin_interface.**
-keep class com.wwc2.camera_interface.** { *; }
-dontwarn com.wwc2.camera_interface.**
-keep class com.wwc2.poweroff_interface.** { *; }
-dontwarn com.wwc2.poweroff_interface.**
-keep class com.wwc2.accoff_interface.** { *; }
-dontwarn com.wwc2.accoff_interface.**
-keep class com.wwc2.audio_interface.** { *; }
-dontwarn com.wwc2.audio_interface.**
-keep class com.wwc2.media_interface.** { *; }
-dontwarn com.wwc2.media_interface.**

-keep class tv.danmaku.ijk.** { *; }

-keep class com.wwc2.video_interface.** { *; }
-dontwarn com.wwc2.video_interface.**
-keep class com.wwc2.launcher_interface.** { *; }
-dontwarn com.wwc2.launcher_interface.**
-keep class com.wwc2.navi_interface.** { *; }
-dontwarn com.wwc2.navi_interface.**
-keep class com.wwc2.mcuupdate_interface.** { *; }
-dontwarn com.wwc2.mcuupdate_interface.**
-keep class com.wwc2.systemupdate_interface.** { *; }
-dontwarn com.wwc2.systemupdate_interface.**
-keep class com.wwc2.mainui_interface.** { *; }
-dontwarn com.wwc2.mainui_interface.**
-keep class com.wwc2.settings_interface.** { *; }
-dontwarn com.wwc2.settings_interface.**
-keep class com.wwc2.systempermission_interface.** { *; }
-dontwarn com.wwc2.systempermission_interface.**
-keep class com.wwc2.voiceassistant_interface.** { *; }
-dontwarn com.wwc2.voiceassistant_interface.**
-keep class com.wwc2.canbus_interface.** { *; }
-dontwarn com.wwc2.canbus_interface.**

-keep class com.wwc2.main.silent.SilentLogic
-keep class com.wwc2.main.canbus.CanBusLogic
-keep class com.wwc2.main.common.CommonLogic
-keep class com.wwc2.main.eventinput.EventInputLogic
-keep class com.wwc2.main.navi.NaviLogic
-keep class com.wwc2.main.third_party.ThirdpartyLogic
-keep class com.wwc2.main.launcher.LauncherLogic
-keep class com.wwc2.main.poweroff.PoweroffLogic
-keep class com.wwc2.main.accoff.AccoffLogic
-keep class com.wwc2.main.phonelink.PhonelinkLogic
-keep class com.wwc2.main.radio.RadioLogic
-keep class com.wwc2.main.bluetooth.BluetoothLogic
-keep class com.wwc2.main.aux1.AuxLogic
-keep class com.wwc2.main.camera.CameraLogic
-keep class com.wwc2.main.standby.StandbyLogic
-keep class com.wwc2.main.media.video.VideoLogic
-keep class com.wwc2.main.media.audio.AudioLogic
-keep class com.wwc2.main.upgrade.mcu.McuUpdateLogic
-keep class com.wwc2.main.upgrade.system.SystemUpdateLogic
-keep class com.wwc2.main.mainui.MainUILogic
-keep class com.wwc2.main.system_permission.SystemPermissionLogic
-keep class com.wwc2.main.settings.SettingsLogic
-keep class com.wwc2.main.voice_assistant.VoiceAssistantLogic
-keep class com.wwc2.main.weather.WeatherLogic
-keep class com.wwc2.main.dvr.DVRLogic
-keep class com.wwc2.main.tv.TVLogic
-keep class com.wwc2.main.irdvr.IRDVRLogic

-keep class com.wwc2.jni.*
-keep class com.wwc2.main.android_serialport_api.** { *; }


-keep interface *{
     <methods>;
     <fields>;
}
-keepclassmembers class * extends com.wwc2.corelib.listener.BaseListener {
    public <methods>;
}

#---------------------------------v0.1--------------------------(无需改动)
-optimizationpasses 5
-dontskipnonpubliclibraryclassmembers
-printmapping proguardMapping.txt
-optimizations !code/simplification/cast,!field/*,!class/merging/*
-keepattributes *Annotation*,InnerClasses
-keepattributes Signature
-keepattributes SourceFile,LineNumberTable

#activity,application,service,broadcastReceiver,contentprovider..不可混淆
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.support.multidex.MultiDexApplication
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class * extends android.view.View
-keep public class * extends android.support.v4.app.ActionBarDrawerToggle
-keep public class * extends android.graphics.drawable.Drawable
-keep public class com.android.vending.licensing.ILicensingService
-keep class android.support.** {*;}

-keep interface *{
         <methods>;
          <fields>;
     }

-keep public class * extends android.view.View{
    *** get*();
    void set*(***);
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
#android:onclick="onClick"
-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
-keep class **.R$* {
 *;
}

-keepclassmembers class * {
    void *(*Event);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

#natvie
-keepclasseswithmembernames class * {
    native <methods>;
}

#Parcelable
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

#webview
-keepclassmembers class fqcn.of.javascript.interface.for.Webview {
   public *;
}
-keepclassmembers class * extends android.webkit.WebViewClient {
    public void *(android.webkit.WebView, java.lang.String, android.graphics.Bitmap);
    public boolean *(android.webkit.WebView, java.lang.String);
}
-keepclassmembers class * extends android.webkit.WebViewClient {
    public void *(android.webkit.WebView, jav.lang.String);
}

#okhttp3.x
-dontwarn com.squareup.okhttp3.**
-keep class com.squareup.okhttp3.** { *;}
-dontwarn okio.**
#sharesdk
-keep class cn.sharesdk.**{*;}
-keep class com.sina.**{*;}
-keep class **.R$* {*;}
-keep class **.R{*;}

-keep class com.mob.**{*;}
-dontwarn com.mob.**
-dontwarn cn.sharesdk.**
-dontwarn **.R$*

## nineoldandroids
-keep public class com.nineoldandroids.** {*;}

-keepclassmembers class * {
   public <init> (org.json.JSONObject);
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

#eventbus 3.0
-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}
#EventBus
-keepclassmembers class ** {
    public void onEvent*(**);
}
-keepclassmembers class ** {
public void xxxxxx(**);
}

# gson
-keep class com.google.gson.** {*;}
-keep class com.google.**{*;}
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }
-keep class com.google.gson.examples.android.model.** { *; }

-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
-keep public class * implements java.io.Serializable {*;}

# support-v4
-dontwarn android.support.v4.**
-keep class android.support.v4.app.** { *; }
-keep interface android.support.v4.app.** { *; }
-keep class android.support.v4.** { *; }

# support-v7
-dontwarn android.support.v7.**
-keep class android.support.v7.internal.** { *; }
-keep interface android.support.v7.internal.** { *; }
-keep class android.support.v7.** { *; }

# support design
-dontwarn android.support.design.**
-keep class android.support.design.** { *; }
-keep interface android.support.design.** { *; }
-keep public class android.support.design.R$* { *; }

# picasso
-keep class com.squareup.picasso.** {*; }
-dontwarn com.squareup.picasso.**

#glide 4.x
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

#greenDao
-keep class de.greenrobot.dao.** {*;}
-keepclassmembers class * extends de.greenrobot.dao.AbstractDao {
    public static Java.lang.String TABLENAME;
}
-keep class **$Properties
#volley
-keep class com.android.volley.** {*;}
-keep class com.android.volley.toolbox.** {*;}
-keep class com.android.volley.Response$* { *; }
-keep class com.android.volley.Request$* { *; }
-keep class com.android.volley.RequestQueue$* { *; }
-keep class com.android.volley.toolbox.HurlStack$* { *; }
-keep class com.android.volley.toolbox.ImageLoader$* { *; }

-dontwarn cn.jpush.**
-keep class cn.jpush.** { *; }

-keep class com.activeandroid.** { *; }
-dontwarn com.ikoding.app.biz.dataobject.**
-keep public class com.ikoding.app.biz.dataobject.** { *;}
-keepattributes *Annotation*

-dontwarn org.apache.log4j.**
-keep class  org.apache.log4j.** { *;}
-keep class com.easemob.** {*;}
-keep class org.jivesoftware.** {*;}
-dontwarn  com.easemob.**

-keepclassmembers class fqcn.of.javascript.interface.for.webview {
 public *;
}

-keepattributes Exceptions,InnerClasses

-keep class io.rong.** {*;}

-keep class * implements io.rong.imlib.model.MessageContent{*;}

-keepattributes Signature

-keepattributes *Annotation*

-keep class sun.misc.Unsafe { *; }

-keep class com.google.gson.examples.android.model.** { *; }

-keepclassmembers class * extends com.sea_monster.dao.AbstractDao {
 public static java.lang.String TABLENAME;
}
-keep class **$Properties
-dontwarn org.eclipse.jdt.annotation.**

-keep class com.ultrapower.** {*;}
-dontwarn com.amap.api.**
-dontwarn com.a.a.**
-dontwarn com.autonavi.**
-keep class com.amap.api.**  {*;}
-keep class com.autonavi.**  {*;}
-keep class com.a.a.**  {*;}
-keep class **.R$* {*;}
-keep class com.isnc.facesdk.aty.**{*;}
-keep class com.isnc.facesdk.**{*;}
-keep class com.isnc.facesdk.common.**{*;}
-keep class com.isnc.facesdk.net.**{*;}
-keep class com.isnc.facesdk.view.**{*;}
-keep class com.isnc.facesdk.viewmodel.**{*;}
-keep class com.matrixcv.androidapi.face.**{*;}

-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

-dontwarn rx.*
-dontwarn sun.misc.**

-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
   long producerIndex;
   long consumerIndex;
}

-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode producerNode;
}

-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode consumerNode;
}

#litepal
-dontwarn org.litepal.
-keep class org.litepal.* { *; }
-keep enum org.litepal.*
-keep interface org.litepal. { *; }
#-keep public class  extends org.litepal.
-keepattributes Annotation
-keepclassmembers class * extends org.litepal.crud.DataSupport{*;}

#fastJson
-dontwarn com.alibaba.fastjson.**
-keep class com.alibaba.fastjson.** { *; }

# Okio
-dontwarn com.squareup.**
-dontwarn okio.**
-keep public class org.codehaus.* { *; }
-keep public class java.nio.* { *; }
# Retrolambda
-dontwarn java.lang.invoke.*

# Do not strip any method/class that is annotated with @DoNotStrip
-keep @com.facebook.common.internal.DoNotStrip class *
-keepclassmembers class * {
    @com.facebook.common.internal.DoNotStrip *;
}
