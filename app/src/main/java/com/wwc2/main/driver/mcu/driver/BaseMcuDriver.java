package com.wwc2.main.driver.mcu.driver;

import com.wwc2.common_interface.Define;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.driver.BaseDriver;
import com.wwc2.corelib.model.BaseModel;
import com.wwc2.main.driver.mcu.McuDriverable;

/**
 * the base mcu driver.
 *
 * @author wwc2
 * @date 2017/1/22
 */
public abstract class BaseMcuDriver extends BaseDriver implements McuDriverable {

    /**数据Model*/
    protected static class McuModel extends BaseModel {
        @Override
        public Packet getInfo() {
            return null;
        }
    }

    @Override
    public BaseModel newModel() {
        return new McuModel();
    }

    /**
     * get the model object.
     */
    protected McuModel Model() {
        McuModel ret = null;
        BaseModel model = getModel();
        if (model instanceof McuModel) {
            ret = (McuModel) model;
        }
        return ret;
    }

    @Override
    public void onCreate(Packet packet) {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int firstBoot() {
        return Define.FirstBoot.DEFAULT;
    }

    @Override
    public int getMemorySource() {
        return Define.Source.SOURCE_NONE;
    }

    @Override
    public int sendMcu(byte head, byte[] buf, int len) {
        return sendMcu(true,SERIAL_PRIORITY_NORMAL, true, head, buf, len);
    }

    @Override
    public int sendMcuImportant(byte head, byte[] buf, int len) {
        return sendMcu(true,SERIAL_PRIORITY_HIGH, false, head, buf, len);
    }

    @Override
    public int sendMcuNack(byte head, byte[] buf, int len) {
        return sendMcu(false,SERIAL_PRIORITY_NORMAL, false, head, buf, len);
    }
}
