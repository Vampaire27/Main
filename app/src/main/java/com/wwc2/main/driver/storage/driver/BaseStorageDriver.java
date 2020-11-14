package com.wwc2.main.driver.storage.driver;

import android.text.TextUtils;
import android.util.Log;

import com.wwc2.accoff_interface.AccoffDefine;
import com.wwc2.common_interface.utils.StorageDevice;
import com.wwc2.corelib.db.Packet;
import com.wwc2.corelib.model.BaseModel;
import com.wwc2.corelib.model.custom.IntegerSS;
import com.wwc2.corelib.model.custom.IntegerSSBoolean;
import com.wwc2.corelib.model.custom.MIntegerSSArray;
import com.wwc2.corelib.model.custom.MIntegerSSBoolean;
import com.wwc2.corelib.model.custom.MIntegerSSBooleanArray;
import com.wwc2.corelib.model.custom.MIntegerSSS;
import com.wwc2.corelib.utils.log.LogUtils;
import com.wwc2.main.accoff.AccoffListener;
import com.wwc2.main.driver.memory.BaseMemoryDriver;
import com.wwc2.main.driver.storage.StorageDriverable;
import com.wwc2.main.manager.ModuleManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * the base storage driver.
 *
 * @author wwc2
 * @date 2017/1/29
 */
public abstract class BaseStorageDriver extends BaseMemoryDriver implements StorageDriverable {

    /**TAG*/
    private static final String TAG = "BaseStorageDriver";

    private Integer mCurrentAccStep = AccoffListener.AccoffStep.STEP_DEFAULT;

    /**
     * 数据Model
     */
    protected static class StorageModel extends BaseModel {
        @Override
        public Packet getInfo() {
            Packet ret = new Packet();
            ret.putParcelable("StorageInfo", mStorageInfo.getVal());
            ret.putParcelableArray("StorageList", mStorageList.getVal());
            ret.putParcelableArray("StorageInfoList",mStorageInfoList.getVal());
            ret.putParcelable("StorageSerialNo",mStorageSerialNo.getVal());
            return ret;
        }

        /**
         * 存储设备发生变化
         */
        private MIntegerSSBoolean mStorageInfo = new MIntegerSSBoolean(this, "StorageInfoListener", null);

        public MIntegerSSBoolean getStorageInfo() {
            return mStorageInfo;
        }
        /**
         * 存储设备列表 include flash sd usb1 usb2 ......
         */
        private MIntegerSSBooleanArray mStorageList = new MIntegerSSBooleanArray(this,"StorageListListener",null);

        public MIntegerSSBooleanArray getStorageList() {
            return mStorageList;
        }
        /**
         * 存储设备列表
         */
        private MIntegerSSArray mStorageInfoList = new MIntegerSSArray(this,"StorageInfoListListener",null);//StorageInfoListListener

        public MIntegerSSArray getStorageInfoList() {
            return mStorageInfoList;
        }
        /**
         * 存储设备列表
         */
        private MIntegerSSS mStorageSerialNo = new MIntegerSSS(this,"StorageSerialNoListener",null);//StorageInfoListListener

        public MIntegerSSS getStorageSerialNo() {
            return mStorageSerialNo;
        }

    }

    @Override
    public BaseModel newModel() {
        return new StorageModel();
    }
    /**
     * get the model object.
     */
    protected StorageModel Model() {
        StorageModel ret = null;
        BaseModel model = getModel();
        if (model instanceof StorageModel) {
            ret = (StorageModel) model;
        }
        return ret;
    }

    /**
     * register Storage.
     */
    protected abstract void regStorages();

    @Override
    public void onCreate(Packet packet) {
        mountFilterMap.clear();
        super.onCreate(packet);
        Model().getStorageInfoList().setVal(null);
        regStorage(StorageDevice.NAND_FLASH, "Nand Flash");
        regStorages();
        readData();
        ModuleManager.getLogicByName(AccoffDefine.MODULE).getModel().bindListener(mAccoffListener);
    }

    @Override
    public void onDestroy() {
        if(mMemory != null){
            mMemory.save();
        }
        if (AccoffListener.AccoffStep.isDeepSleep(mCurrentAccStep)) {
            setMounted(StorageDevice.USB, false);
            setMounted(StorageDevice.USB1, false);
            setMounted(StorageDevice.USB2, false);
            setMounted(StorageDevice.USB3, false);
            LogUtils.d(TAG, "---onDestroy-Will goto deep sleep end");
        }
        unregStorages();
        ModuleManager.getLogicByName(AccoffDefine.MODULE).getModel().unbindListener(mAccoffListener);
    }

