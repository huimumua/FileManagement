package com.askey.dvr.cdr7010.filemanagement.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import com.askey.dvr.cdr7010.filemanagement.application.FileManagerApplication;
import com.askey.dvr.cdr7010.filemanagement.broadcast.SdCardReceiver;
import com.askey.dvr.cdr7010.filemanagement.util.Logg;
import com.askey.dvr.cdr7010.filemanagement.util.SdcardUtils;

public class SdcardService extends Service {

    private static final String TAG = "SdcardService";
    private  SdCardReceiver sdCardReceiver;

    public SdcardService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Logg.i(TAG, "service onCreate ...");
        // 在IntentFilter中选择你要监听的行为
//        SdcardUtils.registerStorageEventListener(FileManagerApplication.getAppContext());
        registerReceiver();

    }

    private void registerReceiver() {
        sdCardReceiver =new SdCardReceiver();
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_MEDIA_MOUNTED);// sd卡被插入，且已经挂载
        intentFilter.setPriority(1000);// 设置最高优先级
        intentFilter.addAction(Intent.ACTION_MEDIA_EJECT);//已拔掉外部大容量储存设备发出的广播（比如SD卡，或移动硬盘
        intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);// sd卡存在，但还没有挂载
        intentFilter.addAction(Intent.ACTION_MEDIA_REMOVED);// sd卡被移除
//        intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTABLE);// sd卡不被支持 损坏
        intentFilter.addAction(Intent.ACTION_MEDIA_SHARED);// sd卡作为 USB大容量存储被共享，挂载被解除
        intentFilter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);// sd卡已经从sd卡插槽拔出，但是挂载点还没解除
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);// 开始扫描
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);// 扫描完成
        intentFilter.addDataScheme("file");
        registerReceiver(sdCardReceiver, intentFilter);// 注册监听函数

        IntentFilter unmountfilter = new IntentFilter();
        unmountfilter.addAction(Intent.ACTION_MEDIA_UNMOUNTABLE);
        registerReceiver(sdCardReceiver, unmountfilter);// 注册监听函数

    }

    public void onDestroy() {
        Logg.i(TAG, "==onDestroy==");
//        SdcardUtils.unRegisterStorageEventListener(FileManagerApplication.getAppContext());
        unregisterReceiver(sdCardReceiver);//取消注册
    };

}
