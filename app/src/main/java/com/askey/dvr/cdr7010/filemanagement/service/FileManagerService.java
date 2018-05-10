package com.askey.dvr.cdr7010.filemanagement.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import com.askey.dvr.cdr7010.filemanagement.IFileManagerAidlInterface;
import com.askey.dvr.cdr7010.filemanagement.ItemData;
import com.askey.dvr.cdr7010.filemanagement.SdcardInfo;
import com.askey.dvr.cdr7010.filemanagement.application.FileManagerApplication;
import com.askey.dvr.cdr7010.filemanagement.controller.FileManager;
import com.askey.dvr.cdr7010.filemanagement.controller.MediaScanner;
import com.askey.dvr.cdr7010.filemanagement.controller.SdcardManager;
import com.askey.dvr.cdr7010.filemanagement.util.BroadcastUtils;
import com.askey.dvr.cdr7010.filemanagement.util.Const;
import com.askey.dvr.cdr7010.filemanagement.util.FileUtils;
import com.askey.dvr.cdr7010.filemanagement.util.Logg;
import com.askey.dvr.cdr7010.filemanagement.util.SdcardUtil;
import com.askey.dvr.cdr7010.filemanagement.util.SdcardUtils;

import java.io.File;
import java.util.List;

public class FileManagerService extends Service {

    private static final String LOG_TAG = FileManagerService.class.getSimpleName();

    public FileManagerService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.

        Const.SDCARD_IS_EXIST = SdcardUtil.checkSdcardExist();

        if(Const.SDCARD_IS_EXIST){
            //获取sdcard状态信息
            Const.CURRENT_SDCARD_SIZE = SdcardUtil.getCurentSdcardInfo(FileManagerApplication.getAppContext());
            boolean result = FileManager.getSingInstance().sdcardInit();
            Logg.i(LOG_TAG,"=sdcardInit=result=="+result);
            //检测升级文件是否存在
            if(FileUtils.fileIsExists(Const.FOTA_PATH)){
                BroadcastUtils.sendMyBroadcast(FileManagerApplication.getAppContext(),
                        Const.ACTION_FOTA_STATUS,Const.CMD_SHOW_FOTA_FILE_EXIST);
            }
        }

        Intent startIntent = new Intent(this, SdcardService.class);
        startService(startIntent);

        return new MyBinder();
    }

    class MyBinder extends IFileManagerAidlInterface.Stub{

        @Override
        public String openSdcard(String filename, String folderType) throws RemoteException {
            String  result = null;
            if(Const.SDCARD_IS_EXIST){
                result = FileManager.getSingInstance().openSdcard(filename,folderType);
            }
            return result;
        }

        @Override
        public boolean closeSdcard() throws RemoteException {
            if(Const.SDCARD_IS_EXIST){
                boolean result = FileManager.getSingInstance().FH_Close();
                return result;
            }
            return false;
        }

        @Override
        public List<String> getAllFilesByType(String type) throws RemoteException {
            Logg.i(LOG_TAG,"====getAllFilesByType==="+type);
            if(Const.SDCARD_IS_EXIST){
                return MediaScanner.getAllFileList(type);
            }
            return null;
        }

        @Override
        public List<ItemData> getAllFileByType(String type) throws RemoteException {
            Logg.i(LOG_TAG,"====getAllFileByType==="+type);
            if(Const.SDCARD_IS_EXIST){
                List <ItemData>list =MediaScanner.getAllFiles(type);
                Logg.i(LOG_TAG,"list.size()==="+list.size());
                for (ItemData item :list){
                    List <ItemData>group= item.getDirFileItem();
                    Logg.i(LOG_TAG,"group.size()==="+group.size());
                    for (ItemData roupItem :group){
                        Logg.i(LOG_TAG,"roupItem.getFileName()=="+roupItem.getFileName());
                    }
                }
                return list;
            }
            return null;
        }


        @Override
        public boolean deleteFile(String path) throws RemoteException {
            if(Const.SDCARD_IS_EXIST){
                //jni提供的删除方法
                return MediaScanner.deleteFile(path);
            }
            return false;
        }

        @Override
        public boolean deleteFileByGroup(List<String> pathArray) throws RemoteException {
            if(Const.SDCARD_IS_EXIST){
                //jni提供的删除方法
                return MediaScanner.deleteFileByGroup(pathArray);
            }
            return false;
        }

        @Override
        public boolean deleteFileByFolder(String type) throws RemoteException {
            if(Const.SDCARD_IS_EXIST){
                return MediaScanner.deleteFileByFolder(type);
            }
            return false;
        }

        @Override
        public List<SdcardInfo> getSdcardInfo() throws RemoteException {
            Logg.i(LOG_TAG,"====getSdcardInfo===");
            if(Const.SDCARD_IS_EXIST){
                return SdcardManager.getSingInstance().getSdcardInfo();
            }
            return null;
        }

        @Override
        public boolean checkNewSystemVersion() throws RemoteException {
            Logg.i(LOG_TAG,"====checkNewSystemVersion===");
            if(Const.SDCARD_IS_EXIST){
                return FileUtils.fileIsExists(Const.FOTA_PATH);
            }
            return false;
        }

        @Override
        public boolean checkSdcardAvailable() throws RemoteException {
            Logg.i(LOG_TAG,"====checkSdcardAvailable===");
            if(Const.SDCARD_IS_EXIST){
                return SdcardUtils.sdcardAvailable();
            }
            return false;
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent stopIntent = new Intent(this, SdcardService.class);
        stopService(stopIntent);
        Logg.i(LOG_TAG, "onDestroy:");
    }

}
