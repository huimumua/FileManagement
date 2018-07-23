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
    private boolean sendOnce = true;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Logg.i(TAG,"===action==="+action);
        if (action.equals(Intent.ACTION_MEDIA_MOUNTED)){// SD卡已经成功挂载
//            SDCardListener.getSingInstance(Const.SDCARD_PATH).startWatche();
            sendOnce = true;
            initSdcard(context);
        } else if (action.equals(Intent.ACTION_MEDIA_REMOVED)// 各种未挂载状态
                || action.equals(Intent.ACTION_MEDIA_UNMOUNTED)
                || action.equals(Intent.ACTION_MEDIA_BAD_REMOVAL)
                || action.equals(Intent.ACTION_MEDIA_EJECT)
                ) {
            Const.SDCARD_IS_EXIST = false;
            Const.SDCARD_INIT_SUCCESS=false;
            Const.IS_SDCARD_FULL_LIMIT = false;
            Const.SDCARD_EVENT_FOLDER_LIMIT =false;
            Const.SDCARD_EVENT_FOLDER_OVER_LIMIT =false;
            Const.SDCARD_PICTURE_FOLDER_LIMIT =false;
            Const.SDCARD_PICTURE_FOLDER_OVER_LIMIT =false;
            Const.SDCARD_NOT_SUPPORTED = false;
            Const.SDCARD_UNRECOGNIZABLE = false;
            if(sendOnce){
                sendOnce = false;
                BroadcastUtils.sendMyBroadcast(FileManagerApplication.getAppContext(),Const.ACTION_SDCARD_STATUS,Const.CMD_SHOW_SDCARD_NOT_EXIST);
            }
//            SDCardListener.getSingInstance(Const.SDCARD_PATH).stopWatche();
        }else if(action.equals(Intent.ACTION_MEDIA_UNMOUNTABLE)
                || action.equals("android.intent.action.ASKEY_MEDIA_UNMOUNTABLE")){//用来判断sdcard是坏的
//            在收到android.intent.action.MEDIA_UNMOUNTABLE,取得fsType的值,若是ntfs就可判為不支持的卡,
//                    若fsType為其它情況(如空值或exfat)..,則該卡被判為異常.
            String fsType = intent.getStringExtra("fsType");
            Logg.i(TAG,"==fsType=="+fsType);
            if("ntfs".equals(fsType)){
                sendOnce = true;
                Const.SDCARD_NOT_SUPPORTED = true;
                BroadcastUtils.sendMyBroadcast(FileManagerApplication.getAppContext(),
                        Const.ACTION_SDCARD_STATUS,Const.CMD_SHOW_SDCARD_NOT_SUPPORTED);
            }else{
                Const.SDCARD_UNRECOGNIZABLE = true;
                BroadcastUtils.sendMyBroadcast(FileManagerApplication.getAppContext(),
                        Const.ACTION_SDCARD_STATUS,Const.CMD_SHOW_SDCARD_UNRECOGNIZABLE);
            }
        } else if (action.equals(Intent.ACTION_MEDIA_SCANNER_STARTED)){//开始扫描

        }else if (action.equals(Intent.ACTION_MEDIA_SCANNER_FINISHED)){//扫描完成

        }else if (action.equals(Intent.ACTION_MEDIA_SHARED)){//扩展介质的挂载被解除 (unmount)。因为它已经作为 USB 大容量存储被共享

        }else {
            Logg.i(TAG, "=sdcard other status=");
        }
    }

    private void initSdcard(final Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Const.SDCARD_IS_EXIST = true;
                BroadcastUtils.sendMyBroadcast(FileManagerApplication.getAppContext(),Const.ACTION_SDCARD_STATUS,Const.CMD_SHOW_SDCARD_MOUNTED);
                boolean result = FileManager.getSingInstance().sdcardInit();
                Logg.i(TAG,"=sdcardInit=result=="+result);
                if(!result){
                    Const.SDCARD_INIT_SUCCESS=false;
                    BroadcastUtils.sendMyBroadcast(FileManagerApplication.getAppContext(),Const.ACTION_SDCARD_STATUS,Const.CMD_SHOW_SDCARD_INIT_FAIL);
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Const.CURRENT_SDCARD_SIZE = SdcardUtil.getCurentSdcardInfo(context);

                boolean validFormat = FileManager.getSingInstance().validFormat();
                Logg.i(TAG,"validFormat-》"+validFormat);
                int sdcardStatus = FileManager.getSingInstance().checkFolderStatus(Const.EVENT_DIR);
                Logg.i(TAG,"checkFolderStatus-》"+sdcardStatus);
                if(sdcardStatus == Const.NO_SPACE_NO_NUMBER_TO_RECYCLE ){
                    Const.IS_SDCARD_FULL_LIMIT = true;
                    String currentAction = Const.CMD_SHOW_SDCARD_FULL_LIMIT;
                    BroadcastUtils.sendLimitBroadcast(FileManagerApplication.getAppContext(),currentAction);
                }else if(sdcardStatus == Const.FOLDER_SPACE_OVER_LIMIT || sdcardStatus == Const.EXIST_FILE_NUM_OVER_LIMIT ){
                    Const.SDCARD_EVENT_FOLDER_OVER_LIMIT = true;
                    Const.IS_SDCARD_FOLDER_LIMIT = true;
                    String currentAction = Const.CMD_SHOW_REACH_EVENT_FILE_OVER_LIMIT;
                    BroadcastUtils.sendLimitBroadcast(FileManagerApplication.getAppContext(),currentAction);
                }else if(sdcardStatus >=2){
                    Const.IS_SDCARD_FULL_LIMIT = false;
                    String currentAction = Const.CMD_SHOW_UNREACH_SDCARD_FULL_LIMIT;
                    BroadcastUtils.sendLimitBroadcast(FileManagerApplication.getAppContext(),currentAction);
                }

                if(!Const.SDCARD_EVENT_FOLDER_OVER_LIMIT && !Const.IS_SDCARD_FULL_LIMIT && result){
                    Const.SDCARD_INIT_SUCCESS=true;
                    BroadcastUtils.sendMyBroadcast(FileManagerApplication.getAppContext(),Const.ACTION_SDCARD_STATUS,Const.CMD_SHOW_SDCARD_INIT_SUCC);
                }

            }
        }).start();
    }

}
