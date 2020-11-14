package com.wwc2.main.navi;

import com.wwc2.common_interface.Define;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.driver.BaseDriver;
import com.wwc2.corelib.utils.apk.ApkUtils;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.logic.BaseLogic;
import com.wwc2.main.navi.driver.BaseNaviDriver;
import com.wwc2.main.navi.driver.NaviDriverable;
import com.wwc2.main.provider.LogicProviderHelper;
import com.wwc2.navi_interface.NaviInterface;
import com.wwc2.navi_interface.Provider;

import java.util.ArrayList;
import java.util.List;

/**
 * the navigation logic.
 *
 * @author wwc2
 * @date 2017/1/17
 */
public class NaviLogic extends BaseLogic {

    /**TAG*/
    private static final String TAG = "NaviLogic";

    /**
     * ContentProvider.
     */
    static {
        LogicProviderHelper.Provider(Provider.NAVI_PKG_NAME(), BaseNaviDriver.DEFALUT_PKG_NAME);
    }

    /**navigation listener.*/
    private NaviListener mNaviListener = new NaviListener() {
        @Override
        public void NaviPacketNameListener(String oldVal, String newVal) {
            LogUtils.d(TAG, "NaviPacketNameListener, oldVal = " + oldVal + ", newVal = " + newVal);
            LogicProviderHelper.getInstance().update(com.wwc2.navi_interface.Provider.NAVI_PKG_NAME(), newVal);
        }
    };

    @Override
    public String getTypeName() {
        return "Navi";
    }

    @Override
    public boolean isStackSource() {
        return false;//导航不作单独的源，不压入源列表中。修改：bug7944 2017-06-01
    }

    @Override
    public int source() {
        return Define.Source.SOURCE_NAVI;
    }

    @Override
    public List<String> getAPKPacketList() {
        List<String> ret = new ArrayList<>();
        ret.add(getInfo().getString(NaviDefine.SELECTION));
        return ret;
    }

    @Override
    public boolean runApk() {
        ApkUtils.runApk(getMainContext(), getNaviPackageName(), null, false);
        return true;
    }

    private String getNaviPackageName() {
        Packet packet = getInfo();
        if (packet != null) {
            return packet.getString(NaviDefine.SELECTION);
        }
        return null;
    }

    @Override
    public BaseDriver newDriver() {
        return new BaseNaviDriver();
    }

    /**
     * the driver interface.
     */
    protected NaviDriverable Driver() {
        NaviDriverable ret = null;
        BaseDriver driver = getDriver();
        if (driver instanceof NaviDriverable) {
            ret = (NaviDriverable) driver;
        }
        return ret;
    }

    @Override
    public void onCreate(Packet packet) {
        getModel().bindListener(mNaviListener);

        super.onCreate(packet);

        BaseDriver driver = getDriver();
        if (null != driver) {
            Packet packet1 = new Packet();
            packet1.putObject("context", getMainContext());
            driver.onCreate(packet1);
        }

        LogicProviderHelper.getInstance().update(com.wwc2.navi_interface.Provider.NAVI_PKG_NAME(), getInfo().getString(NaviDefine.SELECTION));
        getMainContext().getContentResolver().notifyChange(com.wwc2.common_interface.Provider.ProviderColumns.CONTENT_URI, null);
    }

    @Override
    public void onDestroy() {
        getModel().unbindListener(mNaviListener);

        BaseDriver driver = getDriver();
        if (null != driver) {
            driver.onDestroy();
        }

        super.onDestroy();
    }

    @Override
    public Packet dispatch(int nId, Packet packet) {
        Packet ret = super.dispatch(nId, packet);
        switch (nId) {
            case NaviInterface.APK_TO_MAIN.SET_NAVI_PACKET_NAME:
                if (null != packet) {
                    String name = packet.getString("name");
                    Driver().setNavigationPacketName(name);
                }
                break;
            default:
                break;
        }
        return ret;
    }
}
