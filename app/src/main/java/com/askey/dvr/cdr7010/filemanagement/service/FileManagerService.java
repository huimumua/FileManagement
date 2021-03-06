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
        //启动监听器的service
        Intent observerIntent = new Intent(this, ContentObserverService.class);
        startService(observerIntent);

        Const.SDCARD_IS_EXIST = SdcardUtil.checkSdcardExist();
        if (Const.SDCARD_IS_EXIST) {
            //获取sdcard状态信息
            Const.CURRENT_SDCARD_SIZE = SdcardUtil.getCurentSdcardInfo(FileManagerApplication.getAppContext());
            int initResult = FileManager.getSingInstance().sdcardInit();
            if (initResult == Const.INIT_SUCCESS) {
                Logg.i(LOG_TAG, "=sdcardInit===" + initResult);

                int sdcardStatus = FileManager.getSingInstance().checkFolderStatus(Const.EVENT_DIR);
                Logg.i(LOG_TAG, "checkFolderStatus-EVENT》" + sdcardStatus);
                if (sdcardStatus == Const.NO_SPACE_NO_NUMBER_TO_RECYCLE) {
                    Const.IS_SDCARD_FULL_LIMIT = true;
                } else if (sdcardStatus == Const.EXIST_FILE_NUM_OVER_LIMIT) {
                    Const.SDCARD_EVENT_FOLDER_OVER_LIMIT = true;
                    Const.IS_SDCARD_FOLDER_LIMIT = true;
                } else if (sdcardStatus == Const.OPEN_FOLDER_ERROR || sdcardStatus == Const.GLOBAL_SDCARD_PATH_ERROR || sdcardStatus == Const.FOLDER_SPACE_OVER_LIMIT) {
                    Const.SDCARD_NOT_SUPPORTED = true;
                } else if (sdcardStatus >= 2) {
                    Const.IS_SDCARD_FULL_LIMIT = false;
                }

                int sdcardPictureStatus = FileManager.getSingInstance().checkFolderStatus(Const.PICTURE_DIR);
                Logg.i(LOG_TAG, "checkFolderStatus-PICTURE》" + sdcardPictureStatus);
                if (sdcardPictureStatus == Const.EXIST_FILE_NUM_OVER_LIMIT) {
                    Const.SDCARD_PICTURE_FOLDER_OVER_LIMIT = true;
                    Const.IS_SDCARD_FOLDER_LIMIT = true;
                }

                if (Const.SDCARD_EVENT_FOLDER_OVER_LIMIT && Const.SDCARD_PICTURE_FOLDER_OVER_LIMIT) {
                    Const.SDCARD_BOTH_EVENT_AND_PICTURE_FOLDER_OVER_LIMIT = true;
                }

                int sdcardNormalStatus = FileManager.getSingInstance().checkFolderStatus(Const.NORMAL_DIR);
                Logg.i(LOG_TAG, "checkFolderStatus-normal》" + sdcardNormalStatus);
                if (sdcardNormalStatus == Const.EXIST_FILE_NUM_OVER_LIMIT) {

                }

                int eventCurrentNum = FileManager.getSingInstance().FH_GetSDCardInfo(Const.TYPE_EVENT_DIR, Const.CURRENTNUM);
                int eventLimitNum = FileManager.getSingInstance().FH_GetSDCardInfo(Const.TYPE_EVENT_DIR, Const.LIMITNUM);
                if (eventCurrentNum == eventLimitNum) {
                    Const.SDCARD_EVENT_FOLDER_LIMIT = true;
                    Const.IS_SDCARD_FOLDER_LIMIT = true;
                }

                int pictureCurrentNum = FileManager.getSingInstance().FH_GetSDCardInfo(Const.TYPE_PICTURE_DIR, Const.CURRENTNUM);
                int pictureLimitNum = FileManager.getSingInstance().FH_GetSDCardInfo(Const.TYPE_PICTURE_DIR, Const.LIMITNUM);
                if (pictureCurrentNum == pictureLimitNum) {
                    Const.SDCARD_PICTURE_FOLDER_LIMIT = true;
                    Const.IS_SDCARD_FOLDER_LIMIT = true;
                }

                if (Const.SDCARD_EVENT_FOLDER_LIMIT && Const.SDCARD_PICTURE_FOLDER_LIMIT) {
                    Const.SDCARD_BOTH_EVENT_AND_PICTURE_FOLDER_LIMIT = true;
                }

                if (!Const.SDCARD_EVENT_FOLDER_OVER_LIMIT && !Const.IS_SDCARD_FULL_LIMIT && !Const.SDCARD_NOT_SUPPORTED && !Const.SDCARD_UNRECOGNIZABLE) {
                    Const.SDCARD_INIT_SUCCESS = true;
                }
            } else {
                if (initResult == Const.INIT_TABLE_VERSION_TOO_OLD || initResult == Const.INIT_TABLE_VERSION_CANNOT_RECOGNIZE
                        || initResult == Const.INIT_TABLE_READ_ERROR || initResult == Const.INIT_SDCARD_PATH_ERROR) {
                    Const.SDCARD_NOT_SUPPORTED = true;
                } else if (initResult == Const.INIT_SDCARD_SPACE_FULL) {
                    Const.IS_SDCARD_FULL_LIMIT = true;
                }
                Const.SDCARD_INIT_SUCCESS = false;
            }

        }

        return new MyBinder();
    }

    class MyBinder extends IFileManagerAidlInterface.Stub {

        @Override
        public String openSdcard(String filename, String folderType) throws RemoteException {
            String result = null;
            if (Const.SDCARD_IS_EXIST) {
                Logg.i("getRecoderPath", "====openSdcard=start==filename=" + filename + "==folderType==" + folderType);
                result = FileManager.getSingInstance().openSdcard(filename, folderType);
                Logg.i("getRecoderPath", "====openSdcard=end==filename=" + filename + "==folderType==" + folderType);
                Logg.i(LOG_TAG, "====openSdcard===" + result);
            } else {
                Logg.e(LOG_TAG, "====SDCARD_IS_EXIST===" + Const.SDCARD_IS_EXIST);
            }
            return result;
        }

        @Override
        public boolean closeSdcard() throws RemoteException {
            if (Const.SDCARD_IS_EXIST) {
                boolean result = FileManager.getSingInstance().FH_Close();
                Logg.i(LOG_TAG, "====closeSdcard===" + result);
                return result;
            } else {
                Logg.e(LOG_TAG, "====SDCARD_IS_EXIST===" + Const.SDCARD_IS_EXIST);
            }
            return false;
        }

        @Override
        public List<String> getAllFilesByType(String type) throws RemoteException {
            if (Const.SDCARD_IS_EXIST) {
                Logg.i(LOG_TAG, "====getAllFilesByType===" + type);
                List<String> allFileList = MediaScanner.getAllFileList(type);
                Logg.i(LOG_TAG, "list.size()===" + allFileList.size());
                return allFileList;
            } else {
                Logg.e(LOG_TAG, "====SDCARD_IS_EXIST===" + Const.SDCARD_IS_EXIST);
            }
            return null;
        }

        @Override
        public List<ItemData> getAllFileByType(String type) throws RemoteException {
            if (Const.SDCARD_IS_EXIST) {
                Logg.i(LOG_TAG, "====getAllFileByType===" + type);
                List<ItemData> list = MediaScanner.getAllFiles(type);
                Logg.i(LOG_TAG, "list.size()===" + list.size());
                return list;
            } else {
                Logg.e(LOG_TAG, "====SDCARD_IS_EXIST===" + Const.SDCARD_IS_EXIST);
            }
            return null;
        }


        @Override
        public boolean deleteFile(String path) throws RemoteException {
            if (Const.SDCARD_IS_EXIST) {
                //jni提供的删除方法
                boolean result = MediaScanner.deleteFile(path);
                Logg.i(LOG_TAG, "====deleteFile===" + result);
                if (result) {
                    String type = FileManager.getSingInstance().getTypebyPath(path);
                    FileManager.getSingInstance().sendUnreachLimitFileBroadcastByType(type);
                }
                return result;
            } else {
                Logg.e(LOG_TAG, "====SDCARD_IS_EXIST===" + Const.SDCARD_IS_EXIST);
            }
            return false;
        }

        @Override
        public boolean deleteFileByGroup(List<String> pathArray) throws RemoteException {
            if (Const.SDCARD_IS_EXIST) {
                //jni提供的删除方法
                boolean result = MediaScanner.deleteFileByGroup(pathArray);
                Logg.i(LOG_TAG, "====deleteFileByGroup===" + result);
                if (result) {
                    String type = FileManager.getSingInstance().getTypebyPath(pathArray.get(0));
                    FileManager.getSingInstance().sendUnreachLimitFileBroadcastByType(type);
                }
                return result;
            } else {
                Logg.e(LOG_TAG, "====SDCARD_IS_EXIST===" + Const.SDCARD_IS_EXIST);
            }
            return false;
        }

        @Override
        public boolean deleteFileByFolder(String type) throws RemoteException {
            if (Const.SDCARD_IS_EXIST) {
                boolean result = MediaScanner.deleteFileByFolder(type);
                Logg.i(LOG_TAG, "====deleteFileByFolder===" + result);
                if (result) {
                    FileManager.getSingInstance().sendUnreachLimitFileBroadcastByType(type);
                }
                return result;
            } else {
                Logg.e(LOG_TAG, "====SDCARD_IS_EXIST===" + Const.SDCARD_IS_EXIST);
            }
            return false;
        }

        @Override
        public boolean FH_Sync() throws RemoteException {
            if (Const.SDCARD_IS_EXIST) {
                FileManager.getSingInstance().FH_Sync();
                Logg.i(LOG_TAG, "====FH_Sync===");
                return true;
            } else {
                Logg.e(LOG_TAG, "====SDCARD_IS_EXIST===" + Const.SDCARD_IS_EXIST);
            }
            return false;
        }

        @Override
        public List<SdcardInfo> getSdcardInfo() throws RemoteException {
            if (Const.SDCARD_IS_EXIST) {
                Logg.i(LOG_TAG, "====getSdcardInfo===");
                return SdcardManager.getSingInstance().getSdcardInfo();
            } else {
                Logg.e(LOG_TAG, "====SDCARD_IS_EXIST===" + Const.SDCARD_IS_EXIST);
            }
            return null;
        }

        @Override
        public boolean checkNewSystemVersion() throws RemoteException {

            return false;
        }

        /**
         * show_sdcard_not_supported   0
         * show_sdcard_unrecognizable  1
         * show_sdcard_not_exist       2
         * show_sdcard_mounted         3
         * show_sdcard_init_success    4
         * show_sdcard_init_fail       5
         * show_reach_event_file_limit           6
         * show_reach_event_file_over_limit      7
         * show_reach_picture_file_limit         8
         * show_reach_picture_file_over_limit    9
         * show_sdcard_full_limit                10
         * show_sdcard_askey_not_supported       11
         * show_reach_event&picture_file_limit           12
         * show_reach_event&picture_file_over_limit      13
         */
        @Override
        public int checkSdcardAvailable() throws RemoteException {
            int sdcardStatus = getSdcardStatus();
            Logg.i(LOG_TAG, "=====sdcardStatus====" + sdcardStatus);
            return sdcardStatus;
        }

    }

    private int getSdcardStatus() {
        if (Const.SDCARD_IS_EXIST) {
            if (Const.SDCARD_NOT_SUPPORTED) {
                return 0;
            }
            if (Const.CURRENT_SDCARD_SIZE == -1) {
                return 11;
            }
            if (Const.IS_SDCARD_FULL_LIMIT) {
                return 10;
            }
            if (Const.SDCARD_BOTH_EVENT_AND_PICTURE_FOLDER_OVER_LIMIT) {
                return 13;
            }
            if (Const.SDCARD_EVENT_FOLDER_OVER_LIMIT) {
                return 7;
            }
            if (Const.SDCARD_BOTH_EVENT_AND_PICTURE_FOLDER_LIMIT) {
                return 12;
            }
            if (Const.SDCARD_PICTURE_FOLDER_OVER_LIMIT) {
                return 9;
            }
            if (Const.SDCARD_EVENT_FOLDER_LIMIT) {
                return 6;
            }
            if (Const.SDCARD_PICTURE_FOLDER_LIMIT) {
                return 8;
            }

            if (Const.SDCARD_INIT_SUCCESS) {
                return 4;
            } else {
                return 5;
            }
//                return 3;
        } else {
            if (Const.SDCARD_NOT_SUPPORTED) {
                return 0;
            }
            if (Const.SDCARD_UNRECOGNIZABLE) {
                return 1;
            }
            return 2;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent stopIntent = new Intent(this, SdcardService.class);
        stopService(stopIntent);
        Intent observerIntent = new Intent(this, ContentObserverService.class);
        stopService(observerIntent);
        Logg.i(LOG_TAG, "onDestroy:");
    }

}
