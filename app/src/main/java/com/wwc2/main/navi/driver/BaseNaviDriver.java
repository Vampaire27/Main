package com.wwc2.main.navi.driver;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;

import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.driver.DriverManager;
import com.wwc2.corelib.model.BaseModel;
import com.wwc2.corelib.model.MString;
import com.wwc2.corelib.model.MStringArray;
import com.wwc2.corelib.utils.apk.ApkUtils;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.driver.memory.BaseMemoryDriver;
import com.wwc2.main.driver.system.SystemDriver;
import com.wwc2.main.driver.system.SystemListener;
import com.wwc2.main.navi.NaviDefine;
import com.wwc2.main.navi.NaviListener;

import java.util.ArrayList;
import java.util.List;

/**
 * the base navigation driver.
 *
 * @author wwc2
 * @date 2017/1/9
 */
public class BaseNaviDriver extends BaseMemoryDriver implements NaviDriverable {

    /**
     * default navigation package name.
     */
    //public static final String DEFALUT_PKG_NAME = "com.autonavi.amapautolite";//车镜版
    public static final String DEFALUT_PKG_NAME = "com.autonavi.amapauto";//车机版

    /**
     * TAG
     */
    private static final String TAG = "BaseNaviDriver";

    /**
     * the all navigation package list.
     */
    private List<String> mAllNaviPackageList = null;

    /**
     * the remove package list.
     */
    private List<String> mRemovePackageList = new ArrayList<>();

    private List<String> mSaveNaviPackageList = null;

    /**
     * the model data.
     */
    protected static class NaviModel extends BaseModel {

        @Override
        public Packet getInfo() {
            Packet packet = new Packet();
            packet.putString(NaviDefine.SELECTION, mNaviPacketName.getVal());
            packet.putStringArray(NaviDefine.SOURCES, mNaviPacketList.getVal());
            packet.putStringArray(NaviDefine.REAL, mNaviRealPacketList.getVal());
            return packet;
        }

        /**
         * 导航包名
         */
        private MString mNaviPacketName = new MString(this, "NaviPacketNameListener", DEFALUT_PKG_NAME);

        public MString getNaviPacketName() {
            return mNaviPacketName;
        }

        /**
         * 导航过滤包名列表
         */
        private MStringArray mNaviPacketList = new MStringArray(this, "NaviPacketListListener", null);

        public MStringArray getNaviPacketList() {
            return mNaviPacketList;
        }

        /**
         * 确认后的导航包名列表
         */
        private MStringArray mNaviRealPacketList = new MStringArray(this, "NaviRealPacketListListener", null);

        public MStringArray getNaviRealPacketList() {
            return mNaviRealPacketList;
        }
    }

    /**
     * Navigation listener.
     */
    private NaviListener mNaviListener = new NaviListener() {
        @Override
        public void NaviPacketNameListener(String oldVal, String newVal) {
            LogUtils.d(TAG, "NaviPacketNameListener, oldVal = " + oldVal + ", newVal = " + newVal);
            if (null != mMemory) {
                mMemory.save();
            }
        }
    };


    @Override
    public BaseModel newModel() {
        return new NaviModel();
    }

    /**
     * get the model object.
     */
    protected NaviModel Model() {
        NaviModel ret = null;
        BaseModel model = getModel();
        if (model instanceof NaviModel) {
            ret = (NaviModel) model;
        }
        return ret;
    }

    /**
     * 系统监听器
     */
    private SystemListener mSystemListener = new SystemListener() {
        @Override
        public void AppInstallListener(String pkgName) {
            if (!TextUtils.isEmpty(pkgName)) {
                boolean add = false;
                if (isNaviApp(pkgName)) {
                    add = true;
                    for (int i = 0; i < mRemovePackageList.size(); i++) {
                        if (pkgName.equals(mRemovePackageList.get(i))) {
                            add = false;
                            break;
                        }
                    }
                }
                if (add) {
                    if (null != mAllNaviPackageList) {
                        mAllNaviPackageList.add(pkgName);
                    }
                    if (null != mSaveNaviPackageList) {
                        mSaveNaviPackageList.add(pkgName);
                        if (mMemory != null) {
                            mMemory.save();
                        }
                    }
                    Model().getNaviPacketList().addVal(-1, pkgName);
                }
            }
        }

        @Override
        public void AppUninstallListener(String pkgName) {
            if (!TextUtils.isEmpty(pkgName)) {
                String[] strings = Model().getNaviPacketList().getVal();
                if (null != strings) {
                    int length = strings.length;
                    if (length > 0) {
                        for (int i = 0; i < length; i++) {
                            if (pkgName.equals(strings[i])) {
                                if (null != mAllNaviPackageList) {
                                    mAllNaviPackageList.remove(pkgName);
                                }
                                if (null != mSaveNaviPackageList) {
                                    mSaveNaviPackageList.remove(pkgName);
                                    if (mMemory != null) {
                                        mMemory.save();
                                    }
                                }
                                Model().getNaviPacketList().delVal(i);
                                break;
                            }
                        }
                    }
                }
            }
        }
    };

