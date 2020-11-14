package com.wwc2.main.driver.version.driver;

import android.os.Build;
import android.text.TextUtils;

import com.wwc2.corelib.utils.log.LogUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * get the system version.
 *
 * @author wwc2
 * @date 2017/1/4
 */
public class VersionUtils {

    private static final String TAG = "VersionUtils";

    private static final String FILENAME_PROC_VERSION = "/proc/version";
    private static final String FILENAME_MSV = "/sys/board_properties/soc/msv";

    private static final String KEY_CONTAINER = "container";
    private static final String KEY_TEAM = "team";
    private static final String KEY_CONTRIBUTORS = "contributors";
    private static final String KEY_REGULATORY_INFO = "regulatory_info";
    private static final String KEY_TERMS = "terms";
    private static final String KEY_LICENSE = "license";
    private static final String KEY_COPYRIGHT = "copyright";
    private static final String KEY_SYSTEM_UPDATE_SETTINGS = "system_update_settings";
    private static final String PROPERTY_URL_SAFETYLEGAL = "ro.url.safetylegal";
    private static final String PROPERTY_SELINUX_STATUS = "ro.build.selinux";
    private static final String KEY_KERNEL_VERSION = "kernel_version";
    private static final String KEY_BUILD_NUMBER = "build_number";
    private static final String KEY_DEVICE_MODEL = "device_model";
    private static final String KEY_SELINUX_STATUS = "selinux_status";
    private static final String KEY_BASEBAND_VERSION = "baseband_version";
    private static final String KEY_BASEBAND_VERSION_2 = "baseband_version_2";
    private static final String KEY_FIRMWARE_VERSION = "firmware_version";
    private static final String KEY_SCOMO = "scomo";
    private static final String KEY_MDM_SCOMO = "mdm_scomo";
    private static final String KEY_UPDATE_SETTING = "additional_system_update_settings";
    private static final String KEY_EQUIPMENT_ID = "fcc_equipment_id";
    private static final String PROPERTY_EQUIPMENT_ID = "ro.ril.fccid";
    private static final String KEY_DMSW_UPDATE = "software_update";
    private static final String KEY_MDM_FUMO = "mdm_fumo";
    private static final String KEY_SOFTWARE_UPDATE = "more_software_updates";
    //status info key
    private static final String KEY_STATUS_INFO = "status_info";
    private static final String KEY_STATUS_INFO_GEMINI = "status_info_gemini";
    //custom build version
    private static final String PROPERTY_BUILD_VERSION_CUSTOM = "ro.custom.build.version";
    private static final String SYSTEM_VERSION_PATH = "/system/system.ver";

    //mtk system update info
    private static final String KEY_MTK_SYSTEM_UPDATE_SETTINGS = "mtk_system_update";

    /**型号*/
    public static String getModelNumber(String def) {
        return getSystemString(Build.MODEL, def);
    }

    /**android版本*/
    public static String getFirewareVersion(String def) {
        return getSystemString(KEY_FIRMWARE_VERSION, def);
    }

    /**基带版本*/
    public static String getBasebandVersion(String def) {
        return getSystemString(KEY_BASEBAND_VERSION, def);
    }

    /**内核版本*/
    public static String getKernelVersion(String def) {
        try {
            //return formatKernelVersion(readLine(FILENAME_PROC_VERSION));
            String proc = readLine(FILENAME_PROC_VERSION);
            int index = proc.indexOf("(");
            if(index > 0) {
                proc = proc.substring(0, index);
            }
            return proc;//readLine(FILENAME_PROC_VERSION);
        } catch (IOException e) {
            LogUtils.d(TAG,
                    "IO Exception when getting kernel version for Device Info screen",
                    e);

            return def;
        }
    }

    /**版本号*/
    public static String getVersion(String def) {
        String ret = Build.DISPLAY;
        if (TextUtils.isEmpty(ret)) {
            ret = def;
        }
        return ret;
    }

    /**自定义版本.*/
    public static String getCustomVersion(String def) {
        try {
            String version = readLine(SYSTEM_VERSION_PATH);
            LogUtils.d(TAG, "Systemversion:" + version);
            String newVersion = "Android " + Build.VERSION.RELEASE + " " + version;
            return newVersion;
        } catch (IOException e) {
            LogUtils.d(TAG, e.getMessage());

            return def;
        }
//        finally {
//        }
//        getSystemString(PROPERTY_BUILD_VERSION_CUSTOM, def);
    }

    /**获取系统属性值*/
    private static String getSystemString(String key, String def) {
        String ret = null;
        if (!TextUtils.isEmpty(key)) {
            try {

                Class cl = Class.forName("android.os.SystemProperties");

                Object invoker = cl.newInstance();

                Method m = cl.getMethod("get", new Class[] { String.class,String.class });

                Object result = m.invoke(invoker, new Object[]{key, null});

                ret = (String)result;

            } catch (Exception e) {

            }
        }

        if (TextUtils.isEmpty(ret)) {
            ret = def;
        }
        return ret;
    }

    /**
     * Reads a line from the specified file.
     * @param filename the file to read from
     * @return the first line, if any.
     * @throws IOException if the file couldn't be read
     */
    private static String readLine(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename), 256);
        try {
            String resule= reader.readLine();
            LogUtils.d(TAG, "result:" + resule);
            return resule;
        } finally {
            reader.close();
        }
    }

    private static String formatKernelVersion(String rawKernelVersion) {
        // Example (see tests for more):
        // Linux version 3.0.31-g6fb96c9 (android-build@xxx.xxx.xxx.xxx.com) \
        //     (gcc version 4.6.x-xxx 20120106 (prerelease) (GCC) ) #1 SMP PREEMPT \
        //     Thu Jun 28 11:02:39 PDT 2012

        final String PROC_VERSION_REGEX =
                "Linux version (\\S+) " + /* group 1: "3.0.31-g6fb96c9" */
                        "\\((\\S+?)\\) " +        /* group 2: "x@y.com" (kernel builder) */
                        "(?:\\(gcc.+? \\)) " +    /* ignore: GCC version information */
                        "(#\\d+) " +              /* group 3: "#1" */
                        "(?:.*?)?" +              /* ignore: optional SMP, PREEMPT, and any CONFIG_FLAGS */
                        "((Sun|Mon|Tue|Wed|Thu|Fri|Sat).+)"; /* group 4: "Thu Jun 28 11:02:39 PDT 2012" */

        Matcher m = Pattern.compile(PROC_VERSION_REGEX).matcher(rawKernelVersion);
        if (!m.matches()) {
            LogUtils.d(TAG, "Regex did not match on /proc/version: " + rawKernelVersion);
            return "Unavailable";
        } else if (m.groupCount() < 4) {
            LogUtils.d(TAG, "Regex match on /proc/version only returned " + m.groupCount()
                    + " groups");
            return "Unavailable";
        }
        return m.group(1) + "\n" +                 // 3.0.31-g6fb96c9
                m.group(2) + " " + m.group(3) + "\n" + // x@y.com #1
                m.group(4);                            // Thu Jun 28 11:02:39 PDT 2012
    }
}
