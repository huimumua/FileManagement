package com.askey.dvr.cdr7010.filemanagement.service;

import android.Manifest;
import android.app.Service;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.askey.dvr.cdr7010.dashcam.ICommunication;
import com.askey.dvr.cdr7010.filemanagement.IAskeySettingsAidlInterface;
import com.askey.dvr.cdr7010.filemanagement.application.FileManagerApplication;
import com.askey.dvr.cdr7010.filemanagement.askeysettings.AskeySettingInitAsyncTask;
import com.askey.dvr.cdr7010.filemanagement.askeysettings.AskeySettingSyncTask;
import com.askey.dvr.cdr7010.filemanagement.util.Logg;
import com.askey.platform.AskeySettings;

import org.json.JSONException;
import org.json.JSONObject;

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
    private TelephonyManager mPhoneManager;
    private String imei;
    private String userTag = "_user";//用于拼接setting的key
    private boolean isSettingsBind = false;//判断setting是否绑定了服务
    private MyObserver myObserver;

    public AskeySettingsService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        myObserver = new MyObserver(null);
        contentResolver = FileManagerApplication.getAppContext().getContentResolver();
        mPhoneManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        initSettingChangeObserver(contentResolver);
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
            mCommunication.settingsUpdateRequest(settingsJson());
            Log.d(TAG, "onServiceConnected_0: " + settingsJson());
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
                mCommunication.settingsUpdateRequest(settingsJson());
                Log.d(TAG, "onServiceConnected_1: " + settingsJson());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    /*
        组装json字符串
     */
    private String settingsJson() {
        try {
            int num = getIntSettingValue(AskeySettings.Global.SYSSET_USER_NUM, 1);
            JSONObject setting = new JSONObject();
            setting.put("imei", getImei(mPhoneManager));
            setting.put("num", num);
            setting.put("selectuserid", getIntSettingValue(AskeySettings.Global.SYSSET_USER_ID, 1));
            setting.put("selectdate", getStringSettingValue(AskeySettings.Global.SYSSET_SELECT_USER_DAYS));

            JSONObject userCM = new JSONObject();
            userCM.put("rec_voice", getIntSettingValue(AskeySettings.Global.RECSET_VOICE_RECORD, 1));
            userCM.put("rec_info", getIntSettingValue(AskeySettings.Global.RECSET_INFO_STAMP, 1));
            userCM.put("2nd_camera", getIntSettingValue(AskeySettings.Global.SYSSET_2ND_CAMERA, 0));
            userCM.put("cartype", getIntSettingValue(AskeySettings.Global.CAR_TYPE, 2));
            userCM.put("position", getIntSettingValue(AskeySettings.Global.ADAS_MOUNT_POSITION, 1));
            userCM.put("lastupdate", getStringSettingValue(AskeySettings.Global.SYSSET_SET_LASTUPDATE_DAYS));

            setting.put("userCM", userCM);

            for (int i = 1; i < num + 1; i++) {
                putUserNumSetting(setting, i);//组装user1-5的json
            }

            return setting.toString();

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    private void putUserNumSetting(JSONObject setting, int num) throws JSONException {
        JSONObject userNum = new JSONObject();
        userNum.put("user_id_user", getIntSettingValue(AskeySettings.Global.SYSSET_USER_ID + userTag + num, num));
        userNum.put("user_name", getStringSettingValue(AskeySettings.Global.SYSSET_USER_NAME + userTag + num));
        userNum.put("warn_coll", getIntSettingValue(AskeySettings.Global.ADAS_FCWS + userTag + num, 1));
        userNum.put("warn_dev", getIntSettingValue(AskeySettings.Global.ADAS_LDS + userTag + num, 1));
        userNum.put("warn_delay", getIntSettingValue(AskeySettings.Global.ADAS_DELAY_START + userTag + num, 1));
        userNum.put("warn_pades", getIntSettingValue(AskeySettings.Global.ADAS_PEDESTRIAN_COLLISION + userTag + num, 0));
        userNum.put("reverse", getIntSettingValue(AskeySettings.Global.NOTIFY_REVERSE_RUN + userTag + num, 1));
        userNum.put("zone30", getIntSettingValue(AskeySettings.Global.NOTIFY_SPEED_LIMIT_AREA + userTag + num, 1));
        userNum.put("pause", getIntSettingValue(AskeySettings.Global.NOTIFY_STOP + userTag + num, 1));
        userNum.put("accident", getIntSettingValue(AskeySettings.Global.NOTIFY_FREQ_ACCIDENT_AREA + userTag + num, 0));
        userNum.put("runtime", getIntSettingValue(AskeySettings.Global.NOTIFY_DRIVING_TIME + userTag + num, 1));
        userNum.put("rapid", getIntSettingValue(AskeySettings.Global.NOTIFY_INTENSE_DRIVING + userTag + num, 1));
        userNum.put("handle", getIntSettingValue(AskeySettings.Global.NOTIFY_ABNORMAL_HANDING + userTag + num, 0));
        userNum.put("wobble", getIntSettingValue(AskeySettings.Global.NOTIFY_FLUCTUATION_DETECTION + userTag + num, 1));
        userNum.put("outside", getIntSettingValue(AskeySettings.Global.NOTIFY_OUT_OF_AREA + userTag + num, 0));
        userNum.put("report", getIntSettingValue(AskeySettings.Global.NOTIFY_DRIVING_REPORT + userTag + num, 1));
        userNum.put("advice", getIntSettingValue(AskeySettings.Global.NOTIFY_ADVICE + userTag + num, 1));
        userNum.put("notice", getIntSettingValue(AskeySettings.Global.NOTIFY_NOTIFICATION + userTag + num, 1));
        userNum.put("weather", getIntSettingValue(AskeySettings.Global.NOTIFY_WEATHER_INFO + userTag + num, 1));
        userNum.put("animal", getIntSettingValue(AskeySettings.Global.NOTIFY_ROAD_KILL + userTag + num, 1));
        userNum.put("location", getIntSettingValue(AskeySettings.Global.NOTIFY_LOCATION_INFO + userTag + num, 0));
        userNum.put("volume_n", getIntSettingValue(AskeySettings.Global.SYSSET_NOTIFY_VOL + userTag + num, 3));
        userNum.put("volume_p", getIntSettingValue(AskeySettings.Global.SYSSET_PLAYBACK_VOL + userTag + num, 3));
        userNum.put("bright", getIntSettingValue(AskeySettings.Global.SYSSET_MONITOR_BRIGHTNESS + userTag + num, 5));
        userNum.put("psave_s", getIntSettingValue(AskeySettings.Global.SYSSET_POWERSAVE_TIME + userTag + num, 10));
        userNum.put("psave_e", getIntSettingValue(AskeySettings.Global.SYSSET_POWERSAVE_ACTION + userTag + num, 0));
        userNum.put("lang", getIntSettingValue(AskeySettings.Global.SYSSET_LANGUAGE + userTag + num, 0));
        userNum.put("set_update_day", getStringSettingValue(AskeySettings.Global.SYSSET_SET_LASTUPDATE_DAYS + userTag + num));
        userNum.put("outbound_call", getIntSettingValue(AskeySettings.Global.COMM_EMERGENCY_AUTO + userTag + num, 1));
        userNum.put("lastupdate", getStringSettingValue(AskeySettings.Global.SYSSET_SET_LASTUPDATE_DAYS + userTag + num));
        setting.put("user0" + num, userNum);
    }

    private String getStringSettingValue(String key) {
        return Settings.Global.getString(contentResolver, key);
    }

    private int getIntSettingValue(String key, int def) {
        return Settings.Global.getInt(contentResolver, key, def);
    }

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
//        contentResolver.unregisterContentObserver(myObserver);
    }

    private String getImei(TelephonyManager manager) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        return manager.getDeviceId() == null ? "null" : manager.getDeviceId();
    }

    private void initSettingChangeObserver(ContentResolver contentResolver) {
        contentResolver.registerContentObserver(Settings.Global.getUriFor(AskeySettings.Global.SYSSET_NOTIFY_VOL), true, myObserver);
        contentResolver.registerContentObserver(Settings.Global.getUriFor(AskeySettings.Global.SYSSET_MONITOR_BRIGHTNESS), true, myObserver);
        contentResolver.registerContentObserver(Settings.Global.getUriFor(AskeySettings.Global.SYSSET_PLAYBACK_VOL), true, myObserver);
        contentResolver.registerContentObserver(Settings.Global.getUriFor(AskeySettings.Global.SYSSET_POWERSAVE_ACTION), true, myObserver);
        contentResolver.registerContentObserver(Settings.Global.getUriFor(AskeySettings.Global.SYSSET_POWERSAVE_TIME), true, myObserver);
    }
}