    @Override
    public String filePath() {
        return "NaviPackageFilter.ini";
    }

    @Override
    public void onCreate(Packet packet) {
        LogUtils.d(TAG, "onCreate start.");

        getModel().bindListener(mNaviListener);

        super.onCreate(packet);

        DriverManager.getDriverByName(SystemDriver.DRIVER_NAME).getModel().bindListener(mSystemListener);

        // 添加确认后的导航包名列表
        String[] realArray = new String[]{
                "com.baidu.BaiduMap",//百度地图
                "com.baidu.naviauto",//百度地图车机版
                "com.autonavi.amapauto",//高德地图
                "com.autonavi.amapautolite",//高德地图车镜版
                "cld.navi.mainframe",//凯立德地图
                "com.cld.navimate",//凯立德导航伴侣
                "com.tencent.map",//腾讯地图
                "com.tencent.wecarnavi",//腾讯地图车机版
                "com.sogou.map.android.sogounav",//搜狗地图车机版
                "com.google.android.apps.maps",//谷歌地图
                "com.mxnavi.css",//美行地图
                "com.mapbar.android.mapbarmap",//图吧地图
                "dd.cld.navi.kaimap.h4160.mainframe",//凯立德货车版地图
                "cld.navi.kaimap.h4160.mainframe",//凯立德货车版地图
        };
        Model().getNaviRealPacketList().setVal(realArray);

        // 添加导航过滤包名列表
        if (null != mRemovePackageList) {
            mRemovePackageList.add("com.android.contacts");//#联系人
            mRemovePackageList.add("com.android.browser");//#浏览器
            mRemovePackageList.add("com.android.dialer");//#拍照
            mRemovePackageList.add("com.android.camera2");//#3D
            mRemovePackageList.add("com.android.gallery3d");//#图库
            mRemovePackageList.add("com.wwc2.mainui");//#MAIN UI
            mRemovePackageList.add("com.wwc2.systempermission");//#系统APK
            mRemovePackageList.add("com.unique.weatherwidget");//#天气
            mRemovePackageList.add("com.wwc2.weather");//#天气
            mRemovePackageList.add("com.wwc2.networks");//#后台
            mRemovePackageList.add("com.wwc2.main");
        }
        removeDuplicate(mRemovePackageList);

        if (null == mAllNaviPackageList) {
            // 获取应用列表导致执行时间长
            mAllNaviPackageList = new ArrayList<>();
            // 固定列表模式
            mAllNaviPackageList = getNavigationPackageList(realArray);
        }
        List<String> lists = mAllNaviPackageList;

        if (null != lists) {
            // 获取导航列表
            for (int i = 0; i < mRemovePackageList.size(); i++) {
                Object object = mRemovePackageList.get(i);
                if (null != object) {
                    lists.remove(object);
                }
            }

            // 设置导航列表
            String[] string = new String[lists.size()];
            lists.toArray(string);
            Model().getNaviPacketList().setVal(string);
        }
        LogUtils.d(TAG, "onCreate stop.");
    }

    @Override
    public void onDestroy() {
        LogUtils.d(TAG, "onDestroy start.");
        DriverManager.getDriverByName(SystemDriver.DRIVER_NAME).getModel().unbindListener(mSystemListener);
        getModel().unbindListener(mNaviListener);

        super.onDestroy();
        LogUtils.d(TAG, "onDestroy stop.");
    }

    @Override
    public boolean setNavigationPacketName(String pkgName) {
        boolean ret = false;
        if (ApkUtils.isAPKExist(getMainContext(), pkgName)) {
            Model().getNaviPacketName().setVal(pkgName);
            ret = true;
        }
        return ret;
    }

    @Override
    public String getNavigationPacketName() {
        return Model().getNaviPacketName().getVal();
    }

