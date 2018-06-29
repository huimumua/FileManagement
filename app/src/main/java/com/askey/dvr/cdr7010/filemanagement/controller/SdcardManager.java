package com.askey.dvr.cdr7010.filemanagement.controller;

import android.content.Context;

import com.askey.dvr.cdr7010.filemanagement.SdcardInfo;
import com.askey.dvr.cdr7010.filemanagement.application.FileManagerApplication;
import com.askey.dvr.cdr7010.filemanagement.util.Const;
import com.askey.dvr.cdr7010.filemanagement.util.Logg;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 项目名称：filemanagement
 * 类描述：
 * 创建人：skysoft  charles.bai
 * 创建时间：2018/4/17 18:25
 * 修改人：skysoft
 * 修改时间：2018/4/17 18:25
 * 修改备注：
 */
public class SdcardManager {

    private static final String LOG_TAG = SdcardManager.class.getSimpleName();
    private static SdcardManager instance;

    public SdcardManager(Context context){

    }

    public static SdcardManager getSingInstance() {
        if(instance == null){
            instance = new SdcardManager(FileManagerApplication.getAppContext());
        }
        return instance;
    }

    public List<SdcardInfo> getSdcardInfo(){
        Logg.i(LOG_TAG,"====CURRENT_SDCARD_SIZE==="+Const.CURRENT_SDCARD_SIZE);
        if(Const.SDCARD_IS_EXIST && Const.CURRENT_SDCARD_SIZE!=0){
            return getCurrentSdcardInfo(Const.CURRENT_SDCARD_SIZE);
        }
        return null;
    }

    private List<SdcardInfo> getCurrentSdcardInfo(int currentSdcardSize) {
        ArrayList <SdcardInfo> sdcardInfoList= new ArrayList<SdcardInfo>();
        //这里要根据当前sdcard大小及规定的最大文件数量填赋值
        SdcardInfo sdcardInfo = new SdcardInfo();
        sdcardInfo.setEventCurrentSize(currentSdcardSize+Const.SDCARD_SIZE_UNIT);
        Map fileCountMap = MediaScanner.getAllFileCount();
        sdcardInfo.setNormalCurrentSize(String.valueOf(fileCountMap.get(Const.NORMAL_DIR)));
        sdcardInfo.setEventCurrentSize(String.valueOf(fileCountMap.get(Const.EVENT_DIR)));
        sdcardInfo.setParkingCurrentSize(String.valueOf(fileCountMap.get(Const.PARKING_DIR)));
        List <String> pictureList = MediaScanner.getAllFileList(Const.PICTURE_DIR);
        sdcardInfo.setPictureCurrentSize(String.valueOf(pictureList.size()));
        switch (currentSdcardSize){
            case 4:
                sdcardInfo.setNormalSize(String.valueOf(Const.SDCARD_MAX_NORMAL_FILE_SIZE_4Gb));
                sdcardInfo.setEventSize(String.valueOf(Const.SDCARD_MAX_EVENT_FILE_SIZE_4Gb));
                sdcardInfo.setPictureSize(String.valueOf(Const.SDCARD_MAX_PICTURE_FILE_SIZE_4Gb));
                break;
            case 8:
                sdcardInfo.setNormalSize(String.valueOf(Const.SDCARD_MAX_NORMAL_FILE_SIZE_8Gb));
                sdcardInfo.setEventSize(String.valueOf(Const.SDCARD_MAX_EVENT_FILE_SIZE_8Gb));
                sdcardInfo.setPictureSize(String.valueOf(Const.SDCARD_MAX_PICTURE_FILE_SIZE_8Gb));
                break;
            case 16:
                sdcardInfo.setNormalSize(String.valueOf(Const.SDCARD_MAX_NORMAL_FILE_SIZE_16Gb));
                sdcardInfo.setEventSize(String.valueOf(Const.SDCARD_MAX_EVENT_FILE_SIZE_16Gb));
                sdcardInfo.setPictureSize(String.valueOf(Const.SDCARD_MAX_PICTURE_FILE_SIZE_16Gb));
                break;
            case 32:
                sdcardInfo.setNormalSize(String.valueOf(Const.SDCARD_MAX_NORMAL_FILE_SIZE_32Gb));
                sdcardInfo.setEventSize(String.valueOf(Const.SDCARD_MAX_EVENT_FILE_SIZE_32Gb));
                sdcardInfo.setPictureSize(String.valueOf(Const.SDCARD_MAX_PICTURE_FILE_SIZE_32Gb));
                break;
            case 64:
                sdcardInfo.setNormalSize(String.valueOf(Const.SDCARD_MAX_NORMAL_FILE_SIZE_64Gb));
                sdcardInfo.setEventSize(String.valueOf(Const.SDCARD_MAX_EVENT_FILE_SIZE_64Gb));
                sdcardInfo.setPictureSize(String.valueOf(Const.SDCARD_MAX_PICTURE_FILE_SIZE_64Gb));
                break;
            case 128:
                sdcardInfo.setNormalSize(String.valueOf(Const.SDCARD_MAX_NORMAL_FILE_SIZE_128Gb));
                sdcardInfo.setEventSize(String.valueOf(Const.SDCARD_MAX_EVENT_FILE_SIZE_128Gb));
                sdcardInfo.setPictureSize(String.valueOf(Const.SDCARD_MAX_PICTURE_FILE_SIZE_128Gb));
                break;
            case 256:
                sdcardInfo.setNormalSize(String.valueOf(Const.SDCARD_MAX_NORMAL_FILE_SIZE_256Gb));
                sdcardInfo.setEventSize(String.valueOf(Const.SDCARD_MAX_EVENT_FILE_SIZE_256Gb));
                sdcardInfo.setPictureSize(String.valueOf(Const.SDCARD_MAX_PICTURE_FILE_SIZE_256Gb));
                break;
            case 512:
                sdcardInfo.setNormalSize(String.valueOf(Const.SDCARD_MAX_NORMAL_FILE_SIZE_512Gb));
                sdcardInfo.setEventSize(String.valueOf(Const.SDCARD_MAX_EVENT_FILE_SIZE_512Gb));
                sdcardInfo.setPictureSize(String.valueOf(Const.SDCARD_MAX_PICTURE_FILE_SIZE_512Gb));
                break;
            default:
                sdcardInfo.setNormalSize(String.valueOf(Const.SDCARD_MAX_NORMAL_FILE_SIZE_8Gb));
                sdcardInfo.setEventSize(String.valueOf(Const.SDCARD_MAX_EVENT_FILE_SIZE_8Gb));
                sdcardInfo.setPictureSize(String.valueOf(Const.SDCARD_MAX_PICTURE_FILE_SIZE_8Gb));
                break;
        }
        sdcardInfoList.add(sdcardInfo);
        return sdcardInfoList;
    }

}
