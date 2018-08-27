package com.askey.dvr.cdr7010.filemanagement.controller;

import android.content.Context;
import android.os.Environment;

import com.askey.dvr.cdr7010.filemanagement.application.FileManagerApplication;
import com.askey.dvr.cdr7010.filemanagement.util.BroadcastUtils;
import com.askey.dvr.cdr7010.filemanagement.util.Const;
import com.askey.dvr.cdr7010.filemanagement.util.ContentResolverUtil;
import com.askey.dvr.cdr7010.filemanagement.util.DateUtil;
import com.askey.dvr.cdr7010.filemanagement.util.Logg;
import com.askey.dvr.cdr7010.filemanagement.util.SdcardUtil;
import com.askey.platform.AskeySettings;

import java.text.ParseException;

/**
 * 项目名称：filemanagement
 * 类描述：
 * 创建人：skysoft  charles.bai
 * 创建时间：2018/4/9 17:40
 * 修改人：skysoft
 * 修改时间：2018/4/9 17:40
 * 修改备注：
 */
public class FileManager {

    private static final String LOG_TAG = FileManager.class.getSimpleName();
    private static FileManager instance;
    private Context mContext;

    public FileManager(Context context){
        mContext = context;
    }

    public static FileManager getSingInstance() {
        if(instance == null){
            instance = new FileManager(FileManagerApplication.getAppContext());
        }
        return instance;
    }

    static {
        System.loadLibrary("native-lib");
        init_native();
    }

    private native static void init_native();

    public native boolean FH_ValidFormat(String mount_path);
    //
// Purpose: 1.Create Event,Manual,Normal,Parking,Picture,System folder
//          2.Use SDCARD space to calculate every folder can use file
//          3.every file struct save in "Table.config"
// Input:  mount path
// Output: bool, true = 1, false = 0;
// ** If SDCARD not clear, return false **
    public native int FH_Init(String mount_path);

//
// Purpose: 1.Choice folderType to openfile
//          2.Get file_num from "Table.config"
//          3.If "Free" folder have extension for folderType, use it to open file.
// Input:  mount path, open filename,
//         folderType: Event, Manual, Normal, Parking, Picture, System
// Output: FILE Pointer
// ** If file number > folderType file_num, return NULL; **

//   public native File FH_Open(String mount_path, String filename, String folderType);
    public native String FH_Open(String filename, int type);

    //
// Purpose: Close opened file
// Input: Opened FILE Pointer
// Output: bool, true = 1, false = 0;

//    public native boolean FH_Close(File file);
    public native boolean FH_Close();

    //
// Purpose: Move the data from cache to disc
// Input:  Opened FILE Pointer
// Output: bool, true = 1, false = 0;

//    public native boolean FH_Sync(File file);
    public native void FH_Sync();

    //
// Purpose: 1.Compare absolute_filepath, if have folderType String, rename file to Free folder
//          2.The file will be change to (number) + folderType extension
// Input:  mount path, Delete file absolute path
// Output: bool, true = 1, false = 0;
    public native boolean FH_Delete(String absolute_filepath,int cameraType);

    //
// Purpose: Finding the path oldest file ,and return absolute_filepath string
// Input:  finding folder path
// Output: oldest_filepath, ""
// ** Oldest file, means the file which is earliest modification time **

    public native String FH_FindOldest(int type,int cameraType);


// Purpose: before FH_Open, Check sdcard and folder status.
// Input: enum eFolderType
// Output: (Error situation)(look #define)
//         SDCARD_PATH_ERROR                       return 2
//         EXIST_FILE_NUM_OVER_LIMIT               return 5     file_over_limit
//         NO_SPACE_NO_NUMBER_TO_RECYCLE           return 6     sdcard_full
//         OPEN_FOLDER_ERROR                       return 3
//         FOLDER_SPACE_OVER_LIMIT                 return 4     file_limit
//  4、5、6可以提示format sdcard
    public native int FH_CheckFolderStatus(int type);