    @Override
    public IntegerSSBoolean getStorageInfo(int type) {
        return getStorage(type);
    }

    @Override
    public IntegerSSBoolean[] getStoragesInfo() {
        return getStorageArray();
    }

    protected Map<Integer,Boolean> mountFilterMap = new ConcurrentHashMap<>();

    /**
     * register the storage.
     */
    protected void regStorage(Integer type, String describe) {
        if (StorageDevice.UNKNOWN != type) {
            final String path = StorageDevice.getPath(getMainContext(),type);
            final Boolean mounted = StorageDevice.isDiskMounted(getMainContext(), type);
            IntegerSSBoolean storage = new IntegerSSBoolean(type, describe, path, mounted);

            LogUtils.d(TAG, " regStorage StorageArray newVal type = " + storage.getInteger() +
                    ", describe = " + storage.getString1() +
                    ", path = " + storage.getString2() +
                    ", mount:" + storage.getBoolean());
            Model().getStorageList().addVal(-1, storage);
//            if(type != StorageDevice.NAND_FLASH){
                mountFilterMap.put(type, mounted == false);
//            }
        }
    }

    /**
     * unregister the storages.
     */
    protected void unregStorages() {
        Model().getStorageList().setVal(null);
    }

    /**
     * is register storage.
     */
    protected boolean isRegStorage(Integer type) {
        boolean ret = false;
        if (null != getStorage(type)) {
            ret = true;
        }
        return ret;
    }

    /**
     * get storage by type.
     */
    protected IntegerSSBoolean getStorage(Integer type) {
        IntegerSSBoolean ret = null;
        if (null != Model().getStorageList()) {
            ret = Model().getStorageList().getValByInteger(type);
        }
        return ret;
    }

    /**
     * get storage index.
     */
    protected int getStorageIndex(Integer type) {
        int ret = -1;
        if (null != Model().getStorageList()) {
            IntegerSSBoolean[] storages = Model().getStorageList().getVal();
            if (null != storages) {
                for (int i = 0; i < storages.length; i++) {
                    IntegerSSBoolean temp = storages[i];
                    if (null != temp) {
                        if (type.equals(temp.getInteger())) {
                            ret = i;
                            break;
                        }
                    }
                }
            }
        }
        return ret;
    }
    //----------------------develop by ltj--------------------------

    @Override
    public String filePath() {
        return "MountInfo.ini";
    }

    /**
     * write serialArray from file
     */
    @Override
    public boolean readData() {
        LogUtils.d(TAG, "readData !");
        boolean ret = false;
        if(mMemory != null){
            IntegerSSBoolean[] storageArray =  getStorageArray();
            if(storageArray != null){
                LogUtils.d(TAG,"storageArray length:" + storageArray.length);
                for(int i = 0 ; i< storageArray.length;i++){
                    IntegerSSBoolean integerSSBoolean = storageArray[i];
                    Object serialId = mMemory.get("SERIALID", StorageDevice.getPath(getMainContext(),integerSSBoolean.getInteger()));
                    LogUtils.d(TAG, "readData key:" + StorageDevice.toString(integerSSBoolean.getInteger()) + " serialId:" + serialId);
                    String id = null;
                    if (null != serialId) {
                        id = (String)serialId;
//                        int index = getStorageIndex(integerSSBoolean.getInteger());//有序列号设置已挂载
//                        if(index > -1){
//                            IntegerSSBoolean temp = new IntegerSSBoolean(integerSSBoolean.getInteger(), integerSSBoolean.getString1(), integerSSBoolean.getString2(), true);
//                            Model().getStorageList().setVal(index, temp);
//                            LogUtils.d(TAG, "readData serialId not null set mounted:true");
//                        }
                    }else{
                        if(integerSSBoolean.getBoolean()){/*设备挂载没有读取到ini数据则读取挂载数据*/
                            id = getSerialByStorageId(integerSSBoolean.getInteger());
                            LogUtils.d(TAG, "readData mount serialId:" + id);
                        }
                    }
                    getStorageInfoArray().addVal(-1,new IntegerSS(integerSSBoolean.getInteger(),id,integerSSBoolean.getString2()));
                    ret = true;
                }
                IntegerSS[] storageInfoArray =  getStorageInfoArray().getVal();
                if(storageInfoArray != null){
                    LogUtils.d(TAG,"mIntegerSSes length:" + (storageInfoArray == null ? 0 : storageInfoArray.length));
                }
            }else{
                LogUtils.d(TAG," integerSSBooleanArray is null!" );
            }
        }
        return ret;
    }

