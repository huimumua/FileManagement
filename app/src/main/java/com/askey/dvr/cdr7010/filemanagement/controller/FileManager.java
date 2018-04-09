package com.askey.dvr.cdr7010.filemanagement.controller;

import android.content.Context;

import com.askey.dvr.cdr7010.filemanagement.application.FileManagerApplication;

/**
 * 项目名称：filemanagement
 * 类描述：
 * 创建人：skysoft  charles.bai
 * 创建时间：2018/4/9 17:40
 * 修改人：skysoft
 * 修改时间：2018/4/9 17:40
 * 修改备注：
 */
public class FileManager {

    private static final String LOG_TAG = FileManager.class.getSimpleName();
    private static FileManager instance;

    public FileManager(Context context){

    }

    public static FileManager getSingInstance() {
        if(instance == null)
            instance = new FileManager(FileManagerApplication.getAppContext());
        return instance;
    }



}