    public native int FH_GetSDCardInfo(int type,int numType);

    public native int FH_GetFolderCameraTypeNumber(int type,int cameraType);

    //
// not implement
// Return ture

//    public native boolean FH_lock(File file);
    public native boolean FH_lock(long filePointer);

// not implement
// Return ture
//    public native boolean FH_unlock(File file);
    public native boolean FH_unlock(long filePointer);


    public boolean validFormat() {
        boolean result = false;
        if(Const.SDCARD_IS_EXIST){
            Const.SDCARD_PATH = Environment.getExternalStorageDirectory().toString();
            result = FH_ValidFormat(Const.SDCARD_PATH);
            Logg.i(LOG_TAG,"=FH_ValidFormat="+result);
        }
        return result;
    }

//    INIT_SUCCESS=0,
//    SDCARD_PATH_ERROR=-1,
//    SDCARD_SPACE_FULL=-2,
//    SDCARD_DETECT_SIZE_ERROR=-3,
//    SDCARD_SIZE_NOT_SUPPORT=-4,
//    TABLE_VERSION_TOO_OLD=-5,
//    TABLE_VERSION_CANNOT_RECOGNIZE=-6,
//    TABLE_READ_ERROR=-7
    public int sdcardInit() {
        int result = 2;
        if(Const.SDCARD_IS_EXIST){
            Const.SDCARD_PATH = Environment.getExternalStorageDirectory().toString();
            result = FH_Init(Const.SDCARD_PATH);
            Logg.i(LOG_TAG,"==sdcardInit=FH_Init="+result);
        }
        return result;
    }

