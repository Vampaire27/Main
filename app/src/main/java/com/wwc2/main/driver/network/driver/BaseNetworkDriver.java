package com.wwc2.main.driver.network.driver;

import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.driver.BaseDriver;
import com.wwc2.corelib.model.BaseModel;
import com.wwc2.main.driver.network.NetworkDriverable;

/**
 * the base network driver.
 *
 * @author wwc2
 * @date 2017/1/20
 */
public class BaseNetworkDriver extends BaseDriver implements NetworkDriverable {

    /**数据Model*/
    protected static class NetworkModel extends BaseModel {
        @Override
        public Packet getInfo() {
            return null;
        }
    }

    @Override
    public BaseModel newModel() {
        return new NetworkModel();
    }

    /**
     * get the model object.
     */
    protected NetworkModel Model() {
        NetworkModel ret = null;
        BaseModel model = getModel();
        if (model instanceof NetworkModel) {
            ret = (NetworkModel) model;
        }
        return ret;
    }

    @Override
    public void onCreate(Packet packet) {

    }

    @Override
    public void onDestroy() {

    }
}
