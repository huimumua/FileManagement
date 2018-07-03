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
public class AskeySettingsService extends Service {

    private static final String LOG_TAG = AskeySettingsService.class.getSimpleName();
    private ICommunication mCommunication;
    private ContentResolver contentResolver;
    private TelephonyManager mPhoneManager;
    private String imei;
    private String userTag = "_user";//用于拼接setting的key

    public AskeySettingsService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        contentResolver = FileManagerApplication.getAppContext().getContentResolver();
        mPhoneManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
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
                Logg.i(LOG_TAG, "init-> userId = " + userId);
                AskeySettingInitAsyncTask askeySettingInitAsyncTask = new AskeySettingInitAsyncTask();
                askeySettingInitAsyncTask.execute(userId);
            } else {
                Logg.e(LOG_TAG, "init-> userId = " + userId);
            }
        }

        @Override
        public void sync() throws RemoteException {
            //用户自己手动设置
            AskeySettingSyncTask askeySettingSyncTask = new AskeySettingSyncTask();
            askeySettingSyncTask.execute();
            Intent intent = new Intent();
            intent.setAction("jvcmodule.local.CommuicationService");
            intent.setPackage("com.askey.dvr.cdr7010.dashcam");
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }

        @Override
        public void write(String key, String value) throws RemoteException {
            //这里负责处理jvc后台处理的设置  需要改变 user 和 user*两个地方,key应该是带user后缀的那种

            //带后缀的
            Settings.Global.putInt(contentResolver, key, Integer.parseInt(value));
            //不带后缀的
            Settings.Global.putInt(contentResolver, key.substring(0, key.lastIndexOf("_")), Integer.parseInt(value));
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mCommunication = ICommunication.Stub.asInterface(service);
            try {
                mCommunication.settingsUpdateRequest(settingsJson());
                Log.d(LOG_TAG, "onServiceConnected: "+settingsJson());
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
            JSONObject setting = new JSONObject();
            setting.put("imei", getImei(mPhoneManager));
            setting.put("num", getIntSettingValue(AskeySettings.Global.SYSSET_USER_NUM, 1));
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

            for (int i = 1; i < 6; i++) {
                putUserNumSetting(setting, i);
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
        unbindService(mConnection);
        return super.onUnbind(intent);
    }

    private String getImei(TelephonyManager manager) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return "";
        }
        return manager.getDeviceId();
    }

    public static final String SYSSET_USER_NUM = "SYSSET_user_num";
    public static final String SYSSET_DEFAULT_USER = "SYSSET_default_user";
    public static final String SYSSET_SELECT_USER = "SYSSET_select_user";
    public static final String SYSSET_SELECT_USER_DAYS = "SYSSET_select_user_days";


    public static final String SYSSET_USER_ID = "SYSSET_user_id";
    public static final String SYSSET_USER_NAME = "SYSSET_user_name";
    public static final String ADAS_FCWS = "ADAS_FCWS";
    public static final String ADAS_LDS = "ADAS_LDS";
    public static final String ADAS_DELAY_START = "ADAS_delay_start";
    public static final String ADAS_PEDESTRIAN_COLLISION = "ADAS_pedestrian_collision";
    public static final String NOTIFY_REVERSE_RUN = "NOTIFY_reverse_run";
    public static final String NOTIFY_SPEED_LIMIT_AREA = "NOTIFY_speed_limit_area";
    public static final String NOTIFY_STOP = "NOTIFY_stop";
    public static final String NOTIFY_FREQ_ACCIDENT_AREA = "NOTIFY_freq_accident_area";
    public static final String NOTIFY_DRIVING_TIME = "NOTIFY_driving_time";
    public static final String NOTIFY_INTENSE_DRIVING = "NOTIFY_Intense_driving";
    public static final String NOTIFY_ABNORMAL_HANDING = "NOTIFY_abnormal_handing";
    public static final String NOTIFY_FLUCTUATION_DETECTION = "NOTIFY_fluctuation_detection";
    public static final String NOTIFY_OUT_OF_AREA = "NOTIFY_out_of_area";
    public static final String NOTIFY_DRIVING_REPORT = "NOTIFY_driving_report";
    public static final String NOTIFY_ADVICE = "NOTIFY_advice";
    public static final String NOTIFY_NOTIFICATION = "NOTIFY_notification";
    public static final String NOTIFY_WEATHER_INFO = "NOTIFY_weather_info";
    public static final String NOTIFY_ROAD_KILL = "NOTIFY_road_kill";
    public static final String NOTIFY_LOCATION_INFO = "NOTIFY_location_info";
    public static final String SYSSET_NOTIFY_VOL = "SYSSET_notify_vol";
    public static final String SYSSET_PLAYBACK_VOL = "SYSSET_playback_vol";
    public static final String SYSSET_MONITOR_BRIGHTNESS = "SYSSET_monitor_brightness";
    public static final String SYSSET_POWERSAVE_TIME = "SYSSET_powersave_time";
    public static final String SYSSET_POWERSAVE_ACTION = "SYSSET_powersave_action";
    public static final String SYSSET_LANGUAGE = "SYSSET_language";
    public static final String SYSSET_SET_LASTUPDATE_DAYS = "SYSSET_set_lastupdate_days";
    public static final String COMM_EMERGENCY_AUTO = "COMM_emergency_auto";


    public static final String SYSSET_USER_ID_USER1 = "SYSSET_user_id_user1";
    public static final String SYSSET_USER_NAME_USER1 = "SYSSET_user_name_user1";
    public static final String ADAS_FCWS_USER1 = "ADAS_FCWS_user1";
    public static final String ADAS_LDS_USER1 = "ADAS_LDS_user1";
    public static final String ADAS_DELAY_START_USER1 = "ADAS_delay_start_user1";
    public static final String ADAS_PEDESTRIAN_COLLISION_USER1 = "ADAS_pedestrian_collision_user1";
    public static final String NOTIFY_REVERSE_RUN_USER1 = "NOTIFY_reverse_run_user1";
    public static final String NOTIFY_SPEED_LIMIT_AREA_USER1 = "NOTIFY_speed_limit_area_user1";
    public static final String NOTIFY_STOP_USER1 = "NOTIFY_stop_user1";
    public static final String NOTIFY_FREQ_ACCIDENT_AREA_USER1 = "NOTIFY_freq_accident_area_user1";
    public static final String NOTIFY_DRIVING_TIME_USER1 = "NOTIFY_driving_time_user1";
    public static final String NOTIFY_INTENSE_DRIVING_USER1 = "NOTIFY_Intense_driving_user1";
    public static final String NOTIFY_ABNORMAL_HANDING_USER1 = "NOTIFY_abnormal_handing_user1";
    public static final String NOTIFY_FLUCTUATION_DETECTION_USER1 = "NOTIFY_fluctuation_detection_user1";
    public static final String NOTIFY_OUT_OF_AREA_USER1 = "NOTIFY_out_of_area_user1";
    public static final String NOTIFY_DRIVING_REPORT_USER1 = "NOTIFY_driving_report_user1";
    public static final String NOTIFY_ADVICE_USER1 = "NOTIFY_advice_user1";
    public static final String NOTIFY_NOTIFICATION_USER1 = "NOTIFY_notification_user1";
    public static final String NOTIFY_WEATHER_INFO_USER1 = "NOTIFY_weather_info_user1";
    public static final String NOTIFY_ROAD_KILL_USER1 = "NOTIFY_road_kill_user1";
    public static final String NOTIFY_LOCATION_INFO_USER1 = "NOTIFY_location_info_user1";
    public static final String SYSSET_NOTIFY_VOL_USER1 = "SYSSET_notify_vol_user1";
    public static final String SYSSET_PLAYBACK_VOL_USER1 = "SYSSET_playback_vol_user1";
    public static final String SYSSET_MONITOR_BRIGHTNESS_USER1 = "SYSSET_monitor_brightness_user1";
    public static final String SYSSET_POWERSAVE_TIME_USER1 = "SYSSET_powersave_time_user1";
    public static final String SYSSET_POWERSAVE_ACTION_USER1 = "SYSSET_powersave_action_user1";
    public static final String SYSSET_LANGUAGE_USER1 = "SYSSET_language_user1";
    public static final String SYSSET_SET_LASTUPDATE_DAYS_USER1 = "SYSSET_set_lastupdate_days_user1";
    public static final String COMM_EMERGENCY_AUTO_USER1 = "COMM_emergency_auto_user1";
}
