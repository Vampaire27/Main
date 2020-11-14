package com.wwc2.main.driver.datetime.driver;

import com.wwc2.common_interface.Define;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.model.BaseModel;
import com.wwc2.corelib.model.MString;
import com.wwc2.main.driver.datetime.DateTimeDriverable;
import com.wwc2.main.driver.memory.BaseMemoryDriver;

/**
 * the base date time driver.
 *
 * @author wwc2
 * @date 2017/1/20
 */
public abstract class BaseDateTimeDriver extends BaseMemoryDriver implements DateTimeDriverable {

    /**数据Model*/
    protected static class DateTimeModel extends BaseModel {
        @Override
        public Packet getInfo() {
            Packet packet = new Packet();
            packet.putString(Define.Time.RESOURCE, getTimeSource().getVal());
            packet.putString(Define.Time.FORMAT, getTimeFormat().getVal());
            return packet;
        }

        public MString mTimeSource = new MString(this, "TimeSource", Define.Time.SRC_SMART);
        public MString getTimeSource() {return mTimeSource;}

        public MString mTimeFormat = new MString(this, "TimeFormat", Define.Time.FORMAT_24);
        public MString getTimeFormat() {return mTimeFormat;}
    }

    @Override
    public BaseModel newModel() {
        return new DateTimeModel();
    }

    /**
     * get the model object.
     */
    protected DateTimeModel Model() {
        DateTimeModel ret = null;
        BaseModel model = getModel();
        if (model instanceof DateTimeModel) {
            ret = (DateTimeModel) model;
        }
        return ret;
    }

    @Override
    public void onCreate(Packet packet) {
        super.onCreate(packet);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public String filePath() {
        return "DateTimeConfig.ini";
    }

    @Override
    public boolean writeData() {
        boolean ret = false;
//        if (null != mMemory) {
//            final String TimeSource = Model().getTimeSource().getVal();
//            if (!TextUtils.isEmpty(TimeSource)) {
//                mMemory.set("DATETIME", "TimeSource", TimeSource);
//            }
//
//            final String TimeFormat = Model().getTimeFormat().getVal();
//            if (!TextUtils.isEmpty(TimeFormat)) {
//                mMemory.set("DATETIME", "TimeFormat", TimeFormat);
//            }
//            ret = true;
//        }
        return ret;
    }

    @Override
    public boolean readData() {
        boolean ret = false;
//        if (null != mMemory) {
//            Object object = mMemory.get("DATETIME", "TimeSource");
//            if (null != object) {
//                String string = (String) object;
//                if (!TextUtils.isEmpty(string)) {
//                    Model().getTimeSource().setVal(string);
//                    ret = true;
//                }
//            }
//
//            object = mMemory.get("DATETIME", "TimeFormat");
//            if (null != object) {
//                String string = (String) object;
//                if (!TextUtils.isEmpty(string)) {
//                    Model().getTimeFormat().setVal(string);
//                    ret = true;
//                }
//            }
//        }
        return ret;
    }

    @Override
    public boolean setTimeSource(String src, int year, int month, int day, int hour, int minute, int second) {
        Model().getTimeSource().setVal(src);
        return false;
    }

    @Override
    public boolean setTimeFormat(String format) {
        Model().getTimeFormat().setVal(format);
        return false;
    }
}
