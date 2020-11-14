package com.wwc2.main.manager;

import android.content.Context;
import android.text.TextUtils;

import com.wwc2.common_interface.Define;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.driver.Driver;
import com.wwc2.corelib.driver.DriverManager;
import com.wwc2.corelib.logic.CoreLogic;
import com.wwc2.corelib.logic.LogicManager;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.logic.BaseLogic;
import com.wwc2.main.third_party.ThirdpartyDefine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * all of the module manager.
 *
 * @author wwc2
 * @date 2017/1/16
 */
public class ModuleManager {

    /**
     * TAG
     */
    private static final String TAG = "ModuleManager";

    /**
     * the main service context.
     */
    private static Context mContext = null;

    /**
     * get the main service context.
     */
    public static Context getContext() {
        return mContext;
    }

    /**
     * module manager prepare method.
     */
    public static void onPrepare() {
        // driver prepare.
        Map<String, Driver> drivers = DriverManager.getDrivers();
        if (null != drivers) {
            for (Map.Entry<String, Driver> entry : drivers.entrySet()) {
                Driver tempObject = entry.getValue();
                if (null != tempObject) {
                    tempObject.onPrepare();
                }
            }
        }

        // third, logic prepare.
        Map<String, CoreLogic> map = LogicManager.getLogicMap();
        if (null != map) {
            for (Map.Entry<String, CoreLogic> entry : map.entrySet()) {
                CoreLogic tempObject = entry.getValue();
                if (null != tempObject) {
                    tempObject.onPrepare();
                }
            }
        }
    }

    /**
     * module manager create method.
     */
    public static void onCreate(Packet packet) {
        // first, get the context.
        if (null != packet) {
            mContext = (Context) packet.getObject("context");
        }

        // driver create.
        onCreateDrivers(packet, null);

        // four, logic create.
        onCreateLogics(packet, null);
    }

    /**
     * module manager destroy method.
     */
    public static void onDestroy() {
        // first, logic destroy.
        onDestroyLogics(null);

        // third, driver destroy.
        onDestroyDrivers(null);
    }

    /**is create over.*/
    public static boolean isCreateOver() {
        boolean ret = true;
        Map<String, CoreLogic> map = LogicManager.getLogicMap();
        if (null != map) {
            for (Map.Entry<String, CoreLogic> entry : map.entrySet()) {
                final String name = entry.getKey();
                CoreLogic tempObject = entry.getValue();
                if (null != tempObject) {
                    ret &= tempObject.isCreateOver();
                    if (!ret) {
                        LogUtils.d(TAG, "isCreateOver##" + tempObject.toString() + " creating.");
                        break;
                    }
                }
            }
        }

        if (ret) {
            Map<String, Driver> drivers = DriverManager.getDrivers();
            if (null != drivers) {
                for (Map.Entry<String, Driver> entry : drivers.entrySet()) {
                    final String name = entry.getKey();
                    Driver tempObject = entry.getValue();
                    if (null != tempObject) {
                        ret &= tempObject.isCreateOver();
                        if (!ret) {
                            LogUtils.d(TAG, "isCreateOver##" + tempObject.toString() + " creating.");
                            break;
                        }
                    }
                }
            }
        }
        return ret;
    }

    /**is destroy over.*/
    public static boolean isDestroyOver() {
        boolean ret = true;
        Map<String, CoreLogic> map = LogicManager.getLogicMap();
        if (null != map) {
            for (Map.Entry<String, CoreLogic> entry : map.entrySet()) {
                final String name = entry.getKey();
                CoreLogic tempObject = entry.getValue();
                if (null != tempObject) {
                    ret &= tempObject.isDestroyOver();
                    if (!ret) {
                        LogUtils.d(TAG, "isDestroyOver##" + tempObject.toString() + " destroying.");
                        break;
                    }
                }
            }
        }

        if (ret) {
            Map<String, Driver> drivers = DriverManager.getDrivers();
            if (null != drivers) {
                for (Map.Entry<String, Driver> entry : drivers.entrySet()) {
                    final String name = entry.getKey();
                    Driver tempObject = entry.getValue();
                    if (null != tempObject) {
                        ret &= tempObject.isDestroyOver();
                        if (!ret) {
                            LogUtils.d(TAG, "isDestroyOver##" + tempObject.toString() + " destroying.");
                            break;
                        }
                    }
                }
            }
        }
        return ret;
    }

