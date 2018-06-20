package com.askey.dvr.cdr7010.filemanagement.controller;

import android.os.FileObserver;
import android.support.annotation.Nullable;

import com.askey.dvr.cdr7010.filemanagement.application.FileManagerApplication;
import com.askey.dvr.cdr7010.filemanagement.util.BroadcastUtils;
import com.askey.dvr.cdr7010.filemanagement.util.Const;
import com.askey.dvr.cdr7010.filemanagement.util.Logg;

/**
 * 项目名称：filemanagement
 * 类描述：
 * 创建人：skysoft  charles.bai
 * 创建时间：2018/5/2 9:31
 * 修改人：skysoft
 * 修改时间：2018/5/2 9:31
 * 修改备注：
 */
public class SDCardListener extends FileObserver {
    private static final String LOG_TAG = SDCardListener.class.getSimpleName();
    private static SDCardListener listener;

    protected SDCardListener(String path) {
        super(path);
        if(listener != null){
            listener = new SDCardListener(path);
        }
    }

    public static SDCardListener getSingInstance(String path) {
        if(listener == null){
            listener = new SDCardListener(path);
        }
        return listener;
    }

    public void startWatche() {
        if(listener!=null){
            //开始监听
            listener.startWatching();
        }
    }

    public void stopWatche() {
        if(listener!=null){
            //停止监听
            listener.stopWatching();
            listener = null;
        }

    }

    @Override
    public void onEvent(int event, @Nullable String path) {
        switch(event) {
            case FileObserver.DELETE:
                //删除文件
                Logg.d(LOG_TAG, "==DELETE==path:"+ path);
                break;
            case FileObserver.DELETE_SELF:
                //删除文件
                Logg.d(LOG_TAG, "==DELETE_SELF==path:"+ path);
                break;
            case FileObserver.CREATE:
                //创建文件

                Logg.d(LOG_TAG, "==CREATE===path:"+ path);
                break;
            case FileObserver.CLOSE_WRITE:
                //写文件已经成功

                Logg.d(LOG_TAG, "==CLOSE_WRITE===path:"+ path);
                break;
            case FileObserver.CLOSE_NOWRITE:
                Logg.d(LOG_TAG, "==CLOSE_NOWRITE===path:"+ path);
                break;
            case FileObserver.MODIFY:
                break;
        }
    }


}
