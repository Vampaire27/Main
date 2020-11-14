package com.wwc2.main.manager;

import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;

import com.wwc2.common_interface.Define;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.driver.mcu.driver.STM32MCUDriver;
import com.wwc2.main.driver.version.VersionDriver;
import com.wwc2.main.driver.volume.VolumeDriver;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by swd1 on 17-12-14.
 */

public class CPUThermalManager {
    /**
     * thermal_zone1 is cpu temp file.
     */
    //zhongyang.hu modify CPU TEMP  O form thermal_zone1 to thermal_zone0
    //zhongyang 6737 /sys/class/thermal/thermal_zone0/temp
    //zhongyang 6737 /sys/class/thermal/thermal_zone1/temp

    static Context mContext =null;

     public static String mPlatfromID =SystemProperties.get("ro.board.platform", "");

    private static String CPUTEMP = "/sys/class/thermal/thermal_zone1/temp";

    private static String CPUTEMP_MT6580 = "/sys/class/thermal/thermal_zone0/temp";

    private static int TEMP_THERMAL_L_FLOOR =100000;

    private static int TEMP_THERMAL_L_CEILING=103000;

    private static int TEMP_THERMAL_H_FLOOR =120000;

    private static int TEMP_THERMAL_H_CEILING=125000;

    private static int TEMP_THERMAL_H_DIE=    130000;

    private static int TEMP_THERMAL_LOW_VOL = 122000;
    private static int VOL_MAX  = 25;


    private static int TEMP_THERMAL_H_FLOOR_6739 =130000;

    private static int TEMP_THERMAL_H_CEILING_6739 =135000;

    private static int TEMP_THERMAL_H_DIE_6739 =    136000;


//    private static int TEMP_THERMAL_L_FLOOR =60000;
//
//    private static int TEMP_THERMAL_L_CEILING=63000;
//
//    private static int TEMP_THERMAL_H_FLOOR =70000;
//
//    private static int TEMP_THERMAL_H_CEILING=80000;


    public static final int TEMP_STATUS_NORMAL = 1;

    public static final int TEMP_STATUS_HIGHT = 2;

    public static final int TEMP_STATUS_OVER_HEAT = 3;

    public static final int TEMP_STATUS_OVER_DIE = 4;


    private static int currentTemp =50000;

    private static int currentStatus = TEMP_STATUS_NORMAL;

    private static int oldStatus = TEMP_STATUS_NORMAL;

    private static int timeCount =0;

    /**
     * need to stop some top app,for reduce temp.
     */
    public static boolean isCPUOverHeatL(){
      return (currentStatus!= TEMP_STATUS_NORMAL);
    }

    /**
     * need to stop some top app, need audio vido cannot open;
     */
    public static boolean isCPUOverHeatH(){
        return (currentStatus== TEMP_STATUS_OVER_HEAT);
    }

    private static boolean isOSSystem6() {
        boolean ret = false;
        String version = VersionDriver.Driver().getSystemVersion();
        if (version.contains("Android 6.0")) {
            ret = true;
        }
        return ret;
    }




    public static void updateCPUState6739(){

        File b;
        b = new File(CPUTEMP_MT6580);

        String temp = new String();
        if (b.exists())
        {
            try {
                FileInputStream fs = new FileInputStream(b);
                DataInputStream ds = new DataInputStream(fs);

                temp=ds.readLine();
                ds.close();
                fs.close();
            }
            catch (IOException ex) {
                Log.d("CPUThermalManager ", " updateCPUState IOE\n");
                return;
            }


            currentTemp=Integer.parseInt(temp);

            oldStatus = currentStatus;

            if(currentStatus == TEMP_STATUS_NORMAL){
                timeCount=0;
                if(currentTemp > TEMP_THERMAL_L_CEILING) {
                    currentStatus = TEMP_STATUS_HIGHT;
                }
            }else if(currentStatus == TEMP_STATUS_HIGHT){
                timeCount=0;
                if(currentTemp > TEMP_THERMAL_H_CEILING_6739){
                    currentStatus = TEMP_STATUS_OVER_HEAT;
                }else if( currentTemp < TEMP_THERMAL_L_FLOOR){
                    currentStatus = TEMP_STATUS_NORMAL;
                }
            }else{
                if(currentTemp < TEMP_THERMAL_H_FLOOR_6739){
                    currentStatus = TEMP_STATUS_HIGHT;
                }else if(currentTemp < TEMP_THERMAL_H_DIE_6739){
                    currentStatus = TEMP_STATUS_OVER_HEAT;
                }

                if(currentTemp > TEMP_THERMAL_H_DIE_6739){
                    if(timeCount ++ > 10 ){
                        currentStatus = TEMP_STATUS_OVER_DIE;
                    }
                }else{
                    timeCount=0;
                }
            }

            if (currentTemp >= TEMP_THERMAL_LOW_VOL && VolumeDriver.Driver().getVolumeValue() > VOL_MAX) {
                VolumeDriver.Driver().set(VOL_MAX);
            }

            Log.d("CPUThermalManager", "CPU TEMP. = "+ temp + " currentStatus= " +currentStatus +" old status =" + oldStatus );
        }
    }


