package com.wwc2.main.upgrade;

import android.content.Context;
import android.text.TextUtils;
import android.util.Patterns;

import com.wwc2.corelib.db.Result;
import com.wwc2.corelib.utils.json.JsonParser;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.upgrade.system.NetworkUtils;
import com.wwc2.settings_interface.SettingsDefine;

import org.json.JSONArray;

/**
 * the online upgrade.
 *
 * @author wwc2
 * @date 2017/1/27
 */
public class OnlineUpgrade {

    /**
     * TAG
     */
    private static final String TAG = "OnlineUpgrade";

    /**
     * model
     */
    private OnlineUpgradeModel mModel = null;

    /**
     * the thread running.
     */
    private Result mRunning = new Result(false, new Result.ResultListener() {
        @Override
        public void onResult(int oldVal, int newVal) {
            if (mRunning.get()) {
                new Thread() {
                    @Override
                    public void run() {
                        final String client = mModel.mClient;
                        final String name = mModel.mName;
                        final String url = "http://www.icar001.com:5200/?" +
                                "client=" + client + "&" +
                                "name=" + name;
                        LogUtils.d(TAG, "url = " + url);
                        String json = NetworkUtils.getWebContent(url);
                        parseOnlineJsonData(json);
                        mRunning.set(false);
                    }

                }.start();
            }
        }
    });

    /**
     * online upgrade listener.
     */
    private OnlineUpgradeListener mOnlineUpgradeListener = null;

    /**
     * construct
     */
    public OnlineUpgrade(OnlineUpgradeListener listener) {
        mOnlineUpgradeListener = listener;
    }

    /**
     * force upgrade all package.
     */
    private boolean mForceUpgradeAllPackage = false;

    /**
     * force upgrade all package.
     */
    public void forceUpgradeAllPackage(boolean open) {
        mForceUpgradeAllPackage = open;
    }

    /**
     * the online upgrade listener.
     */
    public interface OnlineUpgradeListener {
        /**
         * the upgrade status change.
         */
        void UpgradeStatusChange(int status);

        /**
         * upgrade run.
         */
        void UpgradeRun(String address);
    }

    /**
     * the online upgrade model.
     */
    public class OnlineUpgradeModel {
        private String mClient = null;
        private String mName = null;
        private long mID = 0L;
        private String mHardware = null;

        /**
         * construct
         */
        public OnlineUpgradeModel(String client, String name, long id, String hardware) {
            mClient = client;
            mName = name;
            mID = id;
            mHardware = hardware;
        }

        /**
         * construct
         */
        public OnlineUpgradeModel(OnlineUpgradeModel model) {
            if (null != model) {
                mClient = model.mClient;
                mName = model.mName;
                mID = model.mID;
                mHardware = model.mHardware;
            }
        }
    }

    /**
     * online upgrade
     */
    public void onlineUpgrade(Context context, String client, String name, long id, String hardware) {
        if (TextUtils.isEmpty(client)) {
            UpgradeStatusChange(SettingsDefine.UpgradeStatus.ERROR_HOST_CLIENT);
        } else if (SettingsDefine.UpgradeName.error(name)) {
            UpgradeStatusChange(SettingsDefine.UpgradeStatus.ERROR_HOST_NAME);
        } else if (id < 0) {
            UpgradeStatusChange(SettingsDefine.UpgradeStatus.ERROR_HOST_VERSION_ID);
        } else if (TextUtils.isEmpty(hardware)) {
            UpgradeStatusChange(SettingsDefine.UpgradeStatus.ERROR_HOST_HARDWARE);
        } else {
            if (mRunning.get()) {
                UpgradeStatusChange(SettingsDefine.UpgradeStatus.ALREADY_CHECKING);
            } else {
                if (NetworkUtils.checkNetworkAvailable(context)) {
                    mModel = new OnlineUpgradeModel(client, name, id, hardware);
                    mRunning.set(true);
                } else {
                    UpgradeStatusChange(SettingsDefine.UpgradeStatus.ERROR_NETWORK);
                }
            }
        }
    }