    /**
     * save serialArray to file
     */
    @Override
    public boolean writeData() {
        IntegerSS[] storageInfoArray = getStorageInfoArray().getVal();
        LogUtils.d(TAG, "writeData !" + " mMemory:" + (mMemory == null? "null" : "init") + " storageInfoArray:" +  (storageInfoArray == null? "null" : "init"));
        boolean ret = false;
        if (null != mMemory) {
            if(storageInfoArray != null){
                LogUtils.d(TAG,"storageInfoArray length:" + storageInfoArray.length);
                for(int i = 0 ; i< storageInfoArray.length;i++){
                    IntegerSS storageSerialInfo = storageInfoArray[i];
                    String id = storageSerialInfo.getString1();
                    //LogUtils.d(TAG, "writeData "+ StorageDevice.toString(storageSerialInfo.getInteger()) +" serialId:" + id);
                    if (!TextUtils.isEmpty(id)) {/*空值不用保存*/
                        mMemory.set("SERIALID", StorageDevice.getPath(getMainContext(),storageSerialInfo.getInteger()), id);
                        ret = true;
                    }
                }
            }else{
                LogUtils.d(TAG,"mIntegerSSArray is null!");
            }
        }
        return ret;
    }


    /**
     * set mounted.
     */
    protected void setMounted(Integer type, Boolean mounted) {
        IntegerSSBoolean current = Model().getStorageInfo().getVal();
        if (null != current) {
            LogUtils.d(TAG, "driver old storage: type = " + StorageDevice.toString(current.getInteger()) + ", mounted = " + current.getBoolean());
        }
        LogUtils.d(TAG, "driver new storage: type = " + StorageDevice.toString(type) + ", mounted = " + mounted);
        updateMountedByType(type, mounted,true);
    }

