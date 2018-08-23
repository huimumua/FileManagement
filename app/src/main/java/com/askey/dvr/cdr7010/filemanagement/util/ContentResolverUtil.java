package com.askey.dvr.cdr7010.filemanagement.util;

import android.content.ContentResolver;
import android.provider.Settings;

import com.askey.dvr.cdr7010.filemanagement.application.FileManagerApplication;

/**
 * 项目名称：filemanagement
 * 类描述：
 * 创建人：skysoft  charles.bai
 * 创建时间：2018/8/23 13:38
 * 修改人：skysoft
 * 修改时间：2018/8/23 13:38
 * 修改备注：
 */
public class ContentResolverUtil {


    public static String getStringSettingValue(String key) {
        ContentResolver contentResolver = FileManagerApplication.getAppContext().getContentResolver();
        return Settings.Global.getString(contentResolver, key);
    }

    public static int getIntSettingValue(String key, int def) {
        ContentResolver contentResolver = FileManagerApplication.getAppContext().getContentResolver();
        return Settings.Global.getInt(contentResolver, key, def);
    }

    public static boolean setStringSettingValue(String key,String def) {
        ContentResolver contentResolver = FileManagerApplication.getAppContext().getContentResolver();
        return Settings.Global.putString(contentResolver,key,def);
    }

    public static boolean setIntSettingValue(String key, int def) {
        ContentResolver contentResolver = FileManagerApplication.getAppContext().getContentResolver();
        return Settings.Global.putInt(contentResolver, key, def);
    }

}
