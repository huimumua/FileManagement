package com.askey.dvr.cdr7010.filemanagement.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.Formatter;

import com.askey.dvr.cdr7010.filemanagement.application.FileManagerApplication;
import com.askey.dvr.cdr7010.filemanagement.controller.FileManager;
import com.askey.dvr.cdr7010.filemanagement.controller.MediaScanner;
import com.askey.dvr.cdr7010.filemanagement.util.Const;
import com.askey.dvr.cdr7010.filemanagement.util.Logg;
import com.askey.dvr.cdr7010.filemanagement.util.SdcardUtil;

import java.io.File;

public class SdCardReceiver extends BroadcastReceiver {

    private static final String TAG = SdCardReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Logg.i(TAG,"===action==="+action);
        if (action.equals("android.intent.action.MEDIA_MOUNTED"))// SD
        // 卡已经成功挂载
        {
            Const.SDCARD_IS_EXIST = true;
            Logg.i(TAG, "我的卡已经成功挂载");

        } else if (action.equals("android.intent.action.MEDIA_REMOVED")// 各种未挂载状态
                || action.equals("android.intent.action.MEDIA_UNMOUNTED")
                || action.equals("android.intent.action.MEDIA_BAD_REMOVAL")) {
            Const.SDCARD_IS_EXIST = false;

            Logg.i(TAG, "我的各种未挂载状态");

//            Logg.i(TAG,"==Const.SDCARD_PATH=="+Const.SDCARD_PATH);
//            MediaScanner.scanDirAsync(FileManagerApplication.getAppContext(),Const.SDCARD_PATH);


        }else if (action.equals(Intent.ACTION_MEDIA_SCANNER_STARTED)){//开始扫描
            Logg.i(TAG, "开始扫描...");
//            Logg.i(TAG,"==Const.SDCARD_PATH=="+Const.SDCARD_PATH);
//            MediaScanner.scanDirAsync(FileManagerApplication.getAppContext(),Const.SDCARD_PATH);


        }else if (action.equals(Intent.ACTION_MEDIA_SCANNER_FINISHED)){//扫描完成
            Logg.i(TAG, "扫描完成...");
            initSdcard(context);

        }else if (action.equals(Intent.ACTION_MEDIA_SHARED)){//扩展介质的挂载被解除 (unmount)。因为它已经作为 USB 大容量存储被共享

            Logg.i(TAG, "USB 大容量存储被共享...");
        }else {

            Logg.i(TAG, "其他状态...");
        }
    }

    private void initSdcard(final Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Const.CURRENT_SDCARD_SIZE = SdcardUtil.getCurentSdcardInfo(context);
                boolean result = FileManager.getSingInstance().sdcardInit();
                Logg.i(TAG,"==result=="+result);
            }
        }).start();
    }


}
