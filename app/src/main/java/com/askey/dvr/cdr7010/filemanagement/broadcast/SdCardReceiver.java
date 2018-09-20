package com.askey.dvr.cdr7010.filemanagement.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.askey.dvr.cdr7010.filemanagement.application.FileManagerApplication;
import com.askey.dvr.cdr7010.filemanagement.controller.FileManager;
import com.askey.dvr.cdr7010.filemanagement.util.BroadcastUtils;
import com.askey.dvr.cdr7010.filemanagement.util.Const;
import com.askey.dvr.cdr7010.filemanagement.util.Logg;
import com.askey.dvr.cdr7010.filemanagement.util.SdcardUtil;

public class SdCardReceiver extends BroadcastReceiver {

    private static final String TAG = SdCardReceiver.class.getSimpleName();
    private boolean sendOnce = true;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Logg.i(TAG,"===action==="+action);
        if(action.equals(Intent.ACTION_MEDIA_CHECKING)){
            Const.SDCARD_INSERTED = true;
        }else if (action.equals(Intent.ACTION_MEDIA_MOUNTED)){// SD卡已经成功挂载
//            SDCardListener.getSingInstance(Const.SDCARD_PATH).startWatche();
            sendOnce = true;
            initSdcard(context);
        } else if (action.equals(Intent.ACTION_MEDIA_REMOVED)// 各种未挂载状态
                || action.equals(Intent.ACTION_MEDIA_UNMOUNTED)
                || action.equals(Intent.ACTION_MEDIA_BAD_REMOVAL)
                || action.equals(Intent.ACTION_MEDIA_EJECT)
                ) {
            Const.SDCARD_INSERTED = false;

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
            Logg.i(TAG,"==fsType=="+fsType + ",   " + Const.SDCARD_IS_EXIST);
            if("ntfs".equals(fsType) /*|| "vfat".equals(fsType)*/ ){
                sendOnce = true;
                if(Const.SDCARD_INSERTED){ //add by Mark ,如果sd卡被拔出了就不更新该状态了
                    Const.SDCARD_NOT_SUPPORTED = true;
                    BroadcastUtils.sendMyBroadcast(FileManagerApplication.getAppContext(),
                            Const.ACTION_SDCARD_STATUS,Const.CMD_SHOW_SDCARD_NOT_SUPPORTED);
                }
            }else{
                sendOnce = true;
                if(Const.SDCARD_INSERTED) { //add by Mark ,如果sd卡被拔出了就不更新该状态了
                    Const.SDCARD_UNRECOGNIZABLE = true;
                    BroadcastUtils.sendMyBroadcast(FileManagerApplication.getAppContext(),
                            Const.ACTION_SDCARD_STATUS, Const.CMD_SHOW_SDCARD_UNRECOGNIZABLE);
                }
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
                if(initResult!=Const.INIT_SUCCESS){
                    Const.SDCARD_INIT_SUCCESS=false;
                    BroadcastUtils.sendMyBroadcast(FileManagerApplication.getAppContext(),Const.ACTION_SDCARD_STATUS,Const.CMD_SHOW_SDCARD_INIT_FAIL);
                    if(initResult == Const.INIT_TABLE_VERSION_TOO_OLD || initResult == Const.INIT_TABLE_VERSION_CANNOT_RECOGNIZE
                            || initResult == Const.INIT_TABLE_READ_ERROR  ||initResult == Const.INIT_SDCARD_PATH_ERROR ||initResult == Const.INIT_SDCARD_SIZE_NOT_SUPPORT){
                        Const.SDCARD_NOT_SUPPORTED = true;
                        BroadcastUtils.sendMyBroadcast(FileManagerApplication.getAppContext(), Const.ACTION_SDCARD_STATUS,Const.CMD_SHOW_SDCARD_NOT_SUPPORTED);
                    }else if(initResult == Const.INIT_SDCARD_SPACE_FULL){
                        Const.IS_SDCARD_FULL_LIMIT = true;
                        String currentAction = Const.CMD_SHOW_SDCARD_FULL_LIMIT;
                        BroadcastUtils.sendLimitBroadcast(FileManagerApplication.getAppContext(),currentAction);
                    }
                }else{
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
                    }else if( sdcardEventStatus == Const.EXIST_FILE_NUM_OVER_LIMIT ){
                        Const.SDCARD_EVENT_FOLDER_OVER_LIMIT = true;
                        Const.IS_SDCARD_FOLDER_LIMIT = true;
                    }else if(sdcardEventStatus == Const.OPEN_FOLDER_ERROR || sdcardEventStatus == Const.GLOBAL_SDCARD_PATH_ERROR || sdcardEventStatus == Const.FOLDER_SPACE_OVER_LIMIT){
                        Const.SDCARD_NOT_SUPPORTED = true;
                        BroadcastUtils.sendMyBroadcast(FileManagerApplication.getAppContext(),
                                Const.ACTION_SDCARD_STATUS,Const.CMD_SHOW_SDCARD_NOT_SUPPORTED);
                    }

                    FileManager.getSingInstance().checkEventAndPictureIsLimit();

                    int sdcardPictureStatus = FileManager.getSingInstance().checkFolderStatus(Const.PICTURE_DIR);
                    Logg.i(TAG,"checkFolderStatus-PICTURE》"+sdcardPictureStatus);
                    if(sdcardPictureStatus == Const.EXIST_FILE_NUM_OVER_LIMIT ){
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
                    if( sdcardNormalStatus == Const.EXIST_FILE_NUM_OVER_LIMIT ){

                    }

                    if(!Const.SDCARD_EVENT_FOLDER_OVER_LIMIT && !Const.IS_SDCARD_FULL_LIMIT && ! Const.SDCARD_NOT_SUPPORTED && ! Const.SDCARD_UNRECOGNIZABLE){
                        Const.SDCARD_INIT_SUCCESS=true;
                        if(!Const.SDCARD_EVENT_FOLDER_LIMIT && !Const.SDCARD_PICTURE_FOLDER_LIMIT){
                            BroadcastUtils.sendMyBroadcast(FileManagerApplication.getAppContext(),Const.ACTION_SDCARD_STATUS,Const.CMD_SHOW_SDCARD_INIT_SUCC);
                        }
                    }

                    if(sdcardEventStatus >=2){
                        Const.IS_SDCARD_FULL_LIMIT = false;
                        String currentAction = Const.CMD_SHOW_UNREACH_SDCARD_FULL_LIMIT;
                        BroadcastUtils.sendLimitBroadcast(FileManagerApplication.getAppContext(),currentAction);
                    }

                }
            }
        }).start();
    }



}
