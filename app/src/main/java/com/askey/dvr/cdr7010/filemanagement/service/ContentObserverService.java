package com.askey.dvr.cdr7010.filemanagement.service;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;

import com.askey.dvr.cdr7010.filemanagement.application.FileManagerApplication;
import com.askey.platform.AskeySettings;

public class ContentObserverService extends Service{

    private MyObserver myObserver;
    private ContentResolver contentResolver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        myObserver = new MyObserver(null);
        contentResolver = FileManagerApplication.getAppContext().getContentResolver();
        initSettingChangeObserver(contentResolver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        contentResolver.unregisterContentObserver(myObserver);
    }

    private void initSettingChangeObserver(ContentResolver contentResolver) {
        contentResolver.registerContentObserver(Settings.Global.getUriFor(AskeySettings.Global.SYSSET_NOTIFY_VOL), true, myObserver);
        contentResolver.registerContentObserver(Settings.Global.getUriFor(AskeySettings.Global.SYSSET_MONITOR_BRIGHTNESS), true, myObserver);
        contentResolver.registerContentObserver(Settings.Global.getUriFor(AskeySettings.Global.SYSSET_PLAYBACK_VOL), true, myObserver);
        contentResolver.registerContentObserver(Settings.Global.getUriFor(AskeySettings.Global.SYSSET_POWERSAVE_ACTION), true, myObserver);
        contentResolver.registerContentObserver(Settings.Global.getUriFor(AskeySettings.Global.SYSSET_POWERSAVE_TIME), true, myObserver);
        contentResolver.registerContentObserver(Settings.Global.getUriFor(AskeySettings.Global.RECSET_VOICE_RECORD), true, myObserver);
    }
}
