package com.wwc2.main.driver.sdk;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.text.TextUtils;

import com.wwc2.canbussdk.ICanbusDriver;
import com.wwc2.common_interface.utils.SwitchStatus;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.db.Result;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.driver.mcu.driver.McuDefine;
import com.wwc2.main.driver.memory.BaseMemoryDriver;
import com.wwc2.main.logic.BaseLogic;
import com.wwc2.main.manager.McuManager;
import com.wwc2.main.manager.ModuleManager;

/**
 * the base sdk memory driver.
 *
 * @author wwc2
 * @date 2017/1/9
 */
public abstract class BaseSDKMemoryDriver extends BaseMemoryDriver implements SDKable {

    /**
     * TAG
     */
    private static final String TAG = "BaseSDKMemoryDriver";

    /**
     * aidl driver.
     */
    private IInterface mAIDLDriver = null;

    /**
     * the switch status.
     */
    private Result mSwitchStatus = new Result(SwitchStatus.DEFAULT, new Result.ResultListener() {
        @Override
        public void onResult(int oldVal, int newVal) {
            switchStatusChanged(oldVal, newVal);
        }
    });

    /**
     * service connection.
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // TODO Auto-generated method stub
            LogUtils.d(TAG, name + "onServiceDisconnected...");
            // unregister the call back object.
            if (null != mAIDLDriver) {
                onSDKDisconnected(mAIDLDriver);
            }
            mAIDLDriver = null;
            mSwitchStatus.setInt(SwitchStatus.CLOSED);
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // TODO Auto-generated method stub
            LogUtils.d(TAG, name + "onServiceConnected...");
            mAIDLDriver = ICanbusDriver.Stub.asInterface(service);
            if (null != mAIDLDriver) {
                onSDKConnected(mAIDLDriver);
                mSwitchStatus.setInt(SwitchStatus.OPENED);
            }
        }
    };

    @Override
    public String filePath() {
        return null;
    }

    @Override
    public boolean writeData() {
        return false;
    }

    @Override
    public boolean readData() {
        return false;
    }

    @Override
    public boolean needMCU() {
        return false;
    }

    @Override
    public boolean sendData(byte[] val) {
        LogUtils.d(TAG, "sendData---");
        McuManager.sendMcu((byte) McuDefine.ARM_TO_MCU.OP_CanbusData, val, val.length);
        return false;
    }

    @Override
    public void recvData(byte[] val) {

    }

    @Override
    public void onCreate(Packet packet) {
        super.onCreate(packet);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * 连接SDK
     */
    public final boolean connectSDK(String name, String pkgName, String clsName, Packet packet) {
        boolean ret = false;
        Context context = getMainContext();
        if (null == context) {
            LogUtils.e(TAG, "connectSDK failed, context null exception.");
        } else {
            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(pkgName) || TextUtils.isEmpty(clsName)) {
                LogUtils.e(TAG, "connectSDK failed, invalid input param.");
            } else {
                final int status = mSwitchStatus.getInt();
                if (SwitchStatus.isClosed(status)) {
                    // handle <java.lang.IllegalArgumentException:Service not registered> exception.
                    mSwitchStatus.setInt(SwitchStatus.OPENING);
                    Intent intent = new Intent(name);
                    ComponentName component = new ComponentName(pkgName, clsName);
                    intent.setComponent(component);
                    if (null != packet) {
                        Bundle bundle = packet.getBundle();
                        if (null != bundle) {
                            intent.putExtras(bundle);
                        }
                    }
                    context.bindService(intent,
                            mConnection, Context.BIND_AUTO_CREATE);
                    ret = true;
                } else {
                    LogUtils.w(TAG, "connectSDK failed, Not in closed, status = " + SwitchStatus.toString(status));
                }
            }
        }
        return ret;
    }

    /**
     * 断开SDK连接
     */
    public final boolean disconnectSDK() {
        boolean ret = false;

        Context context = getMainContext();
        if (null == context) {
            LogUtils.e(TAG, "disconnectSDK failed, context null exception.");
        } else {
            final int status = mSwitchStatus.getInt();
            if (SwitchStatus.isOpened(status)) {
                /**disconnect.*/
                mSwitchStatus.setInt(SwitchStatus.CLOSING);
                if (null != mConnection) {
                    mConnection.onServiceDisconnected(null);
                }
                // unbind the service.
                context.unbindService(mConnection);
                ret = true;
            } else {
                LogUtils.w(TAG, "disconnectSDK failed, Not in opened, status = " + SwitchStatus.toString(status));
            }
        }
        return ret;
    }

    /**
     * 需要MCU交互改变了
     */
    protected final void needMCUChanged(boolean need) {
        if (need) {
            // 选择为MCU串口方式
//            McuManager.getModel().bindListener(mMcuListener);
        } else {
            // 选择不是MCU串口方式
//            McuManager.getModel().unbindListener(mMcuListener);
        }
    }

    /**MAIN接口*/
    protected final boolean _callApkToMain(String s, int i, Bundle bundle) {
        boolean ret = false;
        if (!TextUtils.isEmpty(s)) {
            BaseLogic logic = ModuleManager.getLogicByName(s);
            if (null != logic) {
                ret = true;
                logic.dispatch(i, new Packet(bundle));
            }
        }
        return ret;
    }

    /**MAIN接口*/
    protected final boolean _callMainToApk(String s, int i, Bundle bundle) {
        boolean ret = false;
        if (!TextUtils.isEmpty(s)) {
            BaseLogic logic = ModuleManager.getLogicByName(s);
            if (null != logic) {
                ret = true;
                logic.Notify(i, new Packet(bundle));
            }
        }
        return ret;
    }

    /**MAIN接口*/
    protected final boolean _sendData(byte[] bytes) {
        return sendData(bytes);
    }

    /**MAIN接口*/
    protected final Bundle _getMainInfo(String s) {
        Bundle bundleInfo = new Bundle();
        if (!TextUtils.isEmpty(s)) {
            BaseLogic logic = ModuleManager.getLogicByName(s);
            if (null != logic) {
                bundleInfo = logic.getInfo().getBundle();
            }
        }
        return bundleInfo;
    }
}
