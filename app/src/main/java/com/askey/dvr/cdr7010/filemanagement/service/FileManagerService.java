package com.askey.dvr.cdr7010.filemanagement.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import com.askey.dvr.cdr7010.filemanagement.IFileManagerAidlInterface;
import com.askey.dvr.cdr7010.filemanagement.ItemData;
import com.askey.dvr.cdr7010.filemanagement.controller.MediaScanner;
import com.askey.dvr.cdr7010.filemanagement.util.Const;
import com.askey.dvr.cdr7010.filemanagement.util.Logg;
import com.askey.dvr.cdr7010.filemanagement.util.SdcardUtil;

import java.util.List;

public class FileManagerService extends Service {

    private static final String LOG_TAG = FileManagerService.class.getSimpleName();

    public FileManagerService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.

        Intent startIntent = new Intent(this, SdcardService.class);
        startService(startIntent);

        Const.SDCARD_IS_EXIST = SdcardUtil.checkSdcardExist();
        return new MyBinder();
    }

    class MyBinder extends IFileManagerAidlInterface.Stub{

        @Override
        public boolean openSdcard() throws RemoteException {

            if(Const.SDCARD_IS_EXIST){

            }
            return false;
        }

        @Override
        public boolean closeSdcard() throws RemoteException {
            if(Const.SDCARD_IS_EXIST){

            }
            return false;
        }

        @Override
        public List<String> getAllFilesByType(String type) throws RemoteException {
            if(Const.SDCARD_IS_EXIST){
                return MediaScanner.getAllFileList(type);
            }
            return null;
        }


        @Override
        public List<ItemData> getAllFileByType(String type) throws RemoteException {
            if(Const.SDCARD_IS_EXIST){
                return MediaScanner.getAllFiles(type);
            }
            return null;
        }


        @Override
        public boolean deleteFile(String path) throws RemoteException {
            if(Const.SDCARD_IS_EXIST){
                return MediaScanner.deleteFile(path);
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

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent stopIntent = new Intent(this, SdcardService.class);
        stopService(stopIntent);
        Logg.i(LOG_TAG, "onDestroy:");
    }

}
