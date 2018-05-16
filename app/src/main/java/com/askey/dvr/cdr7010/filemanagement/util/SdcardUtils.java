package com.askey.dvr.cdr7010.filemanagement.util;

import android.content.Context;

import com.askey.dvr.cdr7010.filemanagement.application.FileManagerApplication;
import com.askey.dvr.cdr7010.filemanagement.controller.FileManager;
import com.askey.platform.storage.AskeyStorageManager;
import com.askey.platform.storage.DiskInfo;
import com.askey.platform.storage.StorageEventListener;

/**
 * Created by test on 2017/3/2.
 */

public class SdcardUtils {
    private static final String LOG_TAG = SdcardUtils.class.getSimpleName();
    private static Context mContext;


    public static boolean isSDCardValid(Context context) {
        AskeyStorageManager storageManager =AskeyStorageManager.getInstance(context);
        for (DiskInfo disk :storageManager.getDisks()) {
            Logg.d(LOG_TAG, "isSDCardValid: disk " + disk.sysPath);
            if (disk.isSd()) {
                Logg.d(LOG_TAG, "isSDCardValid: sdcard disk, volumeCount = " + disk.volumeCount + ", size = " + disk.size);
                if (disk.volumeCount== 0 && disk.size > 0)
                    return false;

                return true;
            }
        }
        return false;
    }

    public static boolean sdcardAvailable() {
        // sdcard 存在  格式正确  且init成功才算可用
        if(isSDCardValid(FileManagerApplication.getAppContext())){
            return FileManager.getSingInstance().sdcardInit();
        }
        return false;
    }

    private static StorageEventListener mStorageEventListener = new StorageEventListener() {
        @Override
        public void onDiskScanned(DiskInfo disk, int volumeCount) {
            Logg.i(LOG_TAG, "onDiskScanned: "+ disk.toString());
            Logg.i(LOG_TAG, "onDiskScanned: volumeCount=" + volumeCount);
            if (disk.isSd()) {
                Logg.d(LOG_TAG, "onDiskScanned: sdcard disk, volumeCount = " + volumeCount + ", size = " + disk.size);
                if (volumeCount == 0 && disk.size > 0) {
                    // format
                    BroadcastUtils.sendMyBroadcast(FileManagerApplication.getAppContext(),
                            Const.ACTION_SDCARD_STATUS,Const.CMD_SHOW_SDCARD_NOT_SUPPORTED);
                }
            }
        }

        @Override
        public void onDiskDestroyed(DiskInfo disk) {
            Logg.d(LOG_TAG, "onDiskDestroyed: " + disk.toString());
            if (disk.isSd()) {
                BroadcastUtils.sendMyBroadcast(FileManagerApplication.getAppContext(),
                        Const.ACTION_SDCARD_STATUS,Const.CMD_SHOW_SDCARD_SUPPORTED);
            }
        }

        @Override
        public void onVolumeForgotten(String fsUuid) {
            Logg.i(LOG_TAG, "onVolumeForgotten: fsUuid=" + fsUuid);
            super.onVolumeForgotten(fsUuid);
        }

        @Override
        public void onStorageStateChanged(String path, String oldState, String newState) {
//            super.onStorageStateChanged(path, oldState, newState);
            Logg.i(LOG_TAG, "onStorageStateChanged: path=" + path);
            Logg.i(LOG_TAG, "onStorageStateChanged: oldState=" + oldState);
            Logg.i(LOG_TAG, "onStorageStateChanged: newState=" + newState);

            if(newState .equals("6")){
                // format
                BroadcastUtils.sendMyBroadcast(FileManagerApplication.getAppContext(),
                        Const.ACTION_SDCARD_STATUS,Const.CMD_SHOW_SDCARD_NOT_SUPPORTED);
//                dialog(mContext);
            }
        }

        @Override
        public void onUsbMassStorageConnectionChanged(boolean connected) {
            Logg.i(LOG_TAG, "onUsbMassStorageConnectionChanged: connected=" + connected);
            super.onUsbMassStorageConnectionChanged(connected);
        }


    };


    public static void registerStorageEventListener(Context context) {
        AskeyStorageManager storageManager =AskeyStorageManager.getInstance(context);
        mContext = context;
        storageManager.registerListener(mStorageEventListener);

    }

    public static void unRegisterStorageEventListener(Context context) {
        AskeyStorageManager storageManager =AskeyStorageManager.getInstance(context);
        storageManager.unregisterListener(mStorageEventListener);
    }




}
