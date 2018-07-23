package com.askey.dvr.cdr7010.filemanagement.controller;

import android.content.Context;
import android.os.Environment;

import com.askey.dvr.cdr7010.filemanagement.application.FileManagerApplication;
import com.askey.dvr.cdr7010.filemanagement.util.BroadcastUtils;
import com.askey.dvr.cdr7010.filemanagement.util.Const;
import com.askey.dvr.cdr7010.filemanagement.util.Logg;
import com.askey.dvr.cdr7010.filemanagement.util.SdcardUtil;

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
    public native boolean FH_Init(String mount_path);

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
    public native boolean FH_Sync();

    //
// Purpose: 1.Compare absolute_filepath, if have folderType String, rename file to Free folder
//          2.The file will be change to (number) + folderType extension
// Input:  mount path, Delete file absolute path
// Output: bool, true = 1, false = 0;
    public native boolean FH_Delete(String absolute_filepath);

    //
// Purpose: Finding the path oldest file ,and return absolute_filepath string
// Input:  finding folder path
// Output: oldest_filepath, ""
// ** Oldest file, means the file which is earliest modification time **

    public native String FH_FindOldest(int type);


    public native int FH_FolderCanUseFilenumber(int type);


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

    public boolean sdcardInit() {
        boolean result = false;
        if(Const.SDCARD_IS_EXIST){
            Const.SDCARD_PATH = Environment.getExternalStorageDirectory().toString();
            result = FH_Init(Const.SDCARD_PATH);
            Logg.i(LOG_TAG,"==sdcardInit=FH_Init="+result);
        }
        return result;
    }

    public String openSdcard(String filename, String folderType) {
        String result= null;
        if(Const.SDCARD_INIT_SUCCESS && !Const.IS_SDCARD_FULL_LIMIT /*&& !Const.SDCARD_EVENT_FOLDER_OVER_LIMIT
                && !Const.SDCARD_PICTURE_FOLDER_OVER_LIMIT*/){
            int sdcardStatus = FileManager.getSingInstance().checkFolderStatus(Const.EVENT_DIR);
            Logg.i(LOG_TAG,"checkFolderStatus-》"+sdcardStatus);
            if(sdcardStatus == Const.NO_SPACE_NO_NUMBER_TO_RECYCLE ){
                Const.IS_SDCARD_FULL_LIMIT = true;
                String currentAction = Const.CMD_SHOW_SDCARD_FULL_LIMIT;
                BroadcastUtils.sendLimitBroadcast(FileManagerApplication.getAppContext(),currentAction);
            }else if(sdcardStatus == Const.FOLDER_SPACE_OVER_LIMIT || sdcardStatus == Const.EXIST_FILE_NUM_OVER_LIMIT ){
                String currentAction = "";
                if(folderType.equals(Const.EVENT_DIR)){
                    Const.SDCARD_EVENT_FOLDER_OVER_LIMIT =true;
                    currentAction = Const.CMD_SHOW_REACH_EVENT_FILE_OVER_LIMIT;
                }else if(folderType.equals(Const.PICTURE_DIR)){
                    Const.SDCARD_PICTURE_FOLDER_OVER_LIMIT =true;
                    currentAction = Const.CMD_SHOW_REACH_PICTURE_FILE_OVER_LIMIT;
                }
                Const.IS_SDCARD_FOLDER_LIMIT = true;
                BroadcastUtils.sendLimitBroadcast(mContext,currentAction);
            }else if(sdcardStatus >=2){
                result= getRecoderFilePath(filename,folderType,sdcardStatus);
            }
            if(!"".equals(result) && null != result){
                final String finalResult = result;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
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

    private String getRecoderFilePath(String filename, String folderType,int sdcardStatus) {
        int type = getCurrentType(folderType);
        Logg.i(LOG_TAG,"=====type====="+type+"==filename=="+filename);
        String result = FH_Open(filename,type);
        Logg.i(LOG_TAG,"=====FH_Open====="+result);
        if(result == null || result.equals("")){
            sendReachLimitFileBroadcastByType(folderType);
            String oldestPath = FH_FindOldest(type);
            if(null != oldestPath && !"".equals(oldestPath)){
                Logg.i(LOG_TAG,"=====FH_FindOldest====="+oldestPath);
                boolean deleteResult = MediaScanner.delete(oldestPath);
                Logg.i(LOG_TAG,"=====deleteResult====="+deleteResult);
                if(deleteResult){
                    result = FH_Open(filename,type);
                }
            }else{
                Logg.e(LOG_TAG,"=====oldestPath====="+oldestPath);
            }
        }
        return result;
    }

    private void sendReachLimitFileBroadcastByType(String folderType) {
        if(folderType.equals(Const.EVENT_DIR)){
            Const.SDCARD_EVENT_FOLDER_LIMIT =true;
            String currentAction = Const.CMD_SHOW_REACH_EVENT_FILE_LIMIT;
            BroadcastUtils.sendLimitBroadcast(mContext,currentAction);
        }else if(folderType.equals(Const.PICTURE_DIR)){
            Const.SDCARD_PICTURE_FOLDER_LIMIT =true;
            String currentAction = Const.CMD_SHOW_REACH_PICTURE_FILE_LIMIT;
            BroadcastUtils.sendLimitBroadcast(mContext,currentAction);
        }
        Const.IS_SDCARD_FOLDER_LIMIT = true;
    }

    //         SDCARD_PATH_ERROR                       return -2
//         EXIST_FILE_NUM_OVER_LIMIT               return -5     file_over_limit
//         NO_SPACE_NO_NUMBER_TO_RECYCLE           return -6     sdcard_full
//         OPEN_FOLDER_ERROR                       return -3
//         FOLDER_SPACE_OVER_LIMIT                 return -4     file_limit
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
            }else if(folderType.equals(Const.NORMAL_DIR)){
                currentAction = Const.CMD_SHOW_UNREACH_NORMAL_FILE_LIMIT;
                BroadcastUtils.sendLimitBroadcast(mContext,currentAction);
            }else if(folderType.equals(Const.PARKING_DIR)){
                currentAction = Const.CMD_SHOW_UNREACH_PARKING_FILE_LIMIT;
                BroadcastUtils.sendLimitBroadcast(mContext,currentAction);
            }else if(folderType.equals(Const.PICTURE_DIR)){
                Const.SDCARD_PICTURE_FOLDER_LIMIT = false;
                Const.SDCARD_PICTURE_FOLDER_OVER_LIMIT = false;
                currentAction = Const.CMD_SHOW_UNREACH_PICTURE_FILE_LIMIT;
                BroadcastUtils.sendLimitBroadcast(mContext,currentAction);
            }else if(folderType.equals(Const.SYSTEM_DIR)){
                currentAction = Const.CMD_SHOW_UNREACH_SYSTEM_FILE_LIMIT;
                BroadcastUtils.sendLimitBroadcast(mContext,currentAction);
            }
            Const.IS_SDCARD_FOLDER_LIMIT = false;

        }

            //取消限制后需要更改inint状态
        if(!Const.SDCARD_INIT_SUCCESS){
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
        }else if(folderType.equals(Const.CAMERA2_DIR)){
            type = Const.TYPE_CAMERA2_DIR;
        }else if(folderType.equals(Const.NMEA_EVENT_DIR)){
            type = Const.TYPE_NMEA_EVENT_DIR;
        }else if(folderType.equals(Const.NMEA_NORMAL_DIR)){
            type = Const.TYPE_NMEA_NORMAL_DIR;
        }else if(folderType.equals(Const.NMEA_CAMERA_DIR)){
            type = Const.TYPE_NMEA_CAMERA_DIR;
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
        }else if(path.contains(Const.CAMERA2_DIR)){
            type = Const.CAMERA2_DIR;
        }else if(path.contains("SYSTEM/NMEA/EVENT")){
            type = Const.NMEA_EVENT_DIR;
        }else if(path.contains("SYSTEM/NMEA/NORMAL")){
            type = Const.NMEA_NORMAL_DIR;
        }else if(path.contains("SYSTEM/NMEA/CAMERA2")){
            type = Const.NMEA_CAMERA_DIR;
        }
        return type;
    }
}
