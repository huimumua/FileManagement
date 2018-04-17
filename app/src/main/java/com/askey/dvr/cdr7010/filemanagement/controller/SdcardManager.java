package com.askey.dvr.cdr7010.filemanagement.controller;

import android.content.Context;

import com.askey.dvr.cdr7010.filemanagement.SdcardInfo;
import com.askey.dvr.cdr7010.filemanagement.application.FileManagerApplication;

import java.util.ArrayList;
import java.util.List;

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

    public static List<SdcardInfo> getSdcardInfo(){
        ArrayList <SdcardInfo> sdcardInfoList= new ArrayList<SdcardInfo>();
        //这里要根据当前sdcard大小及规定的最大文件数量填赋值
        SdcardInfo sdcardInfo = new SdcardInfo();






        sdcardInfoList.add(sdcardInfo);
        return sdcardInfoList;
    }

}
