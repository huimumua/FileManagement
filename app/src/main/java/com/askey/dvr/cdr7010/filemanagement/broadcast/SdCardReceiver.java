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
            Const.SDCARD_BOTH_EVENT_AND_PICTURE_FOLDER_LIMIT =false;
            Const.SDCARD_BOTH_EVENT_AND_PICTURE_FOLDER_OVER_LIMIT =false;
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
            if("ntfs".equals(fsType) /*|| "vfat".equals(fsType)*/ ){
                sendOnce = true;
                Const.SDCARD_NOT_SUPPORTED = true;
                BroadcastUtils.sendMyBroadcast(FileManagerApplication.getAppContext(),
                        Const.ACTION_SDCARD_STATUS,Const.CMD_SHOW_SDCARD_NOT_SUPPORTED);
            }else{
                sendOnce = true;
                Const.SDCARD_UNRECOGNIZABLE = true;
                BroadcastUtils.sendMyBroadcast(FileManagerApplication.getAppContext(),
                        Const.ACTION_SDCARD_STATUS,Const.CMD_SHOW_SDCARD_UNRECOGNIZABLE);
            }
        }
    }

    private void initSdcard(final Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Const.SDCARD_IS_EXIST = true;
                BroadcastUtils.sendMyBroadcast(FileManagerApplication.getAppContext(),Const.ACTION_SDCARD_STATUS,Const.CMD_SHOW_SDCARD_MOUNTED);
                int initResult = FileManager.getSingInstance().sdcardInit();
                Logg.i(TAG,"=sdcardInit=result=="+initResult);
                if(initResult!=0){
                    Const.SDCARD_INIT_SUCCESS=false;
                    BroadcastUtils.sendMyBroadcast(FileManagerApplication.getAppContext(),Const.ACTION_SDCARD_STATUS,Const.CMD_SHOW_SDCARD_INIT_FAIL);
                    if(initResult == -7 || initResult == -9 ){
                        Const.SDCARD_NOT_SUPPORTED = true;
                        BroadcastUtils.sendMyBroadcast(FileManagerApplication.getAppContext(), Const.ACTION_SDCARD_STATUS,Const.CMD_SHOW_SDCARD_NOT_SUPPORTED);
                    }else if(initResult == -2){
                        Const.SDCARD_UNRECOGNIZABLE = true;
                        BroadcastUtils.sendMyBroadcast(FileManagerApplication.getAppContext(),
                                Const.ACTION_SDCARD_STATUS,Const.CMD_SHOW_SDCARD_UNRECOGNIZABLE);
                    }
                }else{
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Const.CURRENT_SDCARD_SIZE = SdcardUtil.getCurentSdcardInfo(context);
                    if(Const.CURRENT_SDCARD_SIZE==-1){
                        BroadcastUtils.sendMyBroadcast(FileManagerApplication.getAppContext(),Const.ACTION_SDCARD_STATUS,Const.CMD_SHOW_SDCARD_ASKEY_NOT_SUPPORTED);
                    }
                    boolean validFormat = FileManager.getSingInstance().validFormat();
                    Logg.i(TAG,"validFormat-》"+validFormat);

                    int sdcardEventStatus = FileManager.getSingInstance().checkFolderStatus(Const.EVENT_DIR);
                    Logg.i(TAG,"checkFolderStatus-EVENT》"+sdcardEventStatus);
                    if(sdcardEventStatus == Const.NO_SPACE_NO_NUMBER_TO_RECYCLE ){
                        Const.IS_SDCARD_FULL_LIMIT = true;
                        String currentAction = Const.CMD_SHOW_SDCARD_FULL_LIMIT;
                        BroadcastUtils.sendLimitBroadcast(FileManagerApplication.getAppContext(),currentAction);
                    }else if(sdcardEventStatus == Const.FOLDER_SPACE_OVER_LIMIT || sdcardEventStatus == Const.EXIST_FILE_NUM_OVER_LIMIT ){
                        Const.SDCARD_EVENT_FOLDER_OVER_LIMIT = true;
                        Const.IS_SDCARD_FOLDER_LIMIT = true;
                    }else if(sdcardEventStatus == Const.OPEN_FOLDER_ERROR ){
                        Const.SDCARD_NOT_SUPPORTED = true;
                        BroadcastUtils.sendMyBroadcast(FileManagerApplication.getAppContext(),
                                Const.ACTION_SDCARD_STATUS,Const.CMD_SHOW_SDCARD_NOT_SUPPORTED);
                    }else if(sdcardEventStatus == Const.SDCARD_PATH_ERROR ){
                        Const.SDCARD_UNRECOGNIZABLE = true;
                        BroadcastUtils.sendMyBroadcast(FileManagerApplication.getAppContext(),
                                Const.ACTION_SDCARD_STATUS,Const.CMD_SHOW_SDCARD_UNRECOGNIZABLE);
                    }else if(sdcardEventStatus >=2){
                        Const.IS_SDCARD_FULL_LIMIT = false;
                        String currentAction = Const.CMD_SHOW_UNREACH_SDCARD_FULL_LIMIT;
                        BroadcastUtils.sendLimitBroadcast(FileManagerApplication.getAppContext(),currentAction);
                    }

                    int sdcardPictureStatus = FileManager.getSingInstance().checkFolderStatus(Const.PICTURE_DIR);
                    Logg.i(TAG,"checkFolderStatus-PICTURE》"+sdcardPictureStatus);
                    if(sdcardPictureStatus == Const.FOLDER_SPACE_OVER_LIMIT || sdcardPictureStatus == Const.EXIST_FILE_NUM_OVER_LIMIT ){
                        Const.SDCARD_PICTURE_FOLDER_OVER_LIMIT = true;
                        Const.IS_SDCARD_FOLDER_LIMIT = true;
                    }

                    if(Const.SDCARD_EVENT_FOLDER_OVER_LIMIT && Const.SDCARD_PICTURE_FOLDER_OVER_LIMIT){
                        Const.SDCARD_BOTH_EVENT_AND_PICTURE_FOLDER_OVER_LIMIT =true;
                        String currentAction = Const.CMD_SHOW_BOTH_EVENT_AND_PICTURE_FOLDER_OVER_LIMIT;
                        BroadcastUtils.sendLimitBroadcast(FileManagerApplication.getAppContext(),currentAction);
                    }else if(!Const.SDCARD_EVENT_FOLDER_OVER_LIMIT && Const.SDCARD_PICTURE_FOLDER_OVER_LIMIT){
                        String currentAction = Const.CMD_SHOW_REACH_PICTURE_FILE_OVER_LIMIT;
                        BroadcastUtils.sendLimitBroadcast(FileManagerApplication.getAppContext(),currentAction);
                    }else if(Const.SDCARD_EVENT_FOLDER_OVER_LIMIT && !Const.SDCARD_PICTURE_FOLDER_OVER_LIMIT){
                        String currentAction = Const.CMD_SHOW_REACH_EVENT_FILE_OVER_LIMIT;
                        BroadcastUtils.sendLimitBroadcast(FileManagerApplication.getAppContext(),currentAction);
                    }

                    int sdcardNormalStatus = FileManager.getSingInstance().checkFolderStatus(Const.NORMAL_DIR);
                    Logg.i(TAG,"checkFolderStatus-normal》"+sdcardNormalStatus);
                    if(sdcardNormalStatus == Const.FOLDER_SPACE_OVER_LIMIT || sdcardNormalStatus == Const.EXIST_FILE_NUM_OVER_LIMIT ){

                    }

                    int eventCurrentNum = FileManager.getSingInstance().FH_GetSDCardInfo(Const.TYPE_EVENT_DIR,Const.CURRENTNUM);
                    int eventLimitNum = FileManager.getSingInstance().FH_GetSDCardInfo(Const.TYPE_EVENT_DIR,Const.LIMITNUM);
                    if(eventCurrentNum == eventLimitNum){
                        Const.IS_SDCARD_FOLDER_LIMIT = true;
                        Const.SDCARD_EVENT_FOLDER_LIMIT =true;
                    }

                    int pictureCurrentNum = FileManager.getSingInstance().FH_GetSDCardInfo(Const.TYPE_PICTURE_DIR,Const.CURRENTNUM);
                    int pictureLimitNum = FileManager.getSingInstance().FH_GetSDCardInfo(Const.TYPE_PICTURE_DIR,Const.LIMITNUM);
                    if(pictureCurrentNum == pictureLimitNum){
                        Const.IS_SDCARD_FOLDER_LIMIT = true;
                        Const.SDCARD_PICTURE_FOLDER_LIMIT =true;
                    }

                    if(Const.SDCARD_EVENT_FOLDER_LIMIT && Const.SDCARD_PICTURE_FOLDER_LIMIT){
                        Const.SDCARD_BOTH_EVENT_AND_PICTURE_FOLDER_LIMIT =true;
                        String currentAction = Const.CMD_SHOW_BOTH_EVENT_AND_PICTURE_FOLDER_LIMIT;
                        BroadcastUtils.sendLimitBroadcast(FileManagerApplication.getAppContext(),currentAction);
                    }else if(Const.SDCARD_EVENT_FOLDER_LIMIT && !Const.SDCARD_PICTURE_FOLDER_LIMIT){
                        String currentAction = Const.CMD_SHOW_REACH_EVENT_FILE_LIMIT;
                        BroadcastUtils.sendLimitBroadcast(FileManagerApplication.getAppContext(),currentAction);
                    }

                    if(!Const.SDCARD_EVENT_FOLDER_OVER_LIMIT && !Const.IS_SDCARD_FULL_LIMIT && ! Const.SDCARD_NOT_SUPPORTED && ! Const.SDCARD_UNRECOGNIZABLE){
                        Const.SDCARD_INIT_SUCCESS=true;
                        BroadcastUtils.sendMyBroadcast(FileManagerApplication.getAppContext(),Const.ACTION_SDCARD_STATUS,Const.CMD_SHOW_SDCARD_INIT_SUCC);
                    }
                }

            }
        }).start();
    }

}