    public String openSdcard(String filename, final String folderType) {
        String result= null;
        if(Const.SDCARD_INIT_SUCCESS && !Const.IS_SDCARD_FULL_LIMIT /*&& !Const.SDCARD_EVENT_FOLDER_OVER_LIMIT
                && !Const.SDCARD_PICTURE_FOLDER_OVER_LIMIT*/){
            if(folderType.equals(Const.EVENT_DIR) || folderType.equals(Const.NORMAL_DIR) || folderType.equals(Const.PICTURE_DIR)){
                int sdcardStatus = FileManager.getSingInstance().checkFolderStatus(folderType);
                Logg.i(LOG_TAG,"checkFolderStatus-》"+sdcardStatus);
                if(sdcardStatus == Const.NO_SPACE_NO_NUMBER_TO_RECYCLE ){
                    Const.IS_SDCARD_FULL_LIMIT = true;
                    String currentAction = Const.CMD_SHOW_SDCARD_FULL_LIMIT;
                    BroadcastUtils.sendLimitBroadcast(FileManagerApplication.getAppContext(),currentAction);
                }else if( sdcardStatus == Const.EXIST_FILE_NUM_OVER_LIMIT ){
                    String currentAction = "";
                    if(folderType.equals(Const.EVENT_DIR)){
                        Const.SDCARD_EVENT_FOLDER_OVER_LIMIT =true;
                        if(Const.SDCARD_PICTURE_FOLDER_OVER_LIMIT){
                            Const.SDCARD_BOTH_EVENT_AND_PICTURE_FOLDER_OVER_LIMIT =true;
                            currentAction = Const.CMD_SHOW_BOTH_EVENT_AND_PICTURE_FOLDER_OVER_LIMIT;
                        }else{
                            currentAction = Const.CMD_SHOW_REACH_EVENT_FILE_OVER_LIMIT;
                        }
                    }else if(folderType.equals(Const.PICTURE_DIR)){
                        Const.SDCARD_PICTURE_FOLDER_OVER_LIMIT =true;
                        if(Const.SDCARD_EVENT_FOLDER_OVER_LIMIT){
                            Const.SDCARD_BOTH_EVENT_AND_PICTURE_FOLDER_OVER_LIMIT =true;
                            currentAction = Const.CMD_SHOW_BOTH_EVENT_AND_PICTURE_FOLDER_OVER_LIMIT;
                        }else{
                            currentAction = Const.CMD_SHOW_REACH_PICTURE_FILE_OVER_LIMIT;
                        }
                    }
                    Const.IS_SDCARD_FOLDER_LIMIT = true;
                    BroadcastUtils.sendLimitBroadcast(mContext,currentAction);
                }else if(sdcardStatus >=2){
                    result= getRecoderFilePath(filename,folderType);
                }
            }else{
                result= getRecoderFilePath(filename,folderType);
            }
            if(!"".equals(result) && null != result){
                final String finalResult = result;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            checkEventAndPictureIsLimit();
                            Thread.sleep(2000);
                            MediaScanner.scanFileAsync(mContext, finalResult);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        }else{
            Logg.e(LOG_TAG,"=====is SDCARD_INIT_SUCCESS====="+Const.SDCARD_INIT_SUCCESS);
            Logg.e(LOG_TAG,"=====is IS_SDCARD_FULL_LIMIT====="+Const.IS_SDCARD_FULL_LIMIT);
//            BroadcastUtils.sendMyBroadcast(FileManagerApplication.getAppContext(),Const.ACTION_SDCARD_STATUS,Const.CMD_SHOW_SDCARD_INIT_FAIL);
        }
        return result;
    }

    private String getRecoderFilePath(String filename, String folderType) {
        int type = getCurrentType(folderType);
        Logg.i(LOG_TAG,"=====type====="+type+"==filename=="+filename);
        String result = getRecoderPath(filename,type);
        Logg.i(LOG_TAG,"=====FH_Open====="+result);
        if(result == null || result.equals("")){
            String oldestPath = null;
            if(filename.contains("_2")){
                oldestPath = FH_FindOldest(type,2);
            }else{
                oldestPath = FH_FindOldest(type,1);
            }
            if(null != oldestPath && !"".equals(oldestPath)){
                Logg.i(LOG_TAG,"=====FH_FindOldest====="+oldestPath);
                boolean deleteResult = MediaScanner.deleteFile(oldestPath);
                Logg.i(LOG_TAG,"=====deleteResult====="+deleteResult);
                if(deleteResult && Const.SDCARD_IS_EXIST){
                    result = getRecoderPath(filename,type);
                }
            }else{
                Logg.e(LOG_TAG,"=====oldestPath====="+oldestPath);
            }
        }
        return result;
    }

    private String isMp4FileName = "";
    /**
     * 处理文件名称，当系统时间未校正时，名称中需要增加_unkonwn,若已经改变则不需要处理
     * */
    private String getRecoderPath(String filename, int type) {
        // SYSSET_last_rectime   19700101000000
        if(filename.contains(".mp4")){
            String datatime = ContentResolverUtil.getStringSettingValue(AskeySettings.Global.SYSSET_LAST_RECTIME);
            int  time = ContentResolverUtil.getIntSettingValue(AskeySettings.Global.SYSSET_LAST_RECTIME,0);
            Logg.i(LOG_TAG,"===getRecoderPath==datatime====="+datatime);
            Logg.i(LOG_TAG,"===getRecoderPath==time====="+time);
            String str[] = filename.split("\\.");
            if(datatime.length()==14){
                String index = datatime.substring(0,4);
                Logg.i(LOG_TAG,"===getRecoderPath==index====="+index);
                String lastRectime = "";
                if(Integer.valueOf(index)<2018){
                    try {
                        datatime = DateUtil.timeAddOneMinute(datatime);
                        Logg.i(LOG_TAG,"==getRecoderPath====datatime==="+datatime);
                        lastRectime = datatime.substring(2,datatime.length());
                        Logg.i(LOG_TAG,"==getRecoderPath====lastRectime==="+lastRectime);
                        filename = lastRectime +"_UNKNOWN." + str[1] ;
                        Logg.i(LOG_TAG,"==getRecoderPath====filename==="+filename);
                        ContentResolverUtil.setStringSettingValue(AskeySettings.Global.SYSSET_LAST_RECTIME,datatime);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }else{
                    ContentResolverUtil.setStringSettingValue(AskeySettings.Global.SYSSET_LAST_RECTIME,"20"+str[0]);
                }
            }
            isMp4FileName = filename;
        }else{
            filename = isMp4FileName;
        }
        return FH_Open(filename,type);
    }

    public void checkEventAndPictureIsLimit() {
        int eventCurrentNum = FileManager.getSingInstance().FH_GetSDCardInfo(Const.TYPE_EVENT_DIR,Const.CURRENTNUM);
        int eventLimitNum = FileManager.getSingInstance().FH_GetSDCardInfo(Const.TYPE_EVENT_DIR,Const.LIMITNUM);
        if(eventCurrentNum == eventLimitNum){
            Const.IS_SDCARD_FOLDER_LIMIT = true;
            Const.SDCARD_EVENT_FOLDER_LIMIT =true;
        }

        int pictureCurrentNum = FileManager.getSingInstance().FH_GetSDCardInfo(Const.TYPE_PICTURE_DIR,Const.CURRENTNUM);
        int pictureLimitNum = FileManager.getSingInstance().FH_GetSDCardInfo(Const.TYPE_PICTURE_DIR,Const.LIMITNUM);
        if(pictureCurrentNum == pictureLimitNum){
            Const.IS_SDCARD_FOLDER_LIMIT = true;
            Const.SDCARD_PICTURE_FOLDER_LIMIT =true;
        }

        if(Const.SDCARD_EVENT_FOLDER_LIMIT && Const.SDCARD_PICTURE_FOLDER_LIMIT){
            Const.SDCARD_BOTH_EVENT_AND_PICTURE_FOLDER_LIMIT =true;
            String currentAction = Const.CMD_SHOW_BOTH_EVENT_AND_PICTURE_FOLDER_LIMIT;
            BroadcastUtils.sendLimitBroadcast(FileManagerApplication.getAppContext(),currentAction);
        }else if(Const.SDCARD_EVENT_FOLDER_LIMIT && !Const.SDCARD_PICTURE_FOLDER_LIMIT){
            String currentAction = Const.CMD_SHOW_REACH_EVENT_FILE_LIMIT;
            BroadcastUtils.sendLimitBroadcast(FileManagerApplication.getAppContext(),currentAction);
        }
    }

//    GLOBAL_SDCARD_PATH_ERROR=-1,      format
//    OPEN_FOLDER_ERROR=-2,             format
//    EXIST_FILE_NUM_OVER_LIMIT=-3,     file_over_limit
//    FOLDER_SPACE_OVER_LIMIT=-4,       format
//    NO_SPACE_NO_NUMBER_TO_RECYCLE=-5   sdcard_full
    public int checkFolderStatus(String folderType) {
        int type = getCurrentType(folderType);
        return FH_CheckFolderStatus(type);
    }

    public void sendUnreachLimitFileBroadcastByType(String folderType) {
        String currentAction = "";
        int sdcardStatus = FileManager.getSingInstance().checkFolderStatus(folderType);
        Logg.i(LOG_TAG,"checkFolderStatus-》"+sdcardStatus);
        if(Const.IS_SDCARD_FULL_LIMIT && sdcardStatus >=2 ){
            Const.IS_SDCARD_FULL_LIMIT = false;
            currentAction = Const.CMD_SHOW_UNREACH_SDCARD_FULL_LIMIT;
            BroadcastUtils.sendLimitBroadcast(mContext,currentAction);
        }

        if(Const.IS_SDCARD_FOLDER_LIMIT && sdcardStatus >=2){
            if(folderType.equals(Const.EVENT_DIR)){
                Const.SDCARD_EVENT_FOLDER_LIMIT = false;
                Const.SDCARD_EVENT_FOLDER_OVER_LIMIT = false;
                currentAction = Const.CMD_SHOW_UNREACH_EVENT_FILE_LIMIT;
                BroadcastUtils.sendLimitBroadcast(mContext,currentAction);
            }else if(folderType.equals(Const.PICTURE_DIR)){
                Const.SDCARD_PICTURE_FOLDER_LIMIT = false;
                Const.SDCARD_PICTURE_FOLDER_OVER_LIMIT = false;
                currentAction = Const.CMD_SHOW_UNREACH_PICTURE_FILE_LIMIT;
                BroadcastUtils.sendLimitBroadcast(mContext,currentAction);
            }
            Const.SDCARD_BOTH_EVENT_AND_PICTURE_FOLDER_LIMIT =false;
            Const.SDCARD_BOTH_EVENT_AND_PICTURE_FOLDER_OVER_LIMIT =false;
            Const.IS_SDCARD_FOLDER_LIMIT = false;
        }

            //取消限制后需要更改inint状态
        if(!Const.SDCARD_INIT_SUCCESS && sdcardStatus >=2){
            Const.SDCARD_INIT_SUCCESS = true;
            BroadcastUtils.sendMyBroadcast(FileManagerApplication.getAppContext(),Const.ACTION_SDCARD_STATUS,Const.CMD_SHOW_SDCARD_INIT_SUCC);
        }
    }

    private int getCurrentType(String folderType) {
        int type = -1;
        if(folderType.equals(Const.EVENT_DIR)){
            type = Const.TYPE_EVENT_DIR;
        }else if(folderType.equals(Const.NORMAL_DIR)){
            type = Const.TYPE_NORMAL_DIR;
        }else if(folderType.equals(Const.PARKING_DIR)){
            type = Const.TYPE_PARKING_DIR;
        }else if(folderType.equals(Const.PICTURE_DIR)){
            type = Const.TYPE_PICTURE_DIR;
        }else if(folderType.equals(Const.SYSTEM_DIR)){
            type = Const.TYPE_SYSTEM_DIR;
        }else if(folderType.equals(Const.HASH_NORMAL_DIR)){
            type = Const.TYPE_HASH_NORMAL_DIR;
        }else if(folderType.equals(Const.NMEA_EVENT_DIR)){
            type = Const.TYPE_NMEA_EVENT_DIR;
        }else if(folderType.equals(Const.NMEA_NORMAL_DIR)){
            type = Const.TYPE_NMEA_NORMAL_DIR;
        }else if(folderType.equals(Const.HASH_EVENT_DIR)){
            type = Const.TYPE_HASH_EVENT_DIR;
        }
        return type;
    }

    public String getTypebyPath(String path) {
        String type =Const.NORMAL_DIR;
        if(path.contains(Const.EVENT_DIR)){
            type = Const.EVENT_DIR;
        }else if(path.contains(Const.NORMAL_DIR)){
            type = Const.NORMAL_DIR;
        }else if(path.contains(Const.PARKING_DIR)){
            type = Const.PARKING_DIR;
        }else if(path.contains(Const.PICTURE_DIR)){
            type = Const.PICTURE_DIR;
        }else if(path.contains(Const.SYSTEM_DIR)){
            type = Const.SYSTEM_DIR;
        }else if(path.contains(Const.HASH_NORMAL_DIR)){
            type = Const.HASH_NORMAL_DIR;
        }else if(path.contains("SYSTEM/NMEA/EVENT")){
            type = Const.NMEA_EVENT_DIR;
        }else if(path.contains("SYSTEM/NMEA/NORMAL")){
            type = Const.NMEA_NORMAL_DIR;
        }else if(path.contains(Const.HASH_EVENT_DIR)){
            type = Const.HASH_EVENT_DIR;
        }
        return type;
    }
}
