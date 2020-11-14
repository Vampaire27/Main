package com.wwc2.main.driver.tptouch.driver;

import android.graphics.Rect;
import android.text.TextUtils;

import com.wwc2.common_interface.Define;
import com.wwc2.corelib.callback.BaseCallback;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.model.BaseModel;
import com.wwc2.corelib.model.MBoolean;
import com.wwc2.corelib.model.MInteger;
import com.wwc2.corelib.model.MIntegerArray;
import com.wwc2.corelib.model.custom.FourInteger;
import com.wwc2.corelib.model.custom.MFourIntegerArray;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.corelib.utils.timer.TimerQueue;
import com.wwc2.corelib.utils.timer.TimerUtils;
import com.wwc2.corelib.utils.timer.Timerable;
import com.wwc2.main.driver.factory.FactoryDriver;
import com.wwc2.main.driver.memory.BaseMemoryDriver;
import com.wwc2.main.driver.memory.mode.ini.IniMemory;
import com.wwc2.main.driver.tptouch.TPTouchDriverable;
import com.wwc2.main.driver.tptouch.TPTouchListener;
import com.wwc2.main.manager.EventInputManager;
import com.wwc2.main.manager.SourceManager;
import com.wwc2.main.settings.util.FileUtil;
import com.wwc2.settings_interface.SettingsDefine;

import static com.wwc2.main.settings.SettingsLogic.TP_COORDINATE_SWITCH_NODE;

/**
 * the base tp driver.
 *
 * @author wwc2
 * @date 2017/1/22
 */
public abstract class BaseTpTouchDriver extends BaseMemoryDriver implements TPTouchDriverable {

    /**
     * TAG
     */
    public static final String TAG = "BaseTpTouchDriver";

    /**
     * save the code.
     */
    private boolean mSave = false;

    /**
     * 截获TP坐标列表
     */
    private TPTouchListener.TPPositionListener tpPositionListener;

    /**
     * 按下的按键值
     */
    private int mDownKeyCode = Define.Key.KEY_NONE;

    /**
     * 长按键延时器
     */
    private TimerQueue mLongKeyTimerQueue = new TimerQueue();

    /**
     * 持续按键定时器ID
     */
    private int mLastKeyTimerId = 0;
    /**
     * 持续按键按键值
     */
    private int mLastKeyCode = Define.Key.KEY_NONE;

    /**
     * 长按事件标志位
     */
    private boolean isLastKeyEvent = false;

    boolean mbUpComming = true;

    /**
     * 数据Model
     */
    protected static class TpModel extends BaseModel {
        @Override
        public Packet getInfo() {
            Packet ret = new Packet();
            ret.putParcelableArray("TouchRects", mTouchRects.getVal());
            ret.putParcelableArray("Rects", mRects.getVal());
            ret.putIntegerObjectArray("Codes", mCodes.getVal());
            ret.putIntegerObjectArray("LongCodes", mLongCodes.getVal());
            ret.putIntegerObjectArray("LastCodes", mLastCodes.getVal());
            ret.putBoolean("Learn", mLearn.getVal());
            ret.putIntegerObjectArray("Init", mInit.getVal());
            ret.putInt("PixelOffset", mPixelOffset.getVal());
            ret.putInt(SettingsDefine.TPCoordinate.SWITCH, mTPCoordinateSwitch.getVal());
            return ret;
        }

        /**
         * 可触摸区域
         */
        private MFourIntegerArray mTouchRects = new MFourIntegerArray(this, "TouchRectsListener", null);

        public MFourIntegerArray getTouchRects() {
            return mTouchRects;
        }

        /**
         * 区域值
         */
        private MFourIntegerArray mRects = new MFourIntegerArray(this, "RectsListener", null);

        public MFourIntegerArray getRects() {
            return mRects;
        }

        /**
         * 短按按键码
         */
        private MIntegerArray mCodes = new MIntegerArray(this, "CodesListener", null);

        public MIntegerArray getCodes() {
            return mCodes;
        }

        /**
         * 长按按键码
         */
        private MIntegerArray mLongCodes = new MIntegerArray(this, "LongCodesListener", null);

        public MIntegerArray getLongCodes() {
            return mLongCodes;
        }