    /**
     * acc 断电上电挂载检测
     * @param type  storage id
     * @param mounted   mount value
     * @param filter  mount Receiver need set true;
     */
    protected void updateMountedByType(Integer type, Boolean mounted,Boolean filter) {
        IntegerSSBoolean storage = getStorage(type);
        if (null != storage) {
            LogUtils.d(TAG,"updateMountedByType: " + StorageDevice.toString(type) + " mounted:" + mounted + " memoryMounted :" + storage.getBoolean());
            if (mounted != storage.getBoolean() || mounted) {//插入设备不需要判断之前的状态，否则会出现ACC ON后不重新扫描。
                if (null != Model().getStorageList()) {
                    final int index = getStorageIndex(type);
                    if (-1 != index) {
                        IntegerSSBoolean temp = new IntegerSSBoolean(storage.getInteger(), storage.getString1(), storage.getString2(), mounted);
                        Model().getStorageList().setVal(index, temp);
                        if(filter){
                            LogUtils.d(TAG,"filter temp storageId:" + temp.getInteger() + " describe:" + temp.getString1() + " path:" + temp.getString2() + " mount:" + mounted + " mountFilterMap:" + mountFilterMap.get(type));
                            if(mountFilter(type,mounted) /*&& (mountFilterMap.get(type) == false)*/){/*filter acc on*/
                                Model().getStorageInfo().setVal(temp);
                            }
                        }else {
                            Model().getStorageInfo().setVal(temp);
                            LogUtils.d(TAG,"temp storageId:" + temp.getInteger() + " describe:" + temp.getString1() + " path:" + temp.getString2() + " mount:" + mounted);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void updateMountedStatus(Integer type, boolean mount) {
        updateMountedByType(type, mount, false);
    }

    /**
     *
     * @param type
     * @param mounted
     * @return
     */
    private Boolean mountFilter(Integer type, Boolean mounted){
        boolean filter = false;
        String memorySerial = getSerialFromArrayById(type);
        String mountSerial = updateMountSerialByType(type, mounted);
        LogUtils.d(TAG," mountFilter memorySerial:" + memorySerial + " mountSerial:" + mountSerial);
        if(!TextUtils.isEmpty(memorySerial) && !TextUtils.isEmpty(mountSerial)){
            if(!mountSerial.equals(memorySerial)){
                filter = true;
            }
        }else if(memorySerial == null && !TextUtils.isEmpty(mountSerial)){
            LogUtils.d(TAG," mountFilter mount:" + " mountSerial:" + mountSerial);
            filter = true;/*启动main 插sd卡\\播放别的卡*/
        }else if(memorySerial == null && mountSerial == null){
            filter = true;/*播放sd拔卡*/
        }else if(!TextUtils.isEmpty(memorySerial) && mountSerial == null){
            filter = true;/*播放sd拔卡 2、acc on拔卡*/
        }
        LogUtils.d(TAG," mountFilter filter:" + filter);
        return filter;
    }
    /**自定义获取储存卡路径*/
    public abstract String getSerialByStorageId(int storageId);

    protected MIntegerSSArray getStorageInfoArray(){
        return Model().getStorageInfoList();
    }

    protected IntegerSSBoolean[] getStorageArray(){
        if(Model().getStorageList() != null){
            /*
            IntegerSSBoolean[] debugArray = Model().getStorageList().getVal();//debug log info
            if(debugArray != null){
                for(int i =0;i <debugArray.length;i++){
                    IntegerSSBoolean item = debugArray[i];
                    LogUtils.d(TAG, " StorageArray newVal type = " + item.getInteger() +
                            ", describe = " + item.getString1() +
                            ", path = " + item.getString2() +
                            ", mount:" + item.getBoolean());
                }
            }
            */
            return Model().getStorageList().getVal();
        }
        return null;
    }

    /**
     * update mount serial no
     * @param storageId
     * @param mounted
     * @return mount serial no
     */
    protected String updateMountSerialByType(Integer storageId,Boolean mounted){
        String mountSerialId = null;
        IntegerSS[] storageInfoArray = getStorageInfoArray().getVal();
        LogUtils.d(TAG," integerSSArray1 length:" + (storageInfoArray == null? 0: storageInfoArray.length));
        if(getStorageInfoArray() != null){
            IntegerSS storageInfo = getStorageInfoArray().getValByInteger(storageId);
            if(storageInfo != null){
                mountSerialId = mounted ? getSerialByStorageId(storageId) : null;/*获取挂载序列号,错误清空*/
//                if(!TextUtils.isEmpty(mountSerialId)){
                    storageInfo = new IntegerSS(storageInfo.getInteger(),mountSerialId,storageInfo.getString2());
                    int index = getStorageInfoIndex(storageId);
                    LogUtils.d(TAG,"updateIntegerSSArray index:" + index);
                    if(-1 != index){
                        getStorageInfoArray().setVal(index, storageInfo);
//                        if(mMemory != null){
//                            mMemory.save();
//                        }
                    }
//                }
                IntegerSS[] integerSSArray = getStorageInfoArray().getVal();
                LogUtils.d(TAG," integerSSArray length:" + (integerSSArray == null? 0: integerSSArray.length));
                LogUtils.d(TAG," updateIntegerSSArray Integer:" + storageInfo.getInteger() + " serialNo:" + storageInfo.getString1() + " path:" + storageInfo.getString2() + "length:" + (integerSSArray == null? 0: integerSSArray.length));
            }
        }
        return mountSerialId;
    }

    /**
     * read serialNo from array
     * @param storageId
     * @return
     */
    protected String getSerialFromArrayById(int storageId){
        String result = null;
        if(getStorageInfoArray() != null){
            IntegerSS storageInfo = getStorageInfoArray().getValByInteger(storageId);
            if(storageInfo != null){
                result = storageInfo.getString1();
            }
        }
        return result;
    }

    /**
     * get storage index  from MIntegerSSArray by id
     * @param storageId storageId
     * @return  index
     */
    protected int getStorageInfoIndex(Integer storageId){
        int ret = -1;
        if (null != getStorageInfoArray()) {
            IntegerSS[] storageSerials = getStorageInfoArray().getVal();
            if (null != storageSerials) {
                for (int i = 0; i < storageSerials.length; i++) {
                    IntegerSS temp = storageSerials[i];
                    if (null != temp) {
                        if (storageId.equals(temp.getInteger())) {
                            ret = i;
                            break;
                        }
                    }
                }
            }
        }
        return ret;
    }

    /**
     * read serialNo from file
     * @param path file path
     * @return serialNo
     */
    protected String readId(String path){
        File cidFile = new File(path);
        String cid = null;
        BufferedReader bufferedReader=null;
        if(cidFile != null && cidFile.exists()){
            try {
                bufferedReader = new BufferedReader(new FileReader(cidFile));
                cid = bufferedReader.readLine();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.e(TAG, "FileNotFoundException:" + e.getMessage());
            } catch (IOException e) {
                Log.e(TAG,"IOException:" + e.getMessage());
                e.printStackTrace();
            }catch (Exception e){
                Log.e(TAG,"Exception:" + e.getMessage());
                e.printStackTrace();
            }finally {
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        LogUtils.d(TAG, "readId:" + cid+ " ######################################");
        return cid;
    }

    private AccoffListener mAccoffListener = new AccoffListener() {
        @Override
        public void AccoffStepListener(Integer oldVal, Integer newVal) {
            mCurrentAccStep = newVal;
        }
    };
}
