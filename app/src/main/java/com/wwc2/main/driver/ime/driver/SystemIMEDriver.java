package com.wwc2.main.driver.ime.driver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.utils.log.LogUtils;

/**
 * the system ime driver.
 *
 * @author wwc2
 * @date 2017/1/2
 */
public class SystemIMEDriver extends BaseIMEDriver {

    /**
     * TAG
     */
    private static final String TAG = "SystemIMEDriver";

    // 广播监听
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_INPUT_METHOD_CHANGED.equals(action)) {
                // 输入方法ID发生变化
                String id = intent.getStringExtra("input_method_id");
                LogUtils.d(TAG, "receiver action = " + action + ", input_method_id = " + id);
                Model().getInputMethodID().setVal(id);
            }
        }
    };

    @Override
    public void onCreate(Packet packet) {
        super.onCreate(packet);

        Context context = getMainContext();
        if (null != context) {
            IntentFilter myIntentFilter = new IntentFilter();
            myIntentFilter.addAction(Intent.ACTION_INPUT_METHOD_CHANGED);
            context.registerReceiver(mBroadcastReceiver, myIntentFilter);
        }
    }

    @Override
    public void onDestroy() {
        Context context = getMainContext();
        if (null != context) {
            try {
                context.unregisterReceiver(mBroadcastReceiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        super.onDestroy();
    }
}
