package com.wwc2.main.accoff.driver;

import android.os.SystemProperties;
import android.text.TextUtils;

import com.wwc2.accoff_interface.AccoffDefine;
import com.wwc2.camera_interface.CameraDefine;
import com.wwc2.common_interface.Define;
import com.wwc2.corelib.db.Packet;
import com.wwc2.main.driver.backlight.BacklightDriver;
import com.wwc2.main.driver.system.SystemDriver;
import com.wwc2.main.eventinput.EventInputDefine;
import com.wwc2.mainui_interface.MainUIDefine;
import com.wwc2.systempermission_interface.SystemPermissionDefine;

import java.util.ArrayList;
import java.util.List;

/**
 * the delay sleep memory acc off driver.
 *
 * @author wwc2
 * @date 2017/1/18
 */
public abstract class BaseDelaySleepMemoryAccoffDriver extends BaseDelaySleepAccoffDriver {

    /**不用杀死的包名过滤列表*/
    protected List<String> mKeepProcessPackageFilter = new ArrayList<>();

    /**销毁或创建无需理会的逻辑对象*/
    protected List<String> mWithoutHasslesLogics = new ArrayList<>();

    /**销毁或创建无需理会的逻辑对象*/
    protected List<String> mWithoutHasslesDrivers = new ArrayList<>();

    @Override
    public void onCreate(Packet packet) {
        super.onCreate(packet);

        if (null != mKeepProcessPackageFilter) {
            mKeepProcessPackageFilter.add("com.wwc2.main");
            mKeepProcessPackageFilter.add("com.wwc2.accoff");
            mKeepProcessPackageFilter.add("com.wwc2.systempermission");
            mKeepProcessPackageFilter.add("com.wwc2.camera");
            mKeepProcessPackageFilter.add("com.wwc2.mainui");
            mKeepProcessPackageFilter.add("com.wwc2.launcher");
            //begin add by zhua. add voice
            mKeepProcessPackageFilter.add("com.wwc2.voice_assistant");
            //end
            //mKeepProcessPackageFilter.add("com.android.smspush");
            mKeepProcessPackageFilter.add("android.process.acore");
            mKeepProcessPackageFilter.add("com.android.externalstorage");
            mKeepProcessPackageFilter.add("com.android.cellbroadcastreceiver");
            mKeepProcessPackageFilter.add("com.android.dm");
            mKeepProcessPackageFilter.add("com.android.systemui");
            //begin zhongyang.hu modify for acc off
            //mKeepProcessPackageFilter.add("com.android.launcher");
            //mKeepProcessPackageFilter.add("com.android.launcher3");
            mKeepProcessPackageFilter.add("com.mediatek.mtklogger");
            //end
            mKeepProcessPackageFilter.add("android.process.media");
            mKeepProcessPackageFilter.add("com.android.onetimeinitializer");
            //mKeepProcessPackageFilter.add("com.android.settings");
            mKeepProcessPackageFilter.add("com.android.providers.media");
            mKeepProcessPackageFilter.add("com.android.providers.downloads");
            mKeepProcessPackageFilter.add("com.android.providers.drm");
            mKeepProcessPackageFilter.add("com.android.providers.settings");
            mKeepProcessPackageFilter.add("com.android.location.fused");
            //begin zhongyang.hu remove for acc off
            //mKeepProcessPackageFilter.add("com.android.contacts");
            //mKeepProcessPackageFilter.add("com.android.deskclock");
            //mKeepProcessPackageFilter.add("com.android.mms");
            //mKeepProcessPackageFilter.add("com.android.gallery3d");
            //end
            mKeepProcessPackageFilter.add("com.android.providers.applications");
            mKeepProcessPackageFilter.add("com.android.providers.userdictionary");
            mKeepProcessPackageFilter.add("com.android.providers.contacts");
            mKeepProcessPackageFilter.add("com.svox.pico");
            mKeepProcessPackageFilter.add("com.android.providers.telephony");
            mKeepProcessPackageFilter.add("com.android.stk");
            mKeepProcessPackageFilter.add("com.android.phone");
            //begin zhongyang.hu remove for acc off
            //mKeepProcessPackageFilter.add("com.android.keyguard");
            //end
            mKeepProcessPackageFilter.add("com.android.inputmethod.latin");
            mKeepProcessPackageFilter.add("android");
            mKeepProcessPackageFilter.add("com.android.keychain");
            mKeepProcessPackageFilter.add("com.android.defcontainer");
            mKeepProcessPackageFilter.add("com.wwc2.tpmservice");
            mKeepProcessPackageFilter.add("com.wwc2.canbussdk");

            mKeepProcessPackageFilter.add("com.aispeech.aios");
            mKeepProcessPackageFilter.add("com.aispeech.aios.adapter");
            mKeepProcessPackageFilter.add("com.aispeech.aios.wechat");

            /*-20180503-ydinggen-modify-关ACC增加黑屏界面,解决关ACC还会有界面图标显示,不杀Black-*/
            mKeepProcessPackageFilter.add("com.wwc2.black");

            mKeepProcessPackageFilter.add("com.wwc2.networks");
            mKeepProcessPackageFilter.add("com.wwc2.networks:pushcore");

            if (SystemProperties.get("ro.wtDVR", "false").equals("true")) {
                mKeepProcessPackageFilter.add("com.wwc2.networks:ipc");
                mKeepProcessPackageFilter.add("io.rong.push");
                mKeepProcessPackageFilter.add("com.wwc2.dvr");
            }
            //通天星相关APK
            mKeepProcessPackageFilter.add("com.wwc2.ttxassist");
//            mKeepProcessPackageFilter.add("net.babelstar.gdispatch");
        }

        if (null != mWithoutHasslesLogics) {
            mWithoutHasslesLogics.add(Define.MODULE);
            mWithoutHasslesLogics.add(EventInputDefine.MODULE);
            mWithoutHasslesLogics.add(AccoffDefine.MODULE);
            mWithoutHasslesLogics.add(CameraDefine.MODULE);
            mWithoutHasslesLogics.add(SystemPermissionDefine.MODULE);
            mWithoutHasslesLogics.add(MainUIDefine.MODULE);
            //begin add by zhua. remove voice
//            mWithoutHasslesLogics.add(VoiceAssistantDefine.MODULE);
            //end
        }

        if (null != mWithoutHasslesDrivers) {
            mWithoutHasslesDrivers.add(BacklightDriver.DRIVER_NAME);
            mWithoutHasslesDrivers.add(SystemDriver.DRIVER_NAME);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean autoSave() {
        return false;
    }

    @Override
    public String filePath() {
        return "PowerProcessFilter.ini";
    }

    @Override
    public boolean readData() {
        boolean ret = false;
        if (null != mMemory) {
            if (null != mKeepProcessPackageFilter) {
                Object object = mMemory.get("KEEP", "total");
                if (object instanceof String) {
                    String temp = (String) object;
                    try {
                        final int max = Integer.parseInt(temp.trim());
                        if (max > 0) {
                            ret = true;
                            for (int i = 0; i < max; i++) {
                                final String key = "package" + (i + 1);
                                object = mMemory.get("KEEP", key);
                                if (object instanceof String) {
                                    temp = (String) object;
                                    if (!TextUtils.isEmpty(temp)) {
                                        mKeepProcessPackageFilter.add(temp.trim());
                                    }
                                }
                            }

                            // remove duplicate item.
                            removeDuplicate(mKeepProcessPackageFilter);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return ret;
    }
}
