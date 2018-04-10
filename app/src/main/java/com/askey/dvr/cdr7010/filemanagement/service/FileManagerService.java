package com.askey.dvr.cdr7010.filemanagement.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import com.askey.dvr.cdr7010.filemanagement.IFileManagerAidlInterface;
import com.askey.dvr.cdr7010.filemanagement.controller.MediaScanner;
import com.askey.dvr.cdr7010.filemanagement.util.Logg;

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

        return new MyBinder();
    }

    class MyBinder extends IFileManagerAidlInterface.Stub{

        @Override
        public boolean openSdcard() throws RemoteException {

            return false;
        }

        @Override
        public boolean closeSdcard() throws RemoteException {

            return false;
        }

        @Override
        public List<String> getAllFilesByType(String type) throws RemoteException {

            return MediaScanner.getAllFileList(type);
        }

        @Override
        public boolean deleteFile(String path) throws RemoteException {

            return MediaScanner.deleteFile(path);
        }

        @Override
        public boolean deleteFileByFolder(String path) throws RemoteException {

            return MediaScanner.deleteDirectory(path);
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
