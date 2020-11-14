package com.wwc2.main.driver.version.driver;

import android.os.SystemProperties;
import android.text.TextUtils;

import com.wwc2.corelib.db.Packet;
import com.wwc2.main.driver.mcu.driver.McuDefine;
import com.wwc2.main.manager.McuManager;

/**
 * the android4.4 version driver.
 *
 * @author wwc2
 * @date 2017/1/20
 */
public class MTK6737VersionDriver extends BaseVersionDriver {

    private static final String TAG = "MTK6737VersionDriver";

    /**硬件版本号*/
    private String mHardwareVersion = "1.0";

    /**MCU版本号*/
    private String mMCUVersion = null;

    private McuManager.MCUListener mMCUListener = new McuManager.MCUListener() {

        @Override
        public void OpenListener(int status) {
            // 请求MCU版本号
            //McuManager.sendMcu((byte)McuDefine.ARM_TO_MCU.REQ_McuVer, new byte[]{(byte)1}, 1);
            // 请求硬件版本号
            McuManager.sendMcu((byte)McuDefine.ARM_TO_MCU.REQ_HardwareVer, new byte[]{(byte)1}, 1);
        }

        @Override
        public void DataListener(byte[] val) {
            if (null != val) {
                final int length = val.length;
                if (length > 0) {
                    final int cmd = val[0]&0xff;
                    switch (cmd) {
                        case McuDefine.MCU_TO_ARM.MACK_SysInitdata:
                            if (length > 21) {
                                // MCU版本号
                                String name = "CM_";
                                name += byteToDocString(val[17]);
                                name += ".";

                                name += byteToDocString(val[18]);
                                name += ".";

                                name += byteToDocString(val[19]);
                                name += "_";

                                name += byteToDocString(val[20]);
                                name += ".";
                                name += byteToDocString(val[21]);
                                mMCUVersion = name;
                                //begin zhongyang.hu add for Vsync apk
                                SystemProperties.set("persist.sys.mcu_version",mMCUVersion);
                                //END
                            }
                            break;
                        case McuDefine.MCU_TO_ARM.MRPT_HardwareVer:
                            // 去掉命令字，和结尾0xff
                            if (length > 2) {
                                byte[] buffer = new byte[length-2];
                                System.arraycopy(val, 1, buffer, 0, length-2);
                                mHardwareVersion = new String(buffer);
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
        }
    };

    public static String byteToDocString(byte data) {
        data %= 100;
        String str = ((data/10) == 0) ? "0": "";
        str += Integer.toString(data);
        return str;
    }

    @Override
    public void onCreate(Packet packet) {
        super.onCreate(packet);
        McuManager.getModel().bindListener(mMCUListener);
    }

    @Override
    public void onDestroy() {
        McuManager.getModel().unbindListener(mMCUListener);
        super.onDestroy();
    }

    @Override
    public String getModelNumber() {
        return VersionUtils.getModelNumber(getUnknownString());
    }

    @Override
    public String getHardVersion() {
        String ret = mHardwareVersion;
        if (TextUtils.isEmpty(ret)) {
            ret = getUnknownString();
        }
        return ret;
    }

    @Override
    public String getFirewareVersion() {
        return VersionUtils.getFirewareVersion(getUnknownString());
    }

    @Override
    public String getBasebandVersion() {
        return VersionUtils.getBasebandVersion(getUnknownString());
    }

    @Override
    public String getKernelVersion() {
        return VersionUtils.getKernelVersion(getUnknownString());
    }

    @Override
    public String getSystemVersion() {
        return VersionUtils.getCustomVersion(getUnknownString());
    }

    @Override
    public String getMcuVersion() {
        String ret = mMCUVersion;
        if (TextUtils.isEmpty(ret)) {
            ret = getUnknownString();
        }
        return ret;
    }
}
