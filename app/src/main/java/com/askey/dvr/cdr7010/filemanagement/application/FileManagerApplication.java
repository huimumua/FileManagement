package com.askey.dvr.cdr7010.filemanagement.application;

import android.app.Application;
import android.content.Context;

/**
 * 项目名称：filemanagement
 * 类描述：
 * 创建人：skysoft  charles.bai
 * 创建时间：2018/4/9 18:16
 * 修改人：skysoft
 * 修改时间：2018/4/9 18:16
 * 修改备注：
 */
public class FileManagerApplication extends Application {

    private static final String TAG = FileManagerApplication.class.getSimpleName();
    private static FileManagerApplication instance;
    private static Context appContext;

    @Override
    public void onCreate() {
        super.onCreate();
        setInstance(this);
        setAppContext(this);

    }

    public static FileManagerApplication getInstance() {
        return instance;
    }

    private static void setInstance(FileManagerApplication instance) {
        FileManagerApplication.instance = instance;
    }

    public static Context getAppContext() {
        return appContext;
    }

    private static void setAppContext(Context appContext) {
        FileManagerApplication.appContext = appContext;
    }

}
