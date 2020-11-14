package com.wwc2.main.standby;

import com.wwc2.common_interface.Define;
import com.wwc2.corelib.db.Packet;
import com.wwc2.main.logic.BaseLogic;
import com.wwc2.main.manager.SourceManager;
import com.wwc2.standby_interface.StandbyDefine;

/**
 * the standby logic.
 *
 * @author wwc2
 * @date 2017/1/11
 */
public class StandbyLogic extends BaseLogic {

    @Override
    public String getTypeName() {
        return "Standby";
    }

    @Override
    public String getMessageType() {
        return StandbyDefine.MODULE;
    }

    @Override
    public String getAPKPacketName() {
        return "com.wwc2.standby";
    }

    @Override
    public int source() {
        return Define.Source.SOURCE_STANDBY;
    }

    @Override
    public boolean onKeyEvent(int keyOrigin, int key, Packet packet) {
        boolean ret = false;
        switch (key) {
            case Define.Key.KEY_STANDBY:
                ret = true;
                SourceManager.onPopSourceNoPoweroff(source());
                break;
            default:
                break;
        }
        return ret;
    }
}