    public static void updateCPUState(){
        if("mt6739".equals(mPlatfromID)){
            updateCPUState6739();
        }else{
            updateCPUStateAll();
        }

    }


    public static void updateCPUStateAll(){

        File b;
        if("mt6737".equals(mPlatfromID) && isOSSystem6()) {//6737 6.0的才是读此节点。
             b = new File(CPUTEMP);
        }else{
             b = new File(CPUTEMP_MT6580);
        }

        String temp = new String();
        if (b.exists())
        {
            try {
                FileInputStream fs = new FileInputStream(b);
                DataInputStream ds = new DataInputStream(fs);

                temp=ds.readLine();
                ds.close();
                fs.close();
            }
            catch (IOException ex) {
                Log.d("CPUThermalManager ", " updateCPUState IOE\n");
                return;
            }


            currentTemp=Integer.parseInt(temp);

            oldStatus = currentStatus;

           if(currentStatus == TEMP_STATUS_NORMAL){
               timeCount=0;
               if(currentTemp > TEMP_THERMAL_L_CEILING) {
                   currentStatus = TEMP_STATUS_HIGHT;
               }
           }else if(currentStatus == TEMP_STATUS_HIGHT){
               timeCount=0;
               if(currentTemp > TEMP_THERMAL_H_CEILING){
                   currentStatus = TEMP_STATUS_OVER_HEAT;
               }else if( currentTemp < TEMP_THERMAL_L_FLOOR){
                   currentStatus = TEMP_STATUS_NORMAL;
               }
           }else{
               if(currentTemp < TEMP_THERMAL_H_FLOOR){
                   currentStatus = TEMP_STATUS_HIGHT;
               }else if(currentTemp < TEMP_THERMAL_H_DIE){
                   currentStatus = TEMP_STATUS_OVER_HEAT;
               }

               if(currentTemp > TEMP_THERMAL_H_DIE){
                   if(timeCount ++ > 10 ){
                       currentStatus = TEMP_STATUS_OVER_DIE;
                   }
               }else{
                   timeCount=0;
               }
           }

           if (currentTemp >= TEMP_THERMAL_LOW_VOL && VolumeDriver.Driver().getVolumeValue() > VOL_MAX) {
               VolumeDriver.Driver().set(VOL_MAX);
           }

            Log.d("CPUThermalManager", "CPU TEMP. = "+ temp + " currentStatus= " +currentStatus +" old status =" + oldStatus );
        }
    }


    /* *
   * Helper to enables or disables airplane mode. ,  also broadcasts an intent
   * indicating that the mode has changed.
   *
   * Note: Needs the following permission:
   *  android.permission.WRITE_SETTINGS
   * @param enable true if airplane mode should be ON, false if it should be OFF
   */
    protected static void wwc2setAirplaneModeOn(Context mContext, boolean enable)   {
        int state = enable ? 1 : 0;

        Log.d("CPUThermalManager", "wwc2setAirplaneModeOn true " +enable );

        // Change the system setting
        Settings.Global.putInt(mContext.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON,
                state);

        Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        intent.putExtra("state", enable);
        mContext.sendBroadcast(intent);
    }


    public static void SetThermalPolicy(Context mCxt) {
        mContext = mCxt;
        if(oldStatus!=currentStatus && mCxt!= null) {
            if (oldStatus < TEMP_STATUS_OVER_HEAT && currentStatus == TEMP_STATUS_OVER_HEAT  ) {
                wwc2setAirplaneModeOn(mCxt, true);
            }else if(oldStatus == TEMP_STATUS_OVER_HEAT &&  currentStatus < TEMP_STATUS_OVER_HEAT){
                wwc2setAirplaneModeOn(mCxt, false);
            }
        }

        if( currentStatus > TEMP_STATUS_OVER_HEAT){
            if(Define.Source.SOURCE_VIDEO == SourceManager.getCurSource()){
                Log.d("CPUThermalManager", " start kill  com.wwc2.video" );
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        STM32MCUDriver.killProcess(mContext, "com.wwc2.video");
                    }
                }).start();
            }
        }


    }


}
