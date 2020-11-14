package com.wwc2.main.driver.network.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Parcelable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

public class WifiApiManager {
    /**
      * @ need permission
         <uses-permission android:name="android.permission.CHANGE_NETWORK_STATE"/>
         <uses-permission android:name="android.permission.CHANGE_WIFI_STATE"/>
         <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
         <uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
    */

    private static WifiApiManager mWifiApiManager;

    private List<WifiConfiguration> mWifiConfiguration; //无线网络配置信息类集合(网络连接列表)
    private WifiInfo mWifiInfo;                         //描述任何Wifi连接状态
    private WifiManager.WifiLock mWifilock;             //能够阻止wifi进入睡眠状态，使wifi一直处于活跃状态
    private WifiManager mWifiManager;

    public static WifiApiManager newInstance(Context context) {
        mWifiApiManager = new WifiApiManager(context);
        return mWifiApiManager;
    }

    public static WifiApiManager getInstance() {
        return mWifiApiManager;
    }

    public static WifiManager getWifiManager(Context context) {
        return (context != null)? (WifiManager)context.getSystemService(Context.WIFI_SERVICE) : null;
    }

    private  WifiApiManager(Context context) {
        if (context != null) {
            mWifiManager = getWifiManager(context);
            mWifiInfo = mWifiManager.getConnectionInfo();
        }
    }

    /**
     * 是否存在网络信息
     * @param str  热点名称
     * @return
     */
    private WifiConfiguration isExsits(String str) {
        Iterator localIterator = mWifiManager.getConfiguredNetworks().iterator();
        WifiConfiguration localWifiConfiguration;
        do {
            if(!localIterator.hasNext()) return null;
            localWifiConfiguration = (WifiConfiguration) localIterator.next();
        }while(!localWifiConfiguration.SSID.equals("\"" + str + "\""));
        return localWifiConfiguration;
    }

    /**打开Wifi**/
    public void openWifi() {
        if(!mWifiManager.isWifiEnabled()){ //当前wifi不可用
            mWifiManager.setWifiEnabled(true);
        }
    }
    /**关闭Wifi**/
    public void closeWifi() {
        if(mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(false);
        }
    }
    /**端口指定id的wifi**/
    public void disconnectWifi(int paramInt) {
        mWifiManager.disableNetwork(paramInt);
    }

    /**锁定WifiLock，当下载大文件时需要锁定 **/
    public void acquireWifiLock() {
        mWifilock.acquire();
    }
    /**创建一个WifiLock**/
    public void createWifiLock() {
        mWifilock = mWifiManager.createWifiLock("Test");
    }
    /**解锁WifiLock**/
    public void releaseWifilock() {
        if(mWifilock.isHeld()) { //判断时候锁定
            mWifilock.acquire();
        }
    }

    /**添加指定网络**/
    public void addNetwork(WifiConfiguration paramWifiConfiguration) {
        int i = mWifiManager.addNetwork(paramWifiConfiguration);
        mWifiManager.enableNetwork(i, true);
    }

    /**
     * 连接指定配置好的网络
     * @param index 配置好网络的ID
     */
    public void connectConfiguration(int index) {
        // 索引大于配置好的网络索引返回
        if (index > mWifiConfiguration.size()) {
            return;
        }
        //连接配置好的指定ID的网络
        mWifiManager.enableNetwork(mWifiConfiguration.get(index).networkId, true);
    }