        /**
         * 持续按键码
         */
        private MIntegerArray mLastCodes = new MIntegerArray(this, "LastCodesListener", null);

        public MIntegerArray getLastCodes() {
            return mLastCodes;
        }

        /**
         * TP学习
         */
        private MBoolean mLearn = new MBoolean(this, "LearnListener", false);

        public MBoolean getLearn() {
            return mLearn;
        }

        /**
         * 绑定时候,初始化
         */
        private MIntegerArray mInit = new MIntegerArray(this, "InitListener", null);

        public MIntegerArray getInit() {
            return mInit;
        }

        /**
         * 学习按键的坐标点，上下左右偏移的像素
         */
        private MInteger mPixelOffset = new MInteger(this, "PixelOffsetListener", 20);

        public MInteger getPixelOffset() {
            return mPixelOffset;
        }

        /**
         * 触摸屏坐标交换标志  0:默认值 不交换,1:交换 (bit0:x坐标左右翻转,bit1:y坐标上下翻转,bit2:x和y互换)
         */
        private MInteger mTPCoordinateSwitch = new MInteger(this, "TPCoordinateSwitchListener", 1);

        public MInteger getmTPCoordinateSwitch() {
            return mTPCoordinateSwitch;
        }
    }

    /**
     * the tp touch listener.
     */
    private TPTouchListener mListener = new TPTouchListener() {
        @Override
        public void CodesListener(Integer[] oldVal, Integer[] newVal) {
            // 按键码变化，则进行数据保存
            mSave = true;
        }

        @Override
        public void RectsListener(FourInteger[] oldVal, FourInteger[] newVal) {
            // 按键区域变化，则进行数据保存
            mSave = true;
        }
    };

    @Override
    public BaseModel newModel() {
        return new TpModel();
    }

    /**
     * get the model object.
     */
    protected TpModel Model() {
        TpModel ret = null;
        BaseModel model = getModel();
        if (model instanceof TpModel) {
            ret = (TpModel) model;
        }
        return ret;
    }

    @Override
    public void onCreate(Packet packet) {
        super.onCreate(packet);

        getModel().bindListener(mListener);
    }

    @Override
    public void onDestroy() {
        getModel().unbindListener(mListener);

        super.onDestroy();
    }

    @Override
    public void bindTPPositionListener(TPTouchListener.TPPositionListener tpPositionListener) {
        if (null != tpPositionListener) {
            this.tpPositionListener = tpPositionListener;
            Model().getInit().setVal(Model().getCodes().getVal());
        }
    }

    @Override
    public void unbindTPPositionListener(TPTouchListener.TPPositionListener tpPositionListener) {
        if (null != tpPositionListener) {
            Model().getInit().setVal(null);
            this.tpPositionListener = null;
        }
    }

    @Override
    public void reset() {
        Model().getRects().setVal(null);
        Model().getCodes().setVal(null);
        Model().getLongCodes().setVal(null);
        Model().getLastCodes().setVal(null);
        // 清空TP按键值
        if (null != mMemory) {
            mMemory.clear();
        }
    }

    @Override
    public boolean TPCode(int x, int y, int code, int longCode, int lastCode) {
        boolean ret = false;

        /**根据传入坐标点为中心，偏移{@link #pixelOffset()}为触摸有效区域.*/
        final int offset = Model().getPixelOffset().getVal();
        if (offset > 0) {
            FourInteger rect = new FourInteger(x - offset, y - offset, (offset << 1), (offset << 1));
            final int index = getRectConflictIndex(rect);
            if (-1 == index) {
                Model().getRects().addVal(-1, rect);
                Model().getCodes().addVal(-1, code);
                Model().getLongCodes().addVal(-1, longCode);
                Model().getLastCodes().addVal(-1, lastCode);
                LogUtils.d(TAG, "TP learn success, key = " + Define.Key.toString(code) +
                        ", longCode = " + Define.Key.toString(longCode) +
                        ", lastCode = " + Define.Key.toString(lastCode));
            } else {
                LogUtils.d(TAG, "TP learn cover, key = " + Define.Key.toString(code) + ", cover = " + Define.Key.toString(Model().getCodes().getVal()[index]));
                Model().getCodes().setVal(index, code);
                Model().getLongCodes().setVal(index, longCode);
                Model().getLastCodes().setVal(index, lastCode);
            }
        }
        return ret;
    }

