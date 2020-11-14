package com.wwc2.main.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.wwc2.canbus_interface.CanBusDefine;
import com.wwc2.common_interface.Define;
import com.wwc2.common_interface.Provider.ProviderColumns;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.accoff.driver.WakeupManager;
import com.wwc2.main.canbus.CanBusLogic;
import com.wwc2.main.driver.common.CommonDriver;
import com.wwc2.main.driver.factory.FactoryDriver;
import com.wwc2.main.driver.info.InfoDriver;
import com.wwc2.main.driver.info.InfoDriverable;
import com.wwc2.main.driver.version.VersionDriver;
import com.wwc2.main.driver.version.VersionDriverable;
import com.wwc2.main.driver.volume.VolumeDriver;
import com.wwc2.main.logic.BaseLogic;
import com.wwc2.main.manager.EventInputManager;
import com.wwc2.main.manager.ModuleManager;
import com.wwc2.main.manager.SourceManager;
import com.wwc2.main.manager.TBoxDataManager;
import com.wwc2.main.manager.VolumeManager;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The ContentProvider of the logic.
 *
 * @author wwc2
 * @date 2017/1/24
 */
public class LogicProvider extends ContentProvider {
	private static final String TAG = "LogicProvider";

    private static final String TABLE_NAME = "logic";

    public static final String VERSION_OS           = "version_os";
    public static final String VERSION_KERNEL       = "version_kernel";
    public static final String VERSION_APP          = "version_app";
    public static final String VERSION_BLUE         = "version_blue";
    public static final String VERSION_MCU          = "version_mcu";
    public static final String VERSION_CANBUS       = "version_canbus";
    public static final String INFO_IMEI            = "info_imei";
    public static final String INFO_WIFI_MAC        = "info_wifi_mac";
    public static final String INFO_SERIAL          = "info_serial";
    public static final String VOLUME_VALUE         = "volume_value";
    public static final String VOLUME_MAX           = "volume_max";
    public static final String REAL_ACC_STATUS      = "real_acc_status";
    public static final String ACC_STATUS           = "acc_status";
    public static final String CLIENT_ID            = "client_id";
    public static final String CAMERA_STATUS        = "camera_status";
    public static final String CUR_SOURCE           = "cur_source";
    public static final String CUR_CANSERIES        = "cur_canseries";
    public static final String CUR_CAN_SWITCH       = "cur_can_switch";
    public static final String CUR_CAN_CONNECT      = "cur_can_connect";
    public static final String CAN_SWITCH           = "can_switch";
    public static final String UI_STYLE             = "ui_style";
    public static final String UI_SHOW_SWITCH       = "ui_show_switch";
    public static final String PANORAMIC_SWITCH     = "panoramic_switch";
    public static final String GPRS_APK_NAME        = "gprs_apk_name";
    public static final String USER_KEY             = "user_key";
    public static final String AUTO_LAND_PORT       = "auto_land_port";
    public static final String CLOSE_SCREEN         = "close_screen";
    public static final String SUPPORT_POWEROFF     = "support_poweroff";
    public static final String SUPPORT_RIGHTCAMERA  = "support_rightcamera";
    public static final String SUPPORT_FRONTCAMERA  = "support_frontcamera";
    public static final String SUPPORT_ANGLE        =  "support_angle";
    public static final String FACTORY_PASSWORD     = "factory_password";
    public static final String DVR_SUPPORT          = "dvr_support";
    public static final String CUR_VOLTAGE          = "cur_voltage";
    public static final String TBOX_ICCID           = "tbox_iccid";
    public static final String TBOX_IMEI            = "tbox_imei";
    public static final String TURN_LIGHT           = "turn_light";
    public static final String WAKUP_POWEROFF       = "wakeup_poweroff";
    public static final String VOICE_ENABLE        =  "voice_enable";

    private static ConcurrentHashMap<String, String> sLogicDataMap = new ConcurrentHashMap<String, String>();
    