    /**first boot source.*/
    public static BaseLogic firstBoot() {
        BaseLogic ret = null;
        Map<String, CoreLogic> map = LogicManager.getLogicMap();
        if (null != map) {
            for (Map.Entry<String, CoreLogic> entry : map.entrySet()) {
                final String name = entry.getKey();
                CoreLogic tempObject = entry.getValue();
                if (null != tempObject) {
                    if (tempObject instanceof BaseLogic) {
                        BaseLogic logic = (BaseLogic) tempObject;
                        final int boot = logic.firstBoot();
                        if (Define.FirstBoot.isFirstBoot(boot)) {
                            ret = logic;
                            break;
                        } else if (Define.FirstBoot.isWait(boot)) {
                            ret = logic;
                            break;
                        }
                    }
                }
            }
        }
        return ret;
    }

    /**
     * module manager create logic.
     */
    public static void onCreateLogics(Packet packet, List<String> withoutHassles) {
        Map<String, CoreLogic> map = LogicManager.getLogicMap();
        if (null != map) {
            for (Map.Entry<String, CoreLogic> entry : map.entrySet()) {
                final String name = entry.getKey();
                CoreLogic tempObject = entry.getValue();
                if (!TextUtils.isEmpty(name) && null != tempObject) {
                    if (!inList(name, withoutHassles)) {
                        final String _name = tempObject.getClass().getName();
                        LogUtils.d(TAG, "onCreateLogics##start, name = " + _name);
                        tempObject.onCreate(packet);
                        LogUtils.d(TAG, "onCreateLogics##over, name = " + _name);
                    }
                }
            }
        }
    }

    /**
     * module manager destroy logic.
     */
    public static void onDestroyLogics(List<String> withoutHassles) {
        Map<String, CoreLogic> map = LogicManager.getLogicMap();
        if (null != map) {
            for (Map.Entry<String, CoreLogic> entry : map.entrySet()) {
                final String name = entry.getKey();
                CoreLogic tempObject = entry.getValue();
                if (!TextUtils.isEmpty(name) && null != tempObject) {
                    if (!inList(name, withoutHassles)) {
                        final String _name = tempObject.getClass().getName();
                        LogUtils.d(TAG, "onDestroyLogics##start, name = " + _name);
                        tempObject.onDestroy();
                        LogUtils.d(TAG, "onDestroyLogics##over, name = " + _name);
                    }
                }
            }
        }
    }

    /**
     * module manager create driver.
     */
    public static void onCreateDrivers(Packet packet, List<String> withoutHassles) {
        Map<String, Driver> drivers = DriverManager.getDrivers();
        if (null != drivers) {
            for (Map.Entry<String, Driver> entry : drivers.entrySet()) {
                final String name = entry.getKey();
                Driver tempObject = entry.getValue();
                if (!TextUtils.isEmpty(name) && null != tempObject) {
                    if (!inList(name, withoutHassles)) {
                        final String _name = tempObject.getClass().getName();
                        LogUtils.d(TAG, "onCreateDrivers##start, name = " + _name);
                        tempObject.onCreate(packet);
                        LogUtils.d(TAG, "onCreateDrivers##over, name = " + _name);
                    }
                }
            }
        }
    }

    /**
     * module manager destroy driver.
     */
    public static void onDestroyDrivers(List<String> withoutHassles) {
        Map<String, Driver> drivers = DriverManager.getDrivers();
        if (null != drivers) {
            for (Map.Entry<String, Driver> entry : drivers.entrySet()) {
                final String name = entry.getKey();
                Driver tempObject = entry.getValue();
                if (!TextUtils.isEmpty(name) && null != tempObject) {
                    if (!inList(name, withoutHassles)) {
                        final String _name = tempObject.getClass().getName();
                        LogUtils.d(TAG, "onDestroyDrivers##start, name = " + _name);
                        tempObject.onDestroy();
                        LogUtils.d(TAG, "onDestroyDrivers##over, name = " + _name);
                    }
                }
            }
        }
    }

