package com.wwc2.main;

import com.wwc2.corelib.logic.LogicApplication;
import com.wwc2.corelib.utils.apk.ApkUtils;

/**
 * The main application.
 *
 * @author wwc2
 * @date 2017/1/16
 */
public class MainApplication extends LogicApplication {

    @Override
    public String getLogDirName() {
        return "MainService";
    }

    @Override
    public String getTencentCrashReportId() {
        return "ef4d575844";
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //begin when main apk crash,when start com.wwc2.main.main.MainService early. 20170615
        ApkUtils.startServiceSafety(this, "com.wwc2.main.main.MainService", "com.wwc2.main", "com.wwc2.main.MainService");
        //end
        // init crash report.
//        final String crashReportId = getTencentCrashReportId();
//        if (null != crashReportId) {
//            CrashReport.initCrashReport(getApplicationContext(), crashReportId, false);
//        }
    }
}
