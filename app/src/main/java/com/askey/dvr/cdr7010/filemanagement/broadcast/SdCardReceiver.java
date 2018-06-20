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
import com.askey.dvr.cdr7010.filemanagement.controller.SDCardListener;
import com.askey.dvr.cdr7010.filemanagement.util.BroadcastUtils;
import com.askey.dvr.cdr7010.filemanagement.util.Const;
import com.askey.dvr.cdr7010.filemanagement.util.FileUtils;
import com.askey.dvr.cdr7010.filemanagement.util.Logg;
import com.askey.dvr.cdr7010.filemanagement.util.SdcardUtil;

import java.io.File;

public class SdCardReceiver extends BroadcastReceiver {

    private static final String TAG = SdCardReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Logg.i(TAG,"===action==="+action);
        if (action.equals(Intent.ACTION_MEDIA_MOUNTED)){// SD卡已经成功挂载
            Const.SDCARD_IS_EXIST = true;

        } else if (action.equals(Intent.ACTION_MEDIA_REMOVED)// 各种未挂载状态
                || action.equals(Intent.ACTION_MEDIA_UNMOUNTED)
                || action.equals(Intent.ACTION_MEDIA_BAD_REMOVAL)) {
            Const.SDCARD_IS_EXIST = false;
            Const.IS_SDCARD_FOLDER_LIMIT = false;
        } else if ( action.equals(Intent.ACTION_MEDIA_EJECT)) {
            Const.SDCARD_IS_EXIST = false;
            Const.SDCARD_INIT_SUCCESS=false;
            Const.IS_SDCARD_FULL_LIMIT = false;
            Const.IS_SDCARD_FOLDER_LIMIT = false;
            SDCardListener.getSingInstance(Const.SDCARD_PATH).stopWatche();

        }else if (action.equals(Intent.ACTION_MEDIA_SCANNER_STARTED)){//开始扫描
            boolean result = FileManager.getSingInstance().sdcardInit();
            Logg.i(TAG,"=sdcardInit=result=="+result);
            if(!result){
                Const.SDCARD_INIT_SUCCESS=false;
                BroadcastUtils.sendMyBroadcast(FileManagerApplication.getAppContext(),Const.ACTION_SDCARD_STATUS,Const.CMD_SHOW_SDCARD_INIT_FAIL);
            }else{
                Const.SDCARD_INIT_SUCCESS=true;
                BroadcastUtils.sendMyBroadcast(FileManagerApplication.getAppContext(),Const.ACTION_SDCARD_STATUS,Const.CMD_SHOW_SDCARD_INIT_SUCC);
            }
            SDCardListener.getSingInstance(Const.SDCARD_PATH).startWatche();

        }else if (action.equals(Intent.ACTION_MEDIA_SCANNER_FINISHED)){//扫描完成
            initSdcard(context);

        }else if (action.equals(Intent.ACTION_MEDIA_SHARED)){//扩展介质的挂载被解除 (unmount)。因为它已经作为 USB 大容量存储被共享

        }else {
            Logg.i(TAG, "=sdcard other status=");
        }
    }

    private void initSdcard(final Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Const.CURRENT_SDCARD_SIZE = SdcardUtil.getCurentSdcardInfo(context);

                if(SdcardUtil.checkSDcardIsFull()){
//                if(FileManager.getSingInstance().sdcardIsFull(Const.NORMAL_DIR)){
                    Const.IS_SDCARD_FULL_LIMIT = true;
                    String currentAction = Const.CMD_SHOW_SDCARD_FULL_LIMIT;
                    BroadcastUtils.sendLimitBroadcast(FileManagerApplication.getAppContext(),currentAction);
                }/*else{
                    String currentAction = Const.CMD_SHOW_SDCARD_FULL_LIMIT;
                    BroadcastUtils.sendLimitBroadcast(FileManagerApplication.getAppContext(),currentAction);
                }*/


            }
        }).start();
    }


}