    @Override
    public boolean readData() {
        boolean ret = false;
        LogUtils.d(TAG, "readData start.");
        try {
            if (null != mMemory) {
                // 读取导航包名
                Object object = mMemory.get("PACKAGE", "NaviPacketName");
                if (null != object) {
                    String string = (String) object;
                    if (!TextUtils.isEmpty(string)) {
                        Model().getNaviPacketName().setVal(string);
                    }
                }

                // 读取移除包名列表
                if (null != mRemovePackageList) {
                    int i = 1;
                    while (true) {
                        object = mMemory.get("REMOVE", "package" + i);
                        String temp = null;
                        if (object instanceof String) {
                            temp = (String) object;
                            if (!TextUtils.isEmpty(temp)) {
                                mRemovePackageList.add(temp.trim());
                            }
                        }

                        if (TextUtils.isEmpty(temp)) {
                            break;
                        } else {
                            ret = true;
                            i++;
                        }
                    }
                }

                if (mSaveNaviPackageList == null) {
                    mSaveNaviPackageList = new ArrayList<>();
                } else {
                    mSaveNaviPackageList.clear();
                }
                int total = 0;
                object = mMemory.get("NAVI_PACKAGE", "Navi_Total");
                if (null != object) {
                    total = Integer.parseInt((String) object);
                    ret = true;
                }
                if (total > 0) {
                    for (int i = 0; i < total; i++) {
                        object = mMemory.get("NAVI_PACKAGE", "NaviPkgName" + i);
                        if (object instanceof String) {
                            String temp = (String) object;
                            if (!TextUtils.isEmpty(temp)) {
                                mSaveNaviPackageList.add(temp.trim());
                            }
                        }
                        ret = true;
                    }
//                    if (mSaveNaviPackageList.size() > 0) {
//                        for (int j = 0; j < mSaveNaviPackageList.size(); j++) {
//                            LogUtils.e("readData----" + mSaveNaviPackageList.get(j));
//                        }
//                    }
                }
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        LogUtils.d(TAG, "readData end.");
        return ret;
    }

    @Override
    public boolean writeData() {
        boolean ret = false;
        if (null != mMemory) {
            final String pkgName = Model().getNaviPacketName().getVal();
            if (!TextUtils.isEmpty(pkgName)) {
                mMemory.set("PACKAGE", "NaviPacketName", pkgName);
            }

            if (mSaveNaviPackageList != null) {
                mMemory.set("NAVI_PACKAGE", "Navi_Total", mSaveNaviPackageList.size());
                for (int i = 0; i < mSaveNaviPackageList.size(); i++) {
                    mMemory.set("NAVI_PACKAGE", "NaviPkgName" + i, mSaveNaviPackageList.get(i));
                }
            }
            ret = true;
        }
        return ret;
    }

    /**
     * get the pkg name.
     */
    public String getPkgName() {
        String ret = Model().getNaviPacketName().getVal();

        return ret;
    }

    /**
     * get the navigation package list.
     */
    private List<String> getNavigationPackageList(String[] packName) {
        List<String> ret = new ArrayList<>();
        for (int i = 0; i < packName.length; i++) {
            if (isNaviApp(packName[i])) {
                ret.add(packName[i]);
            }
        }

        if (mSaveNaviPackageList != null) {
            for (int i=0; i<mSaveNaviPackageList.size(); i++) {
                if (isNaviApp(mSaveNaviPackageList.get(i))) {
                    ret.add(mSaveNaviPackageList.get(i));
                }
            }
        }
        // 移除重复项
        removeDuplicate(ret);

        return ret;
    }

    /**
     * get the navigation package list.
     */
    private List<String> getNavigationPackageList() {
        List<String> ret = new ArrayList<>();
        Context context = getMainContext();
        if (null != context) {
            List<ResolveInfo> list = ApkUtils.getAppResolveInfo(context);
            if (null != list) {
                for (int i = 0; i < list.size(); i++) {
                    ResolveInfo info = list.get(i);
                    if (null != info) {
                        String packet = info.activityInfo.packageName;
                        if (isNaviApp(packet) &&
                                ((info.activityInfo.flags & ApplicationInfo.FLAG_SYSTEM) <= 0)) {
                            ret.add(packet);
                        }
                    }
                }
            }
        }

        // 移除重复项
        removeDuplicate(ret);

        return ret;
    }

    /**
     * 是否为导航APP
     */
    private boolean isNaviApp(String pkgName) {
        boolean ret = false;
        Context context = getMainContext();
        if (null != context) {
            if (!TextUtils.isEmpty(pkgName) && ApkUtils.isAPKExist(context, pkgName)) {
                try {
                    String[] permissionInfo = context.getPackageManager().getPackageInfo(pkgName, PackageManager.GET_PERMISSIONS).requestedPermissions;
                    if (permissionInfo != null) {
                        for (int j = 0; j < permissionInfo.length; j++) {
                            if (permissionInfo[j].equals("android.permission.ACCESS_FINE_LOCATION")) {
                                ret = true;
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return ret;
    }
}