    /**
     * 根据wifi信息创建或关闭一个热点
     * @param paramWifiConfiguration
     * @param paramBoolean 关闭标志
     */
    public void createWifiAP(WifiConfiguration paramWifiConfiguration,boolean paramBoolean) {
        try {
            Class localClass = mWifiManager.getClass();
            Class[] arrayOfClass = new Class[2];
            arrayOfClass[0] = WifiConfiguration.class;
            arrayOfClass[1] = Boolean.TYPE;
            Method localMethod = localClass.getMethod("setWifiApEnabled",arrayOfClass);
            WifiManager localWifiManager = mWifiManager;
            Object[] arrayOfObject = new Object[2];
            arrayOfObject[0] = paramWifiConfiguration;
            arrayOfObject[1] = Boolean.valueOf(paramBoolean);
            localMethod.invoke(localWifiManager, arrayOfObject);
            return;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 创建一个wifi信息
     * @param ssid 名称
     * @param passawrd 密码
     * @param paramInt 有3个参数，1是无密码，2是简单密码，3是wap加密
     * @param type 是"ap"还是"wifi"
     * @return
     */
    public WifiConfiguration createWifiInfo(String ssid, String passawrd,int paramInt, String type) {
        //配置网络信息类
        WifiConfiguration localWifiConfiguration1 = new WifiConfiguration();
        //设置配置网络属性
        localWifiConfiguration1.allowedAuthAlgorithms.clear();
        localWifiConfiguration1.allowedGroupCiphers.clear();
        localWifiConfiguration1.allowedKeyManagement.clear();
        localWifiConfiguration1.allowedPairwiseCiphers.clear();
        localWifiConfiguration1.allowedProtocols.clear();

        if(type.equals("wt")) { //wifi连接
            localWifiConfiguration1.SSID = ("\"" + ssid + "\"");
            WifiConfiguration localWifiConfiguration2 = isExsits(ssid);
            if(localWifiConfiguration2 != null) {
                mWifiManager.removeNetwork(localWifiConfiguration2.networkId); //从列表中删除指定的网络配置网络
            }
            if(paramInt == 1) { //没有密码
                localWifiConfiguration1.wepKeys[0] = "";
                localWifiConfiguration1.allowedKeyManagement.set(0);
                localWifiConfiguration1.wepTxKeyIndex = 0;
            } else if(paramInt == 2) { //简单密码
                localWifiConfiguration1.hiddenSSID = true;
                localWifiConfiguration1.wepKeys[0] = ("\"" + passawrd + "\"");
            } else { //wap加密
                localWifiConfiguration1.preSharedKey = ("\"" + passawrd + "\"");
                localWifiConfiguration1.hiddenSSID = true;
                localWifiConfiguration1.allowedAuthAlgorithms.set(0);
                localWifiConfiguration1.allowedGroupCiphers.set(2);
                localWifiConfiguration1.allowedKeyManagement.set(1);
                localWifiConfiguration1.allowedPairwiseCiphers.set(1);
                localWifiConfiguration1.allowedGroupCiphers.set(3);
                localWifiConfiguration1.allowedPairwiseCiphers.set(2);
            }
        }else {//"ap" wifi热点
            localWifiConfiguration1.SSID = ssid;
            localWifiConfiguration1.allowedAuthAlgorithms.set(1);
            localWifiConfiguration1.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            localWifiConfiguration1.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            localWifiConfiguration1.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            localWifiConfiguration1.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            localWifiConfiguration1.allowedKeyManagement.set(0);
            localWifiConfiguration1.wepTxKeyIndex = 0;
            if (paramInt == 1) {  //没有密码
                localWifiConfiguration1.wepKeys[0] = "";
                localWifiConfiguration1.allowedKeyManagement.set(0);
                localWifiConfiguration1.wepTxKeyIndex = 0;
            } else if (paramInt == 2) { //简单密码
                localWifiConfiguration1.hiddenSSID = true;//网络上不广播ssid
                localWifiConfiguration1.wepKeys[0] = passawrd;
            } else if (paramInt == 3) {//wap加密
                localWifiConfiguration1.preSharedKey = passawrd;
                localWifiConfiguration1.allowedAuthAlgorithms.set(0);
                localWifiConfiguration1.allowedProtocols.set(1);
                localWifiConfiguration1.allowedProtocols.set(0);
                localWifiConfiguration1.allowedKeyManagement.set(1);
                localWifiConfiguration1.allowedPairwiseCiphers.set(2);
                localWifiConfiguration1.allowedPairwiseCiphers.set(1);
            }
        }
        return localWifiConfiguration1;
    }

    /**获取热点名**/
    public String getApSSID() {
        try {
            Method localMethod = mWifiManager.getClass().getDeclaredMethod("getWifiApConfiguration", new Class[0]);
            if (localMethod == null) return null;
            Object localObject1 = localMethod.invoke(mWifiManager,new Object[0]);
            if (localObject1 == null) return null;
            WifiConfiguration localWifiConfiguration = (WifiConfiguration) localObject1;
            if (localWifiConfiguration.SSID != null) return localWifiConfiguration.SSID;
            Field localField1 = WifiConfiguration.class .getDeclaredField("mWifiApProfile");
            if (localField1 == null) return null;
            localField1.setAccessible(true);
            Object localObject2 = localField1.get(localWifiConfiguration);
            localField1.setAccessible(false);
            if (localObject2 == null)  return null;
            Field localField2 = localObject2.getClass().getDeclaredField("SSID");
            localField2.setAccessible(true);
            Object localObject3 = localField2.get(localObject2);
            if (localObject3 == null) return null;
            localField2.setAccessible(false);
            String str = (String) localObject3;
            return str;
        } catch (Exception localException) {
        }
        return null;
    }

    /**得到配置好的网络 **/
    public List<WifiConfiguration> getConfiguration() {
        return mWifiConfiguration;
    }

    /**获取ip地址**/
    public int getIPAddress() {
        return (mWifiInfo == null) ? 0 : mWifiInfo.getIpAddress();
    }
    /**获取物理地址(Mac)**/
    public String getMacAddress() {
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.getMacAddress();
    }

    /**获取网络id**/
    public int getNetworkId() {
        return (mWifiInfo == null) ? 0 : mWifiInfo.getNetworkId();
    }
    /**获取热点创建状态**/
    public int getWifiApState() {
        try {
            int i = ((Integer) mWifiManager.getClass()
                    .getMethod("getWifiApState", new Class[0])
                    .invoke(mWifiManager, new Object[0])).intValue();
            return i;
        } catch (Exception localException) {
        }
        return 4;   //未知wifi网卡状态
    }

    /**
     * 获取wifi连接信息
     */
    public WifiInfo getWifiInfo() {
        return mWifiManager.getConnectionInfo();
    }

    /**
     * 开始扫描wifi
     */
    public void startScan() {
        mWifiManager.startScan();
    }

    /**
     * 获取wifi扫描列表
     * @return wifi list
     */
    public List<ScanResult> getWifiList() {
        return mWifiManager.getScanResults();
    }

    /**
     * 获取wifi扫描结果
     * @return results string
     */
    public StringBuilder getWifiListResults() {
        StringBuilder localStringBuilder = new StringBuilder();
        List<ScanResult> list = mWifiManager.getScanResults();
        for (int i = 0; i < list.size(); i++) {
            localStringBuilder.append("Index_"+new Integer(i + 1).toString() + ":");
            //将ScanResult信息转换成一个字符串包(BSSID、SSID、capabilities、frequency、level)
            localStringBuilder.append((list.get(i)).toString());
            localStringBuilder.append("\n");
        }
        return localStringBuilder;
    }

    /**
     * 获取wifi 的 BSSID
     * @return  BSSID
     */
    public String getBSSID() {
        return  mWifiInfo.getBSSID();
    }

    public static abstract class Scan extends BroadcastReceiver {
        public static final int STATE_WIFI_DISABLE = 0;
        public static final int STATE_SCAN_TIMEOUT = 1;
        public static final int STATE_SCAN_SUCCEED = 2;
        public static final int STATE_SERVICE_FAIL = 3;

        private Context mContext;
        private WifiManager mWifiManager;
        private WifiManager.WifiLock mWifiLock;
        private int mTimerOutPeriod;
        private Boolean mLocker = new Boolean(false);
        public Scan(Context context, int period) {
            if (context != null) {
                mContext = context;
                mTimerOutPeriod = period;
                mWifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
                mWifiLock = mWifiManager.createWifiLock(toString());
            }
        }

        public void lock() {
            if (mWifiLock != null) {
                mWifiLock.acquire();
            }
        }

        public void unlock() {
            if (mWifiLock != null) {
                mWifiLock.release();
            }
        }

        public boolean start() {
            boolean ret = false;
            if (mWifiManager != null) {
                if (mWifiManager.isWifiEnabled()) {
                    lock();
                    mContext.registerReceiver(this, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
                    ret = mWifiManager.startScan();
                    if (ret) {
                        try {
                            mLocker = true;
                            mLocker.wait(mTimerOutPeriod);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (mLocker) {
                            onState(STATE_SCAN_TIMEOUT);
                            mContext.unregisterReceiver(Scan.this);
                            unlock();
                        }
                    } else {
                        mContext.unregisterReceiver(this);
                        unlock();
                    }
                } else {
                    onState(STATE_WIFI_DISABLE);
                }
            }
            return ret;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (mWifiManager != null) {
                mLocker = false;
                mLocker.notifyAll();
                onResult(mWifiManager.getScanResults());
            } else {
                onState(STATE_SERVICE_FAIL);
            }
        }

        /**
         * scan result
         * @param resultList null is timeout
         */
        public abstract void onResult(List<ScanResult> resultList);
        public void onState(int state) {}
    }

    public static class Parse {

    }

    public static class StateMonitor extends BroadcastReceiver {
        /**
         * Action description
         * @WifiManager.WIFI_STATE_CHANGED_ACTION
        Description:
        occurs when opened and closed wifi
        Extra:
        WifiManager.EXTRA_PREVIOUS_WIFI_STATE
        Value:
        WifiManager.WIFI_STATE_DISABLED(1)
        WifiManager.WIFI_STATE_DISABLING(0)
        WifiManager.WIFI_STATE_ENABLED(3)
        WifiManager.WIFI_STATE_ENABLING(2)
        WifiManager.WIFI_STATE_UNKNOWN(4)
        WifiManager.EXTRA_WIFI_STATE
        Value:
        WifiManager.WIFI_STATE_DISABLED(1)
        WifiManager.WIFI_STATE_DISABLING(0)
        WifiManager.WIFI_STATE_ENABLED(3)
        WifiManager.WIFI_STATE_ENABLING(2)
        WifiManager.WIFI_STATE_UNKNOWN(4)
         * @WifiManager.NETWORK_STATE_CHANGED_ACTION
        Description:
        occurs when wifi state changed
        Extra:
        WifiManager.EXTRA_BSSID
        WifiManager.EXTRA_NETWORK_INFO
         * @ConnectivityManager.CONNECTIVITY_ACTION
        Description:
        Monitor network connection settings, including the opening and closing wifi and mobile data

         * @WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION
        Description:
        It represents only wifi connection status change, regardless of the network
        Extra:
        WifiManager.EXTRA_SUPPLICANT_CONNECTED
        TRUE:wifi connected
        FALSE: wifi disconnected
         * @WifiManager.SUPPLICANT_STATE_CHANGED_ACTION
        Description:
        Send WIFI connection process information, if an error, ERROR will received. When connecting WIFI trigger, the trigger several times
        Extra:
        WifiManager.EXTRA_NEW_STATE
        WifiManager.EXTRA_SUPPLICANT_ERROR
         */
        public StateMonitor(Context context) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
            filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
            context.registerReceiver(this, filter);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) return;
            if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                onWifiStateChanged(intent.getIntExtra(WifiManager.EXTRA_PREVIOUS_WIFI_STATE, WifiManager.WIFI_STATE_DISABLED),
                        intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_DISABLED));
            } else if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                onNetworkStateChanged(intent.getStringExtra(WifiManager.EXTRA_BSSID),
                        intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO));
            }
        }

        public void onWifiStateChanged(int preState, int curSate) {}
        public void onNetworkStateChanged(String rssid, Parcelable info) {}
    }
}