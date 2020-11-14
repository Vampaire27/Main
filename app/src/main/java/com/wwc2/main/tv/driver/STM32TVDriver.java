package com.wwc2.main.tv.driver;

import android.content.Context;
import android.util.DisplayMetrics;

import com.wwc2.corelib.db.FormatData;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.driver.mcu.driver.McuDefine;
import com.wwc2.main.manager.McuManager;

/**
 * the stm32 tv driver.
 *
 * @author wwc2
 * @date 2017/1/10
 */
public class STM32TVDriver extends BaseTVDriver {

    /**
     * TAG
     */
    private static final String TAG = "STM32TVDriver";

    /**
     * screen width
     */
    private int mWidth = 0;
    /**
     * screen height
     */
    private int mHeight = 0;

    /**the mcu listener*/
    private McuManager.MCUListener mMCUListener = new McuManager.MCUListener() {
        @Override
        public void OpenListener(int status) {
            // set ir tv
            //begin zhongyang remove the no use cmd.
            //byte[] data = {(byte) 0x00, (byte) 0x00, (byte) 0x64, (byte) 0x32};
            //send((byte) McuDefine.ARM_TO_MCU.OP_ExternIRSet, data);
            //end
        }
    };

    @Override
    public void onCreate(Packet packet) {
        super.onCreate(packet);

        // get screen width and height.
        Context context = getMainContext();
        if (null != context) {
            DisplayMetrics dm = new DisplayMetrics();
            dm = context.getResources().getDisplayMetrics();
            mWidth = dm.widthPixels;
            mHeight = dm.heightPixels;
        }

        McuManager.getModel().bindListener(mMCUListener);
    }

    @Override
    public void onDestroy() {
        McuManager.getModel().unbindListener(mMCUListener);
        super.onDestroy();
    }

    @Override
    public void postXY(float x, float y) {
        if (0 != mWidth && 0 != mHeight) {
            final byte xx = FloatToData(x, mWidth);
            final byte yy = FloatToData(y, mHeight);
            byte[] data = {0x00, (byte) 0x55, (byte) 0xaa, xx, yy};
            LogUtils.d(TAG, "postXY, x = " + x + ", y = " + y +
                    ", mWidth = " + mWidth + ", mHeight = " + mHeight +
                    ", xx = " + xx + ", yy = " + yy);
            send((byte) McuDefine.ARM_TO_MCU.OP_ExternUartData, data);
        } else {
            LogUtils.e(TAG, "postXY failed, mWidth = " + mWidth + ", mHeight = " + mHeight);
        }
    }

    @Override
    public void power() {
        sendCmd(0x00);
    }

    @Override
    public void menu() {
        sendCmd(0x17);
    }

    @Override
    public void exit() {
        sendCmd(0x12);
    }

    @Override
    public void up() {
        sendCmd(0x10);
    }

    @Override
    public void down() {
        sendCmd(0x14);
    }

    @Override
    public void left() {
        sendCmd(0x11);
    }

    @Override
    public void right() {
        sendCmd(0x13);
    }

    @Override
    public void ok() {
        sendCmd(0x16);
    }

    @Override
    public void scan() {
        sendCmd(0x19);
    }

    @Override
    public void pvr() {
        sendCmd(0x20);
    }

    /**
     * 发送控制命令
     */
    private void sendCmd(int cmd) {
        byte[] data = {0x00, 0x68, (byte) 0x97, (byte) cmd, (byte) (0xff - cmd)};
        send((byte) McuDefine.ARM_TO_MCU.OP_ExternUartData, data);
    }

    /**发送MCU*/
    private void send(int head, byte[] data) {
        int length = 0;
        if (null != data) {
            length = data.length;
        }
        McuManager.sendMcuImportant((byte) head, data, length);
        // 打印MCU数据
        byte[] _head = new byte[1];
        _head[0] = (byte)head;
        final String string = "ARM --> MCU: head = " + FormatData.formatHexBufToString(_head, _head.length) +
                ", buf = " + FormatData.formatHexBufToString(data, length);
        LogUtils.d(TAG, string);
    }

    /**
     * float 转换为 byte
     */
    private byte FloatToData(float v, int full) {
        byte ret = (byte) 0xff;
        if (0 != full) {
            ret = (byte) ((int)(255 * v) / full);
        } else {
            LogUtils.e(TAG, "FloatToData full is zero, v = " + v);
        }
        return ret;
    }
}