    /**
     * module manager create driver.
     */
    public static void onCreateDriver(Packet packet, String driverName) {
        Map<String, Driver> drivers = DriverManager.getDrivers();
        if (null != drivers) {
            for (Map.Entry<String, Driver> entry : drivers.entrySet()) {
                final String name = entry.getKey();
                if (!TextUtils.isEmpty(name) && name.equals(driverName)) {
                    Driver tempObject = entry.getValue();
                    if (null != tempObject) {
                        final String _name = tempObject.getClass().getName();
                        LogUtils.d(TAG, "onCreateDrivers##start, name = " + _name);
                        tempObject.onCreate(packet);
                        LogUtils.d(TAG, "onCreateDrivers##over, name = " + _name);
                    }
                }
            }
        }
    }

    /**
     * module manager destroy driver.
     */
    public static void onDestroyDriver(String driverName) {
        Map<String, Driver> drivers = DriverManager.getDrivers();
        if (null != drivers) {
            for (Map.Entry<String, Driver> entry : drivers.entrySet()) {
                final String name = entry.getKey();
                if (!TextUtils.isEmpty(name) && name.equals(driverName)) {
                    Driver tempObject = entry.getValue();
                    if (null != tempObject) {
                        final String _name = tempObject.getClass().getName();
                        LogUtils.d(TAG, "onDestroyDrivers##start, name = " + _name);
                        tempObject.onDestroy();
                        LogUtils.d(TAG, "onDestroyDrivers##over, name = " + _name);
                    }
                }
            }
        }
    }

    /**
     * data is in the list.
     */
    private static boolean inList(String string, List<String> list) {
        boolean ret = false;
        if (!TextUtils.isEmpty(string) && null != list) {
            for (int i = 0; i < list.size(); i++) {
                if (string.equals(list.get(i))) {
                    ret = true;
                    break;
                }
            }
        }
        return ret;
    }

    /**
     * get logic object by class name.
     *
     * @param className the class name.
     * @return the logic object.
     */
    public static BaseLogic getLogicByName(String className) {
        BaseLogic ret = null;
        if (null != className) {
            Map<String, CoreLogic> map = LogicManager.getLogicMap();
            if (null != map) {
                for (Map.Entry<String, CoreLogic> entry : map.entrySet()) {
                    final String tempName = entry.getKey();
                    CoreLogic tempObject = entry.getValue();
                    if (className.equals(tempName)) {
                        if (tempObject instanceof BaseLogic) {
                            ret = (BaseLogic) tempObject;
                        }
                        break;
                    }
                }
            }
        }
        return ret;
    }

    /**
     * get logic by the source.
     *
     * @param source the source.
     * @return the logic object.
     */
    public static BaseLogic getLogicBySource(int source) {
        BaseLogic ret = null;
        if (Define.Source.SOURCE_NONE != source) {
            Map<String, CoreLogic> map = LogicManager.getLogicMap();
            if (null != map) {
                for (Map.Entry<String, CoreLogic> entry : map.entrySet()) {
                    CoreLogic tempObject = entry.getValue();
                    if (tempObject instanceof BaseLogic) {
                        BaseLogic logic = (BaseLogic) tempObject;
                        final int temp = logic.source();
                        if (source == temp) {
                            ret = logic;
                            break;
                        }
                    }
                }
            }
        }
        return ret;
    }

