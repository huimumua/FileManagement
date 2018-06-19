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

        Intent startIntent = new Intent(this, SdcardService.class);
        startService(startIntent);

        Const.SDCARD_IS_EXIST = SdcardUtil.checkSdcardExist();

        if(Const.SDCARD_IS_EXIST){
            //获取sdcard状态信息
            Const.CURRENT_SDCARD_SIZE = SdcardUtil.getCurentSdcardInfo(FileManagerApplication.getAppContext());
            Const.SDCARD_INIT_SUCCESS = FileManager.getSingInstance().sdcardInit();
            Logg.i(LOG_TAG,"=sdcardInit=result=="+Const.SDCARD_INIT_SUCCESS);
            //检测升级文件是否存在
            if(FileUtils.fileIsExists(Const.FOTA_PATH)){
                BroadcastUtils.sendMyBroadcast(FileManagerApplication.getAppContext(),
                        Const.ACTION_FOTA_STATUS,Const.CMD_SHOW_FOTA_FILE_EXIST);
            }
        }

        return new MyBinder();
    }

    class MyBinder extends IFileManagerAidlInterface.Stub{

        @Override
        public String openSdcard(String filename, String folderType) throws RemoteException {
            String  result = null;
            if(Const.SDCARD_IS_EXIST){
                result = FileManager.getSingInstance().openSdcard(filename,folderType);
                Logg.i(LOG_TAG,"====openSdcard==="+result);
            }else{
                Logg.e(LOG_TAG,"====SDCARD_IS_EXIST==="+Const.SDCARD_IS_EXIST);
            }
            return result;
        }

        @Override
        public boolean closeSdcard() throws RemoteException {
            if(Const.SDCARD_IS_EXIST){
                boolean result = FileManager.getSingInstance().FH_Close();
                Logg.i(LOG_TAG,"====closeSdcard==="+result);
                return result;
            }else{
                Logg.e(LOG_TAG,"====SDCARD_IS_EXIST==="+Const.SDCARD_IS_EXIST);
            }
            return false;
        }

        @Override
        public List<String> getAllFilesByType(String type) throws RemoteException {
            if(Const.SDCARD_IS_EXIST){
                Logg.i(LOG_TAG,"====getAllFilesByType==="+type);
                return MediaScanner.getAllFileList(type);
            }else{
                Logg.e(LOG_TAG,"====SDCARD_IS_EXIST==="+Const.SDCARD_IS_EXIST);
            }
            return null;
        }

        @Override
        public List<ItemData> getAllFileByType(String type) throws RemoteException {
            if(Const.SDCARD_IS_EXIST){
                Logg.i(LOG_TAG,"====getAllFileByType==="+type);
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
            }else{
                Logg.e(LOG_TAG,"====SDCARD_IS_EXIST==="+Const.SDCARD_IS_EXIST);
            }
            return null;
        }


        @Override
        public boolean deleteFile(String path) throws RemoteException {
            if(Const.SDCARD_IS_EXIST){
                //jni提供的删除方法
                boolean result = MediaScanner.deleteFile(path);
                Logg.i(LOG_TAG,"====deleteFile==="+result);
                if(result){
                    String type = FileManager.getSingInstance().getTypebyPath(path);
                    FileManager.getSingInstance().sendUnreachLimitFileBroadcastByType(type);
                }
                return result;
            }else{
                Logg.e(LOG_TAG,"====SDCARD_IS_EXIST==="+Const.SDCARD_IS_EXIST);
            }
            return false;
        }

        @Override
        public boolean deleteFileByGroup(List<String> pathArray) throws RemoteException {
            if(Const.SDCARD_IS_EXIST){
                //jni提供的删除方法
                boolean result = MediaScanner.deleteFileByGroup(pathArray);
                Logg.i(LOG_TAG,"====deleteFileByGroup==="+result);
                if(result){
                    String type = FileManager.getSingInstance().getTypebyPath(pathArray.get(0));
                    FileManager.getSingInstance().sendUnreachLimitFileBroadcastByType(type);
                }
                return result;
            }else{
                Logg.e(LOG_TAG,"====SDCARD_IS_EXIST==="+Const.SDCARD_IS_EXIST);
            }
            return false;
        }

        @Override
        public boolean deleteFileByFolder(String type) throws RemoteException {
            if(Const.SDCARD_IS_EXIST){
                boolean result = MediaScanner.deleteFileByFolder(type);
                Logg.i(LOG_TAG,"====deleteFileByFolder==="+result);
                if(result){
                    FileManager.getSingInstance().sendUnreachLimitFileBroadcastByType(type);
                }
                return result;
            }else{
                Logg.e(LOG_TAG,"====SDCARD_IS_EXIST==="+Const.SDCARD_IS_EXIST);
            }
            return false;
        }

        @Override
        public boolean FH_Sync() throws RemoteException {
            if(Const.SDCARD_IS_EXIST){
                boolean result = FileManager.getSingInstance().FH_Sync();
                Logg.i(LOG_TAG,"====FH_Sync==="+result);
                return result;
            }else{
                Logg.e(LOG_TAG,"====SDCARD_IS_EXIST==="+Const.SDCARD_IS_EXIST);
            }
            return false;
        }

        @Override
        public List<SdcardInfo> getSdcardInfo() throws RemoteException {
            if(Const.SDCARD_IS_EXIST){
                Logg.i(LOG_TAG,"====getSdcardInfo===");
                return SdcardManager.getSingInstance().getSdcardInfo();
            }else{
                Logg.e(LOG_TAG,"====SDCARD_IS_EXIST==="+Const.SDCARD_IS_EXIST);
            }
            return null;
        }

        @Override
        public boolean checkNewSystemVersion() throws RemoteException {
            if(Const.SDCARD_IS_EXIST){
                boolean result = FileUtils.fileIsExists(Const.FOTA_PATH);
                Logg.i(LOG_TAG,"====checkNewSystemVersion==="+result);
                return result;
            }else{
                Logg.e(LOG_TAG,"====SDCARD_IS_EXIST==="+Const.SDCARD_IS_EXIST);
            }
            return false;
        }

        @Override
        public boolean checkSdcardAvailable() throws RemoteException {
            if(Const.SDCARD_IS_EXIST){
                Boolean result = SdcardUtils.sdcardAvailable();
                Logg.i(LOG_TAG,"====checkSdcardAvailable==="+result);
                return result;
            }else{
                Logg.e(LOG_TAG,"====SDCARD_IS_EXIST==="+Const.SDCARD_IS_EXIST);
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