    /**
     * upgrade status change.
     */
    protected void UpgradeStatusChange(int status) {
        LogUtils.d(TAG, "UpgradeStatusChange, status = " + SettingsDefine.UpgradeStatus.toString(status));
        if (null != mOnlineUpgradeListener) {
            mOnlineUpgradeListener.UpgradeStatusChange(status);
        }
    }

    /**
     * parse online json data.
     */
    protected void parseOnlineJsonData(String json) {
        if (TextUtils.isEmpty(json)) {
            LogUtils.w(TAG, "online upgrade, json data null.");
            UpgradeStatusChange(SettingsDefine.UpgradeStatus.ERROR_SERVER);
        } else {
            String address = "";
            // parse begin.
            final JSONArray array = JsonParser.parseArray(json, "data");
            if (null != array) {
                for (int i = 0; i < array.length(); i++) {
                    String data = array.opt(i).toString();
                    final String client = JsonParser.parseString(data, "customername");
                    final String name = JsonParser.parseString(data, "appname");
                    final String hardware = JsonParser.parseString(data, "hardware_version");
                    long id = 0L;
                    try {
                        id = Long.parseLong(JsonParser.parseString(data, "version"));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    if (mModel.mClient.equals(client)) {
                        if (mModel.mName.equals(name)) {
                            boolean success = false;
                            if (TextUtils.isEmpty(hardware)) {
                                // compatible
                                success = true;
                            } else if (mModel.mHardware.equals(hardware)) {
                                success = true;
                            }
                            if (success) {
                                boolean ota = false;
                                if (!mForceUpgradeAllPackage) {
                                    if (id > mModel.mID) {
                                        if (1 == (id - mModel.mID)) {
                                            ota = true;
                                        }
                                    }
                                }
                                if (ota) {
                                    address = JsonParser.parseString(data, "subpackage_appfile_url");
                                } else {
                                    address = JsonParser.parseString(data, "appfile_url");
                                }
                                if (null != address) {
                                    address = address.replace("\\", "");
                                }
                                LogUtils.d(TAG, "online upgrade, mForceUpgradeAllPackage = " + mForceUpgradeAllPackage + "id = " + id + ", mModel.mID = " + mModel.mID +
                                        ", ota = " + ota + ", address = " + address);
                                break;
                            } else {
                                LogUtils.w(TAG, "online upgrade, error hardware = " + hardware + ", mModel.mHardware = " + mModel.mHardware);
                            }
                        } else {
                            LogUtils.w(TAG, "online upgrade, error name = " + name + ", mModel.mName = " + mModel.mName);
                        }
                    } else {
                        LogUtils.w(TAG, "online upgrade, error client = " + client + ", mModel.mClient = " + mModel.mClient);
                    }
                }
            } else {
                LogUtils.w(TAG, "online upgrade, json data array is null.");
            }
            if (!TextUtils.isEmpty(address)) {
                if (Patterns.WEB_URL.matcher(address).matches()) {
                    if (null != mOnlineUpgradeListener) {
                        LogUtils.d(TAG, "online upgrade result, address = " + address);
                        mOnlineUpgradeListener.UpgradeRun(address);
                    } else {
                        LogUtils.w(TAG, "online upgrade result, mOnlineUpgradeListener is null.");
                    }
                } else {
                    LogUtils.w(TAG, "online upgrade result, address is not matcher web url.");
                    UpgradeStatusChange(SettingsDefine.UpgradeStatus.ERROR_ADDRESS);
                }
            } else {
                LogUtils.w(TAG, "online upgrade result, address is null.");
                UpgradeStatusChange(SettingsDefine.UpgradeStatus.ERROR_ADDRESS);
            }
        }
    }
}
