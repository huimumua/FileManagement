package com.askey.dvr.cdr7010.filemanagement.util;

import android.os.Environment;

import java.io.File;

/**
 * 项目名称：filemanagement
 * 类描述：
 * 创建人：skysoft  charles.bai
 * 创建时间：2018/4/12 19:27
 * 修改人：skysoft
 * 修改时间：2018/4/12 19:27
 * 修改备注：
 */
public class SdcardUtil {

    private static final String TAG = "SdcardUtil";

    public String getSDPath(){
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(android.os.Environment.MEDIA_MOUNTED); //判断sd卡是否存在
        if (sdCardExist)
        {
            sdDir = Environment.getExternalStorageDirectory();//获取跟目录
        }
        return sdDir.toString();
    }

    public static boolean checkSdcardExist(){
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
        {
            return true;
        }
        return false;
    }

}
