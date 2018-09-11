package com.askey.dvr.cdr7010.filemanagement.service;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;

import com.askey.dvr.cdr7010.dashcam.ICommunication;
import com.askey.dvr.cdr7010.filemanagement.application.FileManagerApplication;
import com.askey.dvr.cdr7010.filemanagement.util.JsonUtil;
import com.askey.platform.AskeySettings;

import static android.provider.Settings.System.SCREEN_OFF_TIMEOUT;

public class MyObserver extends ContentObserver {
    private static final String TAG = "MyObserver";
    private static final String SYSTEM_NOTIFY_VOL = "content://settings/global/SYSSET_notify_vol";
    private static final String SYSTEM_PLAYBACK_VOL = "content://settings/global/SYSSET_playback_vol";
    private static final String SCREEN_BRIGHTNESS = "content://settings/global/SYSSET_monitor_brightness";
    private static final String SYSTEM_POWERSAVE_TIME = "content://settings/global/SYSSET_powersave_time";
    private static final String SYSTEM_POWERSAVE_ACTION = "content://settings/global/SYSSET_powersave_action";
    private static final String RECSET_VOCIE_RECORD = "content://settings/global/RECSET_voice_record";

    private ContentResolver contentResolver;
    private Context context;
    private AudioManager audioManager;
    private ICommunication mCommunication;

    /**
     * Creates a content observer.
     *
     * @param handler The handler to run {@link #onChange} on, or null if none.
     */
    MyObserver(Handler handler) {
        super(handler);
        contentResolver = FileManagerApplication.getAppContext().getContentResolver();
        context = FileManagerApplication.getAppContext();
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        super.onChange(selfChange, uri);
        switch (uri.toString()) {
            case SYSTEM_NOTIFY_VOL:
                if (null != audioManager) {
                    audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, Settings.Global.getInt(contentResolver, AskeySettings.Global.SYSSET_NOTIFY_VOL, 1), 0);
                }
                break;
            case SYSTEM_PLAYBACK_VOL:
                if (null != audioManager) {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, Settings.Global.getInt(contentResolver, AskeySettings.Global.SYSSET_PLAYBACK_VOL, 1), 0);
                }
                break;
            case SCREEN_BRIGHTNESS:
                setScreenBrightness(contentResolver, Settings.Global.getInt(contentResolver, AskeySettings.Global.SYSSET_MONITOR_BRIGHTNESS, 125) * 250 / 10);
                break;
            case SYSTEM_POWERSAVE_TIME:
                if (Settings.Global.getInt(contentResolver, AskeySettings.Global.SYSSET_POWERSAVE_ACTION, 0) == 0) {//Always ON
                    Settings.System.putInt(contentResolver, SCREEN_OFF_TIMEOUT, Integer.MAX_VALUE);
                } else if (Settings.Global.getInt(contentResolver, AskeySettings.Global.SYSSET_POWERSAVE_ACTION, 0) == 1) {//OFF
                    Settings.System.putInt(contentResolver, SCREEN_OFF_TIMEOUT, Settings.Global.getInt(contentResolver, AskeySettings.Global.SYSSET_POWERSAVE_TIME, 60) * 1000);
                    Settings.System.putInt(contentResolver, "screen_dim_timeout", 0);   //設成0表示要進入DIM且要關屏
                } else if (Settings.Global.getInt(contentResolver, AskeySettings.Global.SYSSET_POWERSAVE_ACTION, 0) == 2) {//Dim
                    Settings.System.putInt(contentResolver, SCREEN_OFF_TIMEOUT, Settings.Global.getInt(contentResolver, AskeySettings.Global.SYSSET_POWERSAVE_TIME, 60) * 1000);
                    Settings.System.putInt(contentResolver, "screen_dim_timeout", 1);   //設成1表示要進入DIM不關屏
                }
                break;
            case SYSTEM_POWERSAVE_ACTION:
                switch (Settings.Global.getInt(contentResolver, AskeySettings.Global.SYSSET_POWERSAVE_ACTION, 0)) {
                    case 0:
                        Settings.System.putInt(contentResolver, SCREEN_OFF_TIMEOUT, Integer.MAX_VALUE);
                        break;
                    case 1:
                        int monitorTime = Settings.Global.getInt(contentResolver, AskeySettings.Global.SYSSET_POWERSAVE_TIME, 10) * 1000;
                        Settings.System.putInt(contentResolver, SCREEN_OFF_TIMEOUT, monitorTime);
                        Settings.System.putInt(contentResolver, "screen_dim_timeout", 0);   //設成0表示要進入DIM且要關屏
                        break;
                    case 2:
                        int monitorTime1 = Settings.Global.getInt(contentResolver, AskeySettings.Global.SYSSET_POWERSAVE_TIME, 10) * 1000;
                        Settings.System.putInt(contentResolver, SCREEN_OFF_TIMEOUT, monitorTime1);
                        Settings.System.putInt(contentResolver, "screen_dim_timeout", 1);   //設成1表示要進入DIM不關屏
                        break;
                }
                break;
            case RECSET_VOCIE_RECORD:
                if (null == mCommunication) {
                    Intent intent = new Intent();
                    intent.setAction("jvcmodule.local.CommuicationService");
                    intent.setPackage("com.askey.dvr.cdr7010.dashcam");
                    context.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
                } else {
                    try {
                        Log.d(TAG, "onServiceConnected: " + JsonUtil.settingsJson(FileManagerApplication.getAppContext()));
                        mCommunication.settingsUpdateRequest(JsonUtil.settingsJson(context));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    private void setScreenBrightness(ContentResolver resolver, int brightness) {
        if (brightness > 255) {
            brightness = 255;
        } else if (brightness < 0) {
            brightness = 0;
        }
        //设置为手动调节模式
        Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        //保存到系统中
        Uri uri = android.provider.Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS);
        android.provider.Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS, brightness);
        resolver.notifyChange(uri, null);
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mCommunication = ICommunication.Stub.asInterface(service);
            try {
                mCommunication.settingsUpdateRequest(JsonUtil.settingsJson(FileManagerApplication.getAppContext()));
                Log.d(TAG, "onServiceConnected: " + JsonUtil.settingsJson(FileManagerApplication.getAppContext()));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
}
