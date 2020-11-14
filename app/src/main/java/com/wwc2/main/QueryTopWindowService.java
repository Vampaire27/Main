package com.wwc2.main;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.wwc2.corelib.utils.apk.ApkUtils;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.manager.SourceManager;

import java.util.Timer;
import java.util.TimerTask;

/**
 * the query top window service.
 * <uses-permission android:name="android.permission.GET_TASKS" />
 *
 * @author wwc2
 * @date 2017/1/21
 */
public class QueryTopWindowService extends Service {

    public static final String TAG = "QueryTopWindowService";
    private Handler handler = new Handler();
    private Timer timer = null;

    private String mCurActivityName = "";
    private String mCurPackageName = "";

    private static boolean isRunning = false;

    public static boolean isRunning() {
        return isRunning;
    }

    public static void start(Context context) {
//        if (null != context) {
//            Intent intent = new Intent(context, QueryTopWindowService.class);
//            context.startService(intent);
//        }
    }

    public static void stop(Context context) {
//        if (null != context) {
//            Intent intent = new Intent(context, QueryTopWindowService.class);
//            context.stopService(intent);
//        }
    }

    public QueryTopWindowService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        LogUtils.d(TAG, "QueryTopWindowService onCreate");

        // -----------------------------------
        // Set the priority of the calling thread, based on Linux priorities:
        // -----------------------------------
        // The Priority.
        final int priority = android.os.Process.THREAD_PRIORITY_URGENT_DISPLAY;
        // Changes the Priority of the calling Thread!
        android.os.Process.setThreadPriority(priority);
        // Changes the Priority of passed Thread (first param)
        android.os.Process.setThreadPriority(android.os.Process.myTid(), priority);

        isRunning = true;
        if (timer == null) {
            timer = new Timer();
            timer.scheduleAtFixedRate(new RefreshTask(), 0, 200);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        LogUtils.d(TAG, "QueryTopWindowService onDestroy");

        isRunning = false;
    }

    class RefreshTask extends TimerTask {

        @Override
        public void run() {
            if (!isRunning)
                return;
            final String name = getTopActivity(getApplicationContext());
            final String package1 = getTopPackage(getApplicationContext());

            if (!mCurPackageName.equals(package1)) {
                mCurPackageName = package1;
                LogUtils.d(TAG, "top package name = " + package1);
            }

            if (!mCurActivityName.equals(name)) {
                mCurActivityName = name;
                Log.d(TAG, "Activity changed, name = " + name);

                // 对比模式
                if (!TextUtils.isEmpty(mCurPackageName)) {
                    SourceManager.onCompareSource(mCurPackageName, mCurActivityName);
                }
            }
        }

    }

    // To check if service is enabled
    // String servicename = context.getPackageName() + "/" + xxx.class.getName();
    public static boolean isAccessibilitySettingsOn(Context context, String serviceName) {
        int accessibilityEnabled = 0;
        boolean accessibilityFound = false;
        try {
            accessibilityEnabled = Settings.Secure.getInt(context.getApplicationContext().getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Exception e) {
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');
        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(context.getApplicationContext().getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                TextUtils.SimpleStringSplitter splitter = mStringColonSplitter;
                splitter.setString(settingValue);
                while (splitter.hasNext()) {
                    String accessabilityService = splitter.next();
                    if (accessabilityService.equalsIgnoreCase(serviceName)) {
                        return true;
                    }
                }
            }
        } else {
            // Log.v(TAG, "***ACCESSIBILIY IS DISABLED***");
        }

        return accessibilityFound;
    }

    public static String getTopActivity(final Context context) {
        String ret = "";
        ret = ApkUtils.getTopActivity(context);
        return ret;
    }

    public static String getTopPackage(final Context context) {
        String ret = "";
        ret = ApkUtils.getTopPackage(context);
        return ret;
    }
}