    public static void notifyChangeMap() {
    	sLogicDataMap.clear();
    	
    	sLogicDataMap.put(ProviderColumns._ID, ProviderColumns._ID);
    	sLogicDataMap.put(ProviderColumns.IDENTIFICATION, ProviderColumns.IDENTIFICATION);
    	
    	for (Entry<String, String> entry: Config.mElementMap.entrySet()) {
    		String key = (String)entry.getKey();
    		if (null != key) {
				sLogicDataMap.put(key, key);
			}
    	}
    }
    
    /**
     * This class helps open, create, and upgrade the database file.
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, Config.DATABASE_NAME, null, Config.DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
        	String create = "CREATE TABLE " + TABLE_NAME + " ("
                    + ProviderColumns._ID + " INTEGER PRIMARY KEY,";
        	
        	String element = "";
        	for (Entry<String, String> entry: Config.mElementMap.entrySet()) {
        		String key = (String)entry.getKey();
        		if (null != key) {
        			element = element + key + " " + "TEXT" + ",";
    			}
        	}
        	
        	String string = create + element
        			+ ProviderColumns.IDENTIFICATION + " TEXT"
        			+ ");";
        	
            db.execSQL(string);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS notes");
            onCreate(db);
        }
    }

    private DatabaseHelper mOpenHelper;

    @Override
    public boolean onCreate() {
        mOpenHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TABLE_NAME);
        qb.setProjectionMap(sLogicDataMap);

        // If no sort order is specified use the default
        String orderBy;
        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = ProviderColumns.DEFAULT_SORT_ORDER;
        } else {
            orderBy = sortOrder;
        }

        // Get the database and run the query
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);

        // Tell the cursor what uri to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        
        return c;
    }

    @Override
    public String getType(Uri uri) {
        String ret = "Unknown";//ProviderColumns.CONTENT_TYPE;
//        LogUtils.d(TAG, "getType uri = "+uri);
        String path = uri.getEncodedPath();
//        LogUtils.d(TAG, "getType path = "+path);
        String[] param = path.split("/");
        if (null == param || param.length < 1) {
            return ret;
        }
//        for (int i=0; i<param.length; i++) {
//            LogUtils.d(TAG, "param["+i+"] = "+param[i]);
//        }
        if (param.length <= 1) return ret;
        LogUtils.d(TAG, "param["+1+"] = "+param[1]);
//        Packet version = DriverManager.getDriverByName(VersionDriver.DRIVER_NAME).getInfo();
        try {
            if (param[1].equals(VERSION_OS)) {
                VersionDriverable driverable = VersionDriver.Driver();
                if (driverable != null) {
                    ret = driverable.getSystemVersion();
                }
            } else if (param[1].equals(VERSION_KERNEL)) {
                VersionDriverable driverable = VersionDriver.Driver();
                if (driverable != null) {
                    ret = driverable.getKernelVersion();
                }
            } else if (param[1].equals(VERSION_APP)) {
//            if (version != null) {
//                ret = version.getString(SettingsDefine.Version.CLINET) + "_V" + version.getString(SettingsDefine.Version.APP);
//            }
                VersionDriverable driverable = VersionDriver.Driver();
                if (driverable != null) {
                    ret = driverable.getAPPVersion();
                }
            } else if (param[1].equals(VERSION_BLUE)) {
//            if (version != null) {
//                ret = version.getString(SettingsDefine.Version.APP_BLUETOOTH);
//                int temp = ret.indexOf("_");
//                if (temp > 0) {
//                    String blue = ret.substring((temp + 1), ret.length());
//                    ret = blue;
//                }
//            }
                VersionDriverable driverable = VersionDriver.Driver();
                if (driverable != null) {
                    ret = driverable.getBluetoothVersion();
                }
            } else if (param[1].equals(VERSION_MCU)) {
                VersionDriverable driverable = VersionDriver.Driver();
                if (driverable != null) {
                    ret = driverable.getMcuVersion();
                }
            } else if (param[1].equals(VERSION_CANBUS)) {
                VersionDriverable driverable = VersionDriver.Driver();
                if (driverable != null) {
                    ret = driverable.getCanBusVersion();
                }
            } else if (param[1].equals(INFO_IMEI)) {
                InfoDriverable infoDriverable = InfoDriver.Driver();
                if (null != infoDriverable) {
                    ret = infoDriverable.getIMEI(getContext());
                }
            } else if (param[1].equals(INFO_WIFI_MAC)) {
                InfoDriverable infoDriverable = InfoDriver.Driver();
                if (null != infoDriverable) {
                    ret = infoDriverable.getWifiIpAddresses(getContext());
                }
            } else if (param[1].equals(INFO_SERIAL)) {
                InfoDriverable infoDriverable = InfoDriver.Driver();
                if (null != infoDriverable) {
                    ret = infoDriverable.getSerialNumber(getContext());
                }
            } else if (param[1].equals(VOLUME_VALUE)) {
                ret = String.valueOf(VolumeDriver.Driver().getVolumeValue());
            } else if (param[1].equals(VOLUME_MAX)) {
                ret = String.valueOf(VolumeManager.getMax());
            } else if (param[1].equals(ACC_STATUS)) {
                ret = String.valueOf(EventInputManager.getAcc());
            } else if (param[1].equals(REAL_ACC_STATUS)) {
                ret = String.valueOf(EventInputManager.getRealAcc());
            } else if (param[1].equals(CLIENT_ID)) {
                if (VersionDriver.Driver() != null) {
                    ret = VersionDriver.Driver().getClientProject();
                }
            } else if (param[1].equals(CAMERA_STATUS)) {
                ret = String.valueOf(EventInputManager.getCamera());
            } else if (param[1].equals(CUR_SOURCE)) {
                ret = Define.Source.toString(SourceManager.getCurSource());
            } else if (param[1].equals(CUR_CANSERIES) ||
                    param[1].equals(CUR_CAN_SWITCH) ||
                    param[1].equals(CUR_CAN_CONNECT) ||
                    param[1].equals(CAN_SWITCH)) {
            /*-begin-20180426-ydinggen-add-修改Can信息获取方式，不写数据库，防止数据库异常问题-*/
                int value = 0;
                BaseLogic mLogic = ModuleManager.getLogicByName(CanBusDefine.MODULE);
                if (mLogic instanceof CanBusLogic) {
                    CanBusLogic canBusLogic = (CanBusLogic) mLogic;
                    if (canBusLogic != null) {
                        if (param[1].equals(CUR_CANSERIES)) {
                            value = canBusLogic.getCanSeries();
                        } else if (param[1].equals(CUR_CAN_SWITCH)) {
                            value = canBusLogic.getCanSwitchStatus();
                        } else if (param[1].equals(CUR_CAN_CONNECT)) {
                            value = canBusLogic.getCanBusConnected();
                        } else if (param[1].equals(CAN_SWITCH)) {
                            return String.valueOf(canBusLogic.getCanBusSwitch());
                        }
                    }
                }
                ret = String.valueOf(value);
            /*-end-20180426-ydinggen-add-修改Can信息获取方式，不写数据库，防止数据库异常问题-*/
            } else if (param[1].equals(UI_STYLE)) {
                ret = String.valueOf(FactoryDriver.Driver().getUiStyle());
            } else if (param[1].equals(UI_SHOW_SWITCH)) {
                ret = String.valueOf(FactoryDriver.Driver().getUiStyleShow());
            } else if (param[1].equals(PANORAMIC_SWITCH)) {
                ret = String.valueOf(FactoryDriver.Driver().getPanoramicSwitch());
            } else if (param[1].equals(GPRS_APK_NAME)) {
                ret = CommonDriver.Driver().getGprsApkName();
            } else if (param[1].equals(AUTO_LAND_PORT)) {
                ret = String.valueOf(CommonDriver.Driver().getAutoLandPort());
            } else if (param[1].equals(CLOSE_SCREEN)) {
                ret = String.valueOf(FactoryDriver.Driver().getCloseScreen());
            } else if (param[1].equals(SUPPORT_POWEROFF)) {
                ret = String.valueOf(FactoryDriver.Driver().getSupportPoweroff());
            } else if (param[1].equals(SUPPORT_RIGHTCAMERA)) {
                ret = String.valueOf(FactoryDriver.Driver().getSupportRightCamera());
            } else if (param[1].equals(SUPPORT_FRONTCAMERA)) {
                ret = String.valueOf(FactoryDriver.Driver().getSupportFrontCamera());
            } else if (param[1].equals(FACTORY_PASSWORD)) {
                ret = FactoryDriver.Driver().getFactoryPassword();
            } else if (param[1].equals(DVR_SUPPORT)) {
                ret = String.valueOf(FactoryDriver.Driver().getDvrEnable());
            } else if (param[1].equals(CUR_VOLTAGE)) {
                ret = String.valueOf(WakeupManager.getInstance().getCurVoltage());
            } else if (param[1].equals(TBOX_ICCID)) {
                ret = TBoxDataManager.getTBoxManager().getIccidImei(1);
            } else if (param[1].equals(TBOX_IMEI)) {
                ret = TBoxDataManager.getTBoxManager().getIccidImei(2);
            } else if (param[1].equals(TURN_LIGHT)) {
                ret = String.valueOf(EventInputManager.getTurnLight());
            } else if (param[1].equals(WAKUP_POWEROFF)) {
                ret = String.valueOf(FactoryDriver.Driver().getWakeupPower());
            } else if (param[1].equals(SUPPORT_ANGLE)) {
                ret = String.valueOf(FactoryDriver.Driver().getSupportAngle());
            } else if(param[1].equals(VOICE_ENABLE)) {
                ret = String.valueOf(FactoryDriver.Driver().getVoiceEnable());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }
        
        if (values.containsKey(ProviderColumns.IDENTIFICATION) == false) {
            values.put(ProviderColumns.IDENTIFICATION, ProviderColumns.IDENTIFICATION);
        }

        final Cursor cursor = query(uri, null, null, null, null);
        if (null != cursor) {
            final int count = cursor.getCount();

            cursor.moveToFirst();
            for (Entry<String, String> entry: Config.mElementMap.entrySet()) {
                String key = (String)entry.getKey();
                String defaultValue = (String)entry.getValue();
                if (null != key && null != defaultValue) {
                    if (values.containsKey(key) == false) {
                        if (count > 0) {
                            values.put(key, cursor.getString(cursor.getColumnIndex(key)));
                        } else {
                            values.put(key, defaultValue);
                        }
                    }
                }
            }

            cursor.close();
        }

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long rowId = db.insert(TABLE_NAME, ProviderColumns.IDENTIFICATION, values);
        if (rowId > 0) {
            Uri noteUri = ContentUris.withAppendedId(ProviderColumns.CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(noteUri, null);
            return noteUri;
        }

        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count = db.delete(TABLE_NAME, where, whereArgs);

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {

        String path = uri.getEncodedPath();
        String[] param = path.split("/");
        if (param.length > 1) {
            if (param[1].equals(VOLUME_VALUE)) {
                Set<String> set = values.keySet();
                Iterator<String> iter = set.iterator();
                while (iter.hasNext()) {
                    String key = iter.next();
                    if (key.equals(VOLUME_VALUE)) {
                        int iValue = values.getAsInteger(key);
                        if (iValue != -1) {
                            LogUtils.d(TAG, "update---key=" + key + ", value=" + iValue);
                            VolumeDriver.Driver().set(iValue);
                        }
                    }
                }
                return 0;
            } else if (param[1].equals(USER_KEY)) {
                Set<String> set = values.keySet();
                Iterator<String> iter = set.iterator();
                while (iter.hasNext()) {
                    String key = iter.next();
                    if (key.equals(USER_KEY)) {
                        int userKey = values.getAsInteger(key);
                        if (userKey != Define.Key.KEY_NONE) {
                            LogUtils.d(TAG, "update---key=" + key + ", value=" + userKey);
                            EventInputManager.NotifyKeyEvent(false, Define.KeyOrigin.SOFTWARE, userKey, null);
                        }
                    }
                }
                return 0;
            }
        }

        int count = 0;
        try {
            SQLiteDatabase db = mOpenHelper.getWritableDatabase();
            count = db.update(TABLE_NAME, values, where, whereArgs);
        } catch (Exception e) {
            e.printStackTrace();
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}
