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
import com.askey.dvr.cdr7010.filemanagement.util.Const;
import com.askey.dvr.cdr7010.filemanagement.util.Logg;
import com.askey.dvr.cdr7010.filemanagement.util.SdcardUtil;

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

        Const.CURRENT_SDCARD_SIZE = SdcardUtil.getCurentSdcardInfo(FileManagerApplication.getAppContext());

        Intent startIntent = new Intent(this, SdcardService.class);
        startService(startIntent);


        return new MyBinder();
    }

    class MyBinder extends IFileManagerAidlInterface.Stub{

        @Override
        public long openSdcard(String mount_path, String filename, String folderType) throws RemoteException {
            long result = 0;
            if(Const.SDCARD_IS_EXIST){
                result = FileManager.getSingInstance().FH_Open(mount_path,filename,folderType);
            }
            return result;
        }

        @Override
        public boolean closeSdcard(long filePointer) throws RemoteException {
            if(Const.SDCARD_IS_EXIST){
                boolean result = FileManager.getSingInstance().FH_Close(filePointer);
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
        public boolean deleteFileByFolder(String path) throws RemoteException {
            if(Const.SDCARD_IS_EXIST){
                return MediaScanner.deleteDirectory(path);
            }
            return false;
        }

        @Override
        public List<SdcardInfo> getSdcardInfo() throws RemoteException {
            if(Const.SDCARD_IS_EXIST){

                return SdcardManager.getSingInstance().getSdcardInfo();
            }
            return null;
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