    @Override
    public void enterLearn() {
        Model().getLearn().setVal(true);
    }

    @Override
    public void leaveLearn() {
        Model().getLearn().setVal(false);

        if (mSave) {
            mSave = false;

            // 保存TP按键值
            if (null != mMemory) {
                mMemory.save();
            }
        }
    }

    @Override
    public void setTpCoordinateSwitch(int t) {
        if (Model().getmTPCoordinateSwitch().setVal(t)) {
            //坐标转换状态改变后立刻保存到配置文件
            if (null != mMemory) {
                mMemory.save();
            }
        }
    }

    @Override
    public void reloadConfig() {
        if (mMemory != null && mMemory instanceof IniMemory) {
            ((IniMemory) mMemory).reloadIniFile();
            readData();
        }
    }

    @Override
    public String filePath() {
        return "TpTouchConfig.ini";
    }

    @Override
    public boolean readData() {
        boolean ret = false;
        if (null != mMemory) {
            // 解析可触摸区域
            LogUtils.d(TAG, "TP read start.");
            Object object = mMemory.get("RECT", "count");
            if (object instanceof String) {
                String temp = (String) object;
                try {
                    final int max = Integer.parseInt(temp.trim());
                    if (max > 0) {
                        FourInteger[] rects = new FourInteger[max];
                        for (int i = 0; i < max; i++) {
                            final String key = "rect" + (i + 1);
                            object = mMemory.get("RECT", key);
                            if (object instanceof String) {
                                temp = (String) object;
                                FourInteger fourInteger = getRect(temp);
                                rects[i] = fourInteger;
                            }
                        }

                        Model().getTouchRects().setVal(rects);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // 解析按键区域
            object = mMemory.get("KEYCOUNT", "count");
            if (object instanceof String) {
                String temp = (String) object;
                try {
                    final int max = Integer.parseInt(temp.trim());
                    if (max > 0) {
                        Integer[] codes = new Integer[max];
                        Integer[] longs = new Integer[max];
                        Integer[] lasts = new Integer[max];
                        FourInteger[] rects = new FourInteger[max];
                        for (int i = 0; i < max; i++) {
                            final String key = "KEY" + (i + 1);
                            object = mMemory.get(key, "code");
                            if (object instanceof String) {
                                codes[i] = Integer.parseInt(((String) object).trim());
                            } else {
                                codes[i] = Define.Key.KEY_NONE;
                            }
                            object = mMemory.get(key, "long");
                            if (object instanceof String) {
                                longs[i] = Integer.parseInt(((String) object).trim());
                            } else {
                                longs[i] = Define.Key.KEY_NONE;
                            }
                            object = mMemory.get(key, "last");
                            if (object instanceof String) {
                                lasts[i] = Integer.parseInt(((String) object).trim());
                            } else {
                                lasts[i] = Define.Key.KEY_NONE;
                            }
                            object = mMemory.get(key, "rect");
                            if (object instanceof String) {
                                temp = (String) object;
                                FourInteger fourInteger = getRect(temp);
                                rects[i] = fourInteger;
                            }
                        }

                        Model().getCodes().setVal(codes);
                        Model().getLongCodes().setVal(longs);
                        Model().getLastCodes().setVal(lasts);
                        Model().getRects().setVal(rects);
                    }
                } catch (Exception e) {
                    LogUtils.w(TAG, "TP read, exception = " + e.toString());
                    e.printStackTrace();
                }
            } else {
                LogUtils.w(TAG, "TP read, object is not instanceof String, object = " + object);
            }
            object = mMemory.get("SWITCH", "tpCoordinateSwitch");
            if (object instanceof String) {
                String str = (String) object;
                Integer tpCoordinateSwitch = Integer.valueOf(str);
                Model().getmTPCoordinateSwitch().setVal(tpCoordinateSwitch);
                // FIXME: 17-9-12 开机读取数据时还未设置监听器,不能走到监听器中写入节点,暂时写在这里
                FileUtil.write(tpCoordinateSwitch + "", TP_COORDINATE_SWITCH_NODE);
            }

            object = mMemory.get("OFFSET", "tpTouchOffset");
            if (object instanceof String) {
                String s = (String) object;
                Integer offset = Integer.parseInt(s);
                Model().getPixelOffset().setVal(offset);
            }


            LogUtils.d(TAG, "TP read end.");
            ret = true;
        } else {
            LogUtils.w(TAG, "TP file handler null exception.");
        }

        LogUtils.d(TAG, "parse tp touch file = " + absoluteFilePath());
        return ret;
    }

    @Override
    public boolean writeData() {
        //add by hwei. 180106  no need to wirte TpTouchconfig.ini on poweroff source;
        if (SourceManager.getCurSource() == Define.Source.SOURCE_POWEROFF ||
                SourceManager.getCurSource() == Define.Source.SOURCE_ACCOFF) {
            return false;
        }
        //end
        boolean ret = false;
        if (null != mMemory) {
            // 保存TP按键值
            LogUtils.d(TAG, "TP write start.");
            FourInteger[] rects = Model().getRects().getVal();
            Integer[] codes = Model().getCodes().getVal();
            Integer[] longs = Model().getLongCodes().getVal();
            Integer[] lasts = Model().getLastCodes().getVal();
            if (null != rects && null != codes) {
                if (null != longs) {
                    if (null != lasts) {
                        final int length = codes.length;
                        if (length >= longs.length && length >= lasts.length) {
                            mMemory.set("KEYCOUNT", "count", length+"");
                            for (int i = 0; i < length; i++) {
                                final String section = "KEY" + (i + 1);
                                final String rect = ("{" +
                                        rects[i].getInteger1() + "," +
                                        rects[i].getInteger2() + "," +
                                        (rects[i].getInteger1() + rects[i].getInteger3()) + "," +
                                        (rects[i].getInteger2() + rects[i].getInteger4() + "}"));
                                mMemory.set(section, "code", codes[i]+"");
                                mMemory.set(section, "long", longs[i]+"");
                                mMemory.set(section, "last", lasts[i]+"");
                                mMemory.set(section, "rect", rect);
                            }
                            ret = true;
            } else {
                            LogUtils.e(TAG, "TP length error, code_length = " + length +
                                    ", long_length = " + longs.length +
                                    ", last_length = " + lasts.length);
                        }
                    } else {
                        LogUtils.e(TAG, "TP lasts null exception.");
                    }
                } else {
                    LogUtils.e(TAG, "TP longs null exception.");
                }
            } else {
                LogUtils.d(TAG, "TP have not any data to write it.");
            }

            Integer tpCoordinateSwitch = Model().getmTPCoordinateSwitch().getVal();
            mMemory.set("SWITCH", "tpCoordinateSwitch", tpCoordinateSwitch+"");
            LogUtils.d(TAG, "TP write end.");
        } else {
            LogUtils.w(TAG, "TP file handler null exception.");
        }
        return ret;
    }

    /**
     * TP down position.
     */
    public void TPDown(int x, int y) {
        // 截获消息
        if (null != tpPositionListener) {
            if (tpPositionListener.TPDown(x, y)) {
                return;
            }
        }

        // 按下，进行区域判断，或者长按处理
        clearTPAction();
        mDownKeyCode = getCode(x, y);
        LogUtils.d(TAG, "TPDown mDownKeyCode = " + Define.Key.toString(mDownKeyCode) + ", x = " + x + ", y = " + y);
        if (FactoryDriver.Driver().getIsNoTouchKey()) {
            LogUtils.e("TPDown no touch key!");
            return;
        }
        if (null != mLongKeyTimerQueue) {
            Packet packet = new Packet();
            packet.putInt("x", x);
            packet.putInt("y", y);
            mLongKeyTimerQueue.add(600, packet, new BaseCallback() {
                @Override
                public void onCallback(int nId, Packet packet) {
                    //add by huwei 180416 for bug 11199 避免特殊情况下长按和短按一起触发
                    if (mbUpComming) {
                        return;
                    }
                    if (null != packet) {
                        final int x = packet.getInt("x", -1);
                        final int y = packet.getInt("y", -1);
                        mLastKeyCode = mDownKeyCode;
                        switch (mLastKeyCode) {
                            case Define.Key.KEY_NONE:
                                LogUtils.e(TAG, "What's up!!! TPDown onCallback mDownKeyCode = KEY_NONE, xy = " + x + ", " + y);
                                break;
                            case Define.Key.KEY_SHORT_POWEROFF:
                                isLastKeyEvent = true;
                                LogUtils.d(TAG, "TPDown onCallback mDownKeyCode = " + Define.Key.toString(mDownKeyCode) + ", x = " + x + ", y = " + y);
                                EventInputManager.NotifyKeyEvent(false, Define.KeyOrigin.OS, Define.Key.KEY_LONG_POWEROFF, null);
                                break;
                            case Define.Key.KEY_VOL_INC:
                            case Define.Key.KEY_VOL_DEC:
                                isLastKeyEvent = true;
                                LogUtils.d(TAG, "TPDown onCallback mDownKeyCode = " + Define.Key.toString(mDownKeyCode) + ", x = " + x + ", y = " + y);
                                /*-20180530-ydinggen-modifly-加快长按音量+、-的响应速度，由200ms改为150ms。康大提出-*/
                                mLastKeyTimerId = TimerUtils.setTimer(getMainContext(), 150, 150, new Timerable.TimerListener() {
                                    @Override
                                    public void onTimer(int timerId) {
                                        //add by huwei 180416 for bug 11199  避免异常情况下弹起后持续按键事件仍在触发
                                        if (!mbUpComming) {
                                            if (Define.Key.KEY_NONE != mLastKeyCode) {
                                                LogUtils.d(TAG, "TP onKeyEvent, keyOrigin = " + Define.KeyOrigin.toString(Define.KeyOrigin.OS) +
                                                        ", last key = " + Define.Key.toString(mLastKeyCode));
                                                EventInputManager.NotifyKeyEvent(false, Define.KeyOrigin.OS, mLastKeyCode, null);
                                            }
                                        } else {
                                            TimerUtils.killTimer(mLastKeyTimerId);
                                        }
                                    }
                                });
                                break;
                            default:
                                break;

                        }
                    }
                }
            });
            mLongKeyTimerQueue.start();
        }
    }

    /**
     * TP up position.
     */
    public void TPUp(int x, int y) {
        LogUtils.d(TAG, "TPUp mDownKeyCode = " + Define.Key.toString(mDownKeyCode) + ", x = " + x + ", y = " + y);

        if (Define.Key.KEY_NONE != mDownKeyCode) {
            if (isLastKeyEvent) {
                isLastKeyEvent = false;
            } else {
                if (FactoryDriver.Driver().getIsNoTouchKey()) {
                    LogUtils.e("TPUp no touch key!");
                } else {
                    LogUtils.d(TAG, "TP onKeyEvent, keyOrigin = " + Define.KeyOrigin.toString(Define.KeyOrigin.OS) +
                            ", key = " + Define.Key.toString(mDownKeyCode));

                    EventInputManager.NotifyKeyEvent(false, Define.KeyOrigin.OS, mDownKeyCode, null);
                }
            }
        }
        clearTPAction();

        // 截获消息
        if (null != tpPositionListener) {
            if (tpPositionListener.TPUp(x, y)) {
                return;
            }
        }

        // 弹起，进行区域判断，或者长按处理
    }

    /**
     * 清除TP长按动作
     */
    protected void clearTPLongAction() {
        mLastKeyCode = Define.Key.KEY_NONE;
        isLastKeyEvent = false;
        if (0 != mLastKeyTimerId) {
            TimerUtils.killTimer(mLastKeyTimerId);
            mLastKeyTimerId = 0;
        }
        if (null != mLongKeyTimerQueue) {
            mLongKeyTimerQueue.stop();
        }
    }

    /**
     * 清除TP动作
     */
    protected void clearTPAction() {
        mDownKeyCode = Define.Key.KEY_NONE;
        clearTPLongAction();
    }

    /**
     * 判断区域是否为空
     */
    protected boolean isEmpty(FourInteger fourInteger) {
        boolean ret = true;
        if (null != fourInteger) {
            if (fourInteger.getInteger3() > 0 && fourInteger.getInteger4() > 0) {
                ret = false;
            }
        }
        return ret;
    }

    /**
     * 判断点是否在区域内
     */
    protected boolean ptInRect(FourInteger fourInteger, int x, int y) {
        boolean ret = false;
        if (!isEmpty(fourInteger)) {
            if (x >= fourInteger.getInteger1() && x <= (fourInteger.getInteger1() + fourInteger.getInteger3())) {
                if (y >= fourInteger.getInteger2() && y <= (fourInteger.getInteger2() + fourInteger.getInteger4())) {
                    ret = true;
                }
            }
        }
        return ret;
    }

    /**
     * 求两个区域是否存在交集
     */
    protected boolean isIntersectRect(FourInteger rect1, FourInteger rect2) {
        boolean ret = false;

        if (null != rect1 && null != rect2) {
            Rect r1 = new Rect(rect1.getInteger1(), rect1.getInteger2(), rect1.getInteger1() + rect1.getInteger3(), rect1.getInteger2() + rect1.getInteger4());
            Rect r2 = new Rect(rect2.getInteger1(), rect2.getInteger2(), rect2.getInteger1() + rect2.getInteger3(), rect2.getInteger2() + rect2.getInteger4());
            ret = r1.intersect(r2);
        }
        return ret;
    }

    /**
     * 根据坐标获取按键值
     */
    protected int getCode(int x, int y) {
        int ret = Define.Key.KEY_NONE;
        final int index = getIndex(x, y);
        if (-1 != index) {
            Integer[] code = Model().getCodes().getVal();
            if (null != code) {
                if (index >= 0 && index < code.length) {
                    ret = code[index];
                }
            }
        }
        return ret;
    }

    /**
     * 根据坐标获取长按按键值
     */
    protected int getLongCode(int x, int y) {
        int ret = Define.Key.KEY_NONE;
        final int index = getIndex(x, y);
        if (-1 != index) {
            Integer[] code = Model().getLongCodes().getVal();
            if (null != code) {
                if (index >= 0 && index < code.length) {
                    ret = code[index];
                }
            }
        }
        return ret;
    }

    /**
     * 根据坐标获取持续按键值
     */
    protected int getLastCode(int x, int y) {
        int ret = Define.Key.KEY_NONE;
        final int index = getIndex(x, y);
        if (-1 != index) {
            Integer[] code = Model().getLastCodes().getVal();
            if (null != code) {
                if (index >= 0 && index < code.length) {
                    ret = code[index];
                }
            }
        }
        return ret;
    }

    /**
     * 根据坐标获取索引
     */
    protected int getIndex(int x, int y) {
        int ret = -1;
        FourInteger[] array = Model().getRects().getVal();
        if (null != array) {
            for (int i = 0; i < array.length; i++) {
                if (ptInRect(array[i], x, y)) {
                    ret = i;
                    break;
                }
            }
        }
        return ret;
    }

    /**
     * 获取传入区域与已保存的区域有冲突的下标
     */
    protected int getRectConflictIndex(FourInteger rect) {
        int ret = -1;
        if (null != rect) {
            FourInteger[] array = Model().getRects().getVal();
            if (null != array) {
                for (int i = 0; i < array.length; i++) {
                    if (isIntersectRect(rect, array[i])) {
                        ret = i;
                        break;
                    }
                }
            }
        }
        return ret;
    }

    /**
     * 根据字符串获取区域坐标
     */
    protected FourInteger getRect(String string) {
        FourInteger ret = null;
        if (!TextUtils.isEmpty(string)) {
            int x, y, w, h;
            int length = string.length();
            int index1 = string.indexOf("{");
            int index2 = string.indexOf(",");
            x = Integer.parseInt(string.substring(index1 + 1, index2).trim());
            string = string.substring(index2 + 1, length);
            length = string.length();
            index1 = 0;
            index2 = string.indexOf(",");
            y = Integer.parseInt(string.substring(index1, index2).trim());
            string = string.substring(index2 + 1, length);
            length = string.length();
            index1 = 0;
            index2 = string.indexOf(",");
            w = Integer.parseInt(string.substring(index1, index2).trim()) - x;
            string = string.substring(index2 + 1, length);
            length = string.length();
            index1 = 0;
            index2 = string.indexOf("}");
            h = Integer.parseInt(string.substring(index1, index2).trim()) - y;

            ret = new FourInteger(x, y, w, h);
        }
        return ret;
    }
}
