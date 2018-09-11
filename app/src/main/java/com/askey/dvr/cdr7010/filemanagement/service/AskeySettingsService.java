package com.askey.dvr.cdr7010.filemanagement.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;

import com.askey.dvr.cdr7010.dashcam.ICommunication;
import com.askey.dvr.cdr7010.filemanagement.IAskeySettingsAidlInterface;
import com.askey.dvr.cdr7010.filemanagement.application.FileManagerApplication;
import com.askey.dvr.cdr7010.filemanagement.askeysettings.AskeySettingInitAsyncTask;
import com.askey.dvr.cdr7010.filemanagement.askeysettings.AskeySettingSyncTask;
import com.askey.dvr.cdr7010.filemanagement.util.JsonUtil;
import com.askey.dvr.cdr7010.filemanagement.util.Logg;

/**
 * 项目名称：filemanagement
 * 类描述：
 * 创建人：skysoft  charles.bai
 * 创建时间：2018/5/24 13:43
 * 修改人：skysoft
 * 修改时间：2018/5/24 13:43
 * 修改备注：
 */
public class AskeySettingsService extends Service implements AskeySettingSyncTask.AskeySettingSyncCallback{

    private static final String TAG = AskeySettingsService.class.getSimpleName();
    private ICommunication mCommunication;
    private ContentResolver contentResolver;
    private boolean isSettingsBind = false;//判断setting是否绑定了服务

    public AskeySettingsService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        contentResolver = FileManagerApplication.getAppContext().getContentResolver();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new AskeySettingsBinder();
    }

    private class AskeySettingsBinder extends IAskeySettingsAidlInterface.Stub {
        @Override
        public void init(String userId) throws RemoteException {
            if (null != userId && !"".equals(userId)) {
                Logg.i(TAG, "init-> userId = " + userId);
                AskeySettingInitAsyncTask askeySettingInitAsyncTask = new AskeySettingInitAsyncTask();
                askeySettingInitAsyncTask.execute(userId);
            } else {
                Logg.e(TAG, "init-> userId = " + userId);
            }
        }

        @Override
        public void sync() throws RemoteException {
            //用户自己手动设置
            Log.d(TAG, "sync: ");
            AskeySettingSyncTask askeySettingSyncTask = new AskeySettingSyncTask(AskeySettingsService.this);
            askeySettingSyncTask.execute();
        }

        @Override
        public void write(String key, String value) throws RemoteException {
            //这里负责处理jvc后台处理的设置  需要改变 user 和 user*两个地方,key应该是带user后缀的那种，这里时间相关的需用String类型的，暂时没有修改

            //带后缀的
            Settings.Global.putInt(contentResolver, key, Integer.parseInt(value));
            //不带后缀的
            Settings.Global.putInt(contentResolver, key.substring(0, key.lastIndexOf("_")), Integer.parseInt(value));
        }
    }
    /*
        sync完成之后会回调这个方法
     */
    @Override
    public void syncSettingsCompleted() {
        if (null == mCommunication) {
            Intent intent = new Intent();
            intent.setAction("jvcmodule.local.CommuicationService");
            intent.setPackage("com.askey.dvr.cdr7010.dashcam");
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            return;
        }
        try {
            mCommunication.settingsUpdateRequest(JsonUtil.settingsJson(this));
            Log.d(TAG, "onServiceConnected_0: " + JsonUtil.settingsJson(this));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mCommunication = ICommunication.Stub.asInterface(service);
            try {
                isSettingsBind = true;
                mCommunication.settingsUpdateRequest(JsonUtil.settingsJson(AskeySettingsService.this));
                Log.d(TAG, "onServiceConnected_1: " + JsonUtil.settingsJson(AskeySettingsService.this));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind: ");
        if (isSettingsBind) {
            unbindService(mConnection);
        }
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }
}