    /**
     * get logic object by packet name.
     *
     * @param packetName the packet name.
     * @return the logic object.
     */
    public static BaseLogic getLogicByPacketName(String packetName) {
        BaseLogic ret = null;
        if (null != packetName) {
            Map<String, CoreLogic> map = LogicManager.getLogicMap();
            if (null != map) {
                for (Map.Entry<String, CoreLogic> entry : map.entrySet()) {
                    CoreLogic tempObject = entry.getValue();
                    if (tempObject instanceof BaseLogic) {
                        BaseLogic logic = (BaseLogic) tempObject;
                        if (null != logic) {
                            // first, judgement the packet name.
                            if (null == ret) {
                                final String tempPacketName = logic.getAPKPacketName();
                                if (packetName.equals(tempPacketName)) {
                                    ret = logic;
                                }
                            }

                            // second, judgement the packet name list.
                            if (null == ret) {
                                List<String> tempPacketNames = logic.getAPKPacketList();
                                if (null != tempPacketNames) {
                                    for (int i = 0; i < tempPacketNames.size(); i++) {
                                        final String temp = tempPacketNames.get(i);
                                        if (packetName.equals(temp)) {
                                            ret = logic;
                                            break;
                                        }
                                    }
                                }
                            }

                            // third, break
                            if (null != ret) {
                                break;
                            }
                        }
                    }
                }
            }
        }

        // logic is null object, then third party.
        if (null == ret) {
            ret = getLogicByName(ThirdpartyDefine.MODULE);
        }
        return ret;
    }

    /**
     * get the power off logic list.
     */
    public static List<BaseLogic> getPoweroffLogic() {
        List<BaseLogic> ret = null;
        Map<String, CoreLogic> map = LogicManager.getLogicMap();
        if (null != map) {
            for (Map.Entry<String, CoreLogic> entry : map.entrySet()) {
                CoreLogic tempObject = entry.getValue();
                if (tempObject instanceof BaseLogic) {
                    BaseLogic logic = (BaseLogic) tempObject;
                    if (logic.isPoweroffSource()) {
                        if (null == ret) {
                            ret = new ArrayList<>();
                        }
                        ret.add(logic);
                    }
                }
            }
        }
        return ret;
    }

    /**
     * get the screen off logic list.
     */
    public static List<BaseLogic> getScreenoffLogic() {
        List<BaseLogic> ret = null;
        Map<String, CoreLogic> map = LogicManager.getLogicMap();
        if (null != map) {
            for (Map.Entry<String, CoreLogic> entry : map.entrySet()) {
                CoreLogic tempObject = entry.getValue();
                if (tempObject instanceof BaseLogic) {
                    BaseLogic logic = (BaseLogic) tempObject;
                    if (logic.isScreenoffSource()) {
                        if (null == ret) {
                            ret = new ArrayList<>();
                        }
                        ret.add(logic);
                    }
                }
            }
        }
        return ret;
    }

    /**
     * get the front logic object.
     */
    public static BaseLogic getFrontLogic() {
        BaseLogic ret = null;
        final int source = SourceManager.getCurSource();
        if (Define.Source.SOURCE_NONE != source) {
            ret = getLogicBySource(source);
        }
        return ret;
    }

    /**
     * get the old front logic object.
     */
    public static BaseLogic getOldFrontLogic() {
        BaseLogic ret = null;
        final int source = SourceManager.getOldSource();
        if (Define.Source.SOURCE_NONE != source) {
            ret = getLogicBySource(source);
        }
        return ret;
    }

    /**
     * get the background logic object.
     */
    public static BaseLogic getBackLogic() {
        BaseLogic ret = null;
        final int source = SourceManager.getCurBackSource();
        if (Define.Source.SOURCE_NONE != source) {
            ret = getLogicBySource(source);
        }
        return ret;
    }

    /**
     * get the old background logic object.
     */
    public static BaseLogic getOldBackLogic() {
        BaseLogic ret = null;
        final int source = SourceManager.getOldBackSource();
        if (Define.Source.SOURCE_NONE != source) {
            ret = getLogicBySource(source);
        }
        return ret;
    }

    /**
     * get the common logic object.
     */
    public static BaseLogic getCommonLogic() {
        BaseLogic ret = getLogicByName(Define.MODULE);
        return ret;
    }
}
