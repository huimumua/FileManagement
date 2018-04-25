package com.askey.dvr.cdr7010.filemanagement.controller;

import android.content.Context;

import com.askey.dvr.cdr7010.filemanagement.application.FileManagerApplication;
import com.askey.dvr.cdr7010.filemanagement.util.Const;

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
    public native long FH_Open(String mount_path, String filename, String folderType);

    //
// Purpose: Close opened file
// Input: Opened FILE Pointer
// Output: bool, true = 1, false = 0;

//    public native boolean FH_Close(File file);
    public native boolean FH_Close(long filePointer);

    //
// Purpose: Move the data from cache to disc
// Input:  Opened FILE Pointer
// Output: bool, true = 1, false = 0;

//    public native boolean FH_Sync(File file);
    public native boolean FH_Sync(long filePointer);

    //
// Purpose: 1.Compare absolute_filepath, if have folderType String, rename file to Free folder
//          2.The file will be change to (number) + folderType extension
// Input:  mount path, Delete file absolute path
// Output: bool, true = 1, false = 0;
    public native boolean FH_Delete(String mount_path,String absolute_filepath);

    //
// Purpose: Finding the path oldest file ,and return absolute_filepath string
// Input:  finding folder path
// Output: oldest_filepath, ""
// ** Oldest file, means the file which is earliest modification time **

    public native String FH_FindOldest(String finding_path);

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
        boolean result = FH_Init(Const.SDCARD_PATH);
/*        if(!result){
            boolean ret = MediaScanner.deleteDirectory(Const.SDCARD_PATH);
            if(ret){
                result = FH_Init(Const.SDCARD_PATH);
            }
        }*/
        return result;
    }

    public long openSdcard(String filename, String folderType) {
        long result = FH_Open(Const.SDCARD_PATH,filename,folderType);
        if(result == -1){
            // 参数错误, sdcard满或者文件夹个数达到最大限制
            String finding_path = Const.SDCARD_PATH+Const.BACK_SLASH_1+folderType;
            String oldestPath = FH_FindOldest(finding_path);
           boolean deleteResult = MediaScanner.delete(oldestPath);
           if(deleteResult){
               result = FH_Open(Const.SDCARD_PATH,filename,folderType);
           }
        }
        return result;
    }

    public boolean closeSdcard(long filePointer) {
        return FH_Close(filePointer);
    }

}
