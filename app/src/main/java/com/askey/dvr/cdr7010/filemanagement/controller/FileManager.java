package com.askey.dvr.cdr7010.filemanagement.controller;

import android.content.Context;
import android.os.Environment;

import com.askey.dvr.cdr7010.filemanagement.application.FileManagerApplication;
import com.askey.dvr.cdr7010.filemanagement.util.BroadcastUtils;
import com.askey.dvr.cdr7010.filemanagement.util.Const;
import com.askey.dvr.cdr7010.filemanagement.util.Logg;
import com.askey.dvr.cdr7010.filemanagement.util.SdcardUtil;
import com.askey.dvr.cdr7010.filemanagement.util.StringUtil;

import java.io.File;
import java.io.UnsupportedEncodingException;

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

    //
// not implement
// Return ture

//    public native boolean FH_lock(File file);
    public native boolean FH_lock(long filePointer);

// not implement
// Return ture
//    public native boolean FH_unlock(File file);
    public native boolean FH_unlock(long filePointer);

    public boolean sdcardInit() {
        Const.SDCARD_PATH = Environment.getExternalStorageDirectory().toString();
        boolean result = FH_Init(Const.SDCARD_PATH);
/*        if(!result){
            boolean ret = MediaScanner.deleteDirectory(Const.SDCARD_PATH);
            if(ret){
                result = FH_Init(Const.SDCARD_PATH);
            }
        }*/
        return result;
    }

    public String openSdcard(String filename, String folderType) {
        int type = getCurrentType(folderType);
        Logg.i(LOG_TAG,"=====type====="+type+"==filename=="+filename);
        String result= null;
        if(sdcardInit()){
            result = FH_Open(filename,type);
            Logg.i(LOG_TAG,"=====FH_Open====="+result);
            if(result == null || result.equals("")){
                // 参数错误, sdcard满或者文件夹个数达到最大限制    这里定义后需要发送相应的文件夹满的通知
                sendLimitFileBroadcastByType(folderType);
                String oldestPath = FH_FindOldest(type);
                Logg.i(LOG_TAG,"=====FH_FindOldest====="+oldestPath);
                boolean deleteResult = MediaScanner.delete(oldestPath);
                Logg.i(LOG_TAG,"=====deleteResult====="+deleteResult);
                if(deleteResult){
                    sendUnreachLimitFileBroadcastByType(folderType);
                    result = FH_Open(filename,type);
                }
            }

            final String finalResult = result;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(2000);
                        MediaScanner.scanFileAsync(mContext,finalResult);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();


        }else{
            BroadcastUtils.sendLimitBroadcast(FileManagerApplication.getAppContext(),Const.CMD_SHOW_SDCARD_INIT_FAIL);
        }

        return result;
    }

    private void sendLimitFileBroadcastByType(String folderType) {
        String currentAction = "";
        if(SdcardUtil.checkSDcardIsFull()){
            currentAction = Const.CMD_SHOW_SDCARD_FULL_LIMIT;
        }else{
            if(folderType.equals(Const.EVENT_DIR)){
                currentAction = Const.CMD_SHOW_REACH_EVENT_FILE_LIMIT;
            }else if(folderType.equals(Const.MANUAL_DIR)){
                currentAction = Const.CMD_SHOW_REACH_MANUAL_FILE_LIMIT;
            }else if(folderType.equals(Const.NORMAL_DIR)){
                currentAction = Const.CMD_SHOW_REACH_NORMAL_FILE_LIMIT;
            }else if(folderType.equals(Const.PARKING_DIR)){
                currentAction = Const.CMD_SHOW_REACH_PARKING_FILE_LIMIT;
            }else if(folderType.equals(Const.PICTURE_DIR)){
                currentAction = Const.CMD_SHOW_REACH_PICTURE_FILE_LIMIT;
            }else if(folderType.equals(Const.SYSTEM_DIR)){
                currentAction = Const.CMD_SHOW_REACH_SYSTEM_FILE_LIMIT;
            }
        }
        BroadcastUtils.sendLimitBroadcast(mContext,currentAction);
    }

    private void sendUnreachLimitFileBroadcastByType(String folderType) {
        String currentAction = "";
        if(!SdcardUtil.checkSDcardIsFull()){
            currentAction = Const.CMD_SHOW_UNREACH_SDCARD_FULL_LIMIT;
            BroadcastUtils.sendLimitBroadcast(mContext,currentAction);
        }
        if(folderType.equals(Const.EVENT_DIR)){
            currentAction = Const.CMD_SHOW_UNREACH_EVENT_FILE_LIMIT;
        }else if(folderType.equals(Const.MANUAL_DIR)){
            currentAction = Const.CMD_SHOW_UNREACH_MANUAL_FILE_LIMIT;
        }else if(folderType.equals(Const.NORMAL_DIR)){
            currentAction = Const.CMD_SHOW_UNREACH_NORMAL_FILE_LIMIT;
        }else if(folderType.equals(Const.PARKING_DIR)){
            currentAction = Const.CMD_SHOW_UNREACH_PARKING_FILE_LIMIT;
        }else if(folderType.equals(Const.PICTURE_DIR)){
            currentAction = Const.CMD_SHOW_UNREACH_PICTURE_FILE_LIMIT;
        }else if(folderType.equals(Const.SYSTEM_DIR)){
            currentAction = Const.CMD_SHOW_UNREACH_SYSTEM_FILE_LIMIT;
        }
        BroadcastUtils.sendLimitBroadcast(mContext,currentAction);
    }

    private int getCurrentType(String folderType) {
        int type = -1;
        if(folderType.equals(Const.EVENT_DIR)){
            type = Const.TYPE_EVENT_DIR;
        }else if(folderType.equals(Const.MANUAL_DIR)){
            type = Const.TYPE_MANUAL_DIR;
        }else if(folderType.equals(Const.NORMAL_DIR)){
            type = Const.TYPE_NORMAL_DIR;
        }else if(folderType.equals(Const.PARKING_DIR)){
            type = Const.TYPE_PARKING_DIR;
        }else if(folderType.equals(Const.PICTURE_DIR)){
            type = Const.TYPE_PICTURE_DIR;
        }else if(folderType.equals(Const.SYSTEM_DIR)){
            type = Const.TYPE_SYSTEM_DIR;
        }
        return type;
    }


}
