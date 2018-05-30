package com.askey.dvr.cdr7010.filemanagement.service;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.annotation.Nullable;

import com.askey.dvr.cdr7010.filemanagement.IAskeySettingsAidlInterface;
import com.askey.dvr.cdr7010.filemanagement.application.FileManagerApplication;
import com.askey.dvr.cdr7010.filemanagement.askeysettings.AskeySettingInitAsyncTask;
import com.askey.dvr.cdr7010.filemanagement.askeysettings.AskeySettingSyncTask;
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
public class AskeySettingsService extends Service {

    private static final String LOG_TAG = AskeySettingsService.class.getSimpleName();

    public AskeySettingsService() {

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
        }

        @Override
        public void write(String key, String value) throws RemoteException {
            //这里负责处理jvc后台处理的设置  需要改变 user 和 user*两个地方,key应该是带user后缀的那种
            ContentResolver contentResolver = FileManagerApplication.getAppContext().getContentResolver();
            //带后缀的
            Settings.Global.putInt(contentResolver, key, Integer.parseInt(value));
            //不带后缀的
            Settings.Global.putInt(contentResolver, key.substring(0, key.lastIndexOf("_")), Integer.parseInt(value));
        }


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


    public static final String SYSSET_USER_ID_USER2 = "SYSSET_user_id_user2";
    public static final String SYSSET_USER_NAME_USER2 = "SYSSET_user_name_user2";
    public static final String ADAS_FCWS_USER2 = "ADAS_FCWS_user2";
    public static final String ADAS_LDS_USER2 = "ADAS_LDS_user2";
    public static final String ADAS_DELAY_START_USER2 = "ADAS_delay_start_user2";
    public static final String ADAS_PEDESTRIAN_COLLISION_USER2 = "ADAS_pedestrian_collision_user2";
    public static final String NOTIFY_REVERSE_RUN_USER2 = "NOTIFY_reverse_run_user2";
    public static final String NOTIFY_SPEED_LIMIT_AREA_USER2 = "NOTIFY_speed_limit_area_user2";
    public static final String NOTIFY_STOP_USER2 = "NOTIFY_stop_user2";
    public static final String NOTIFY_FREQ_ACCIDENT_AREA_USER2 = "NOTIFY_freq_accident_area_user2";
    public static final String NOTIFY_DRIVING_TIME_USER2 = "NOTIFY_driving_time_user2";
    public static final String NOTIFY_INTENSE_DRIVING_USER2 = "NOTIFY_Intense_driving_user2";
    public static final String NOTIFY_ABNORMAL_HANDING_USER2 = "NOTIFY_abnormal_handing_user2";
    public static final String NOTIFY_FLUCTUATION_DETECTION_USER2 = "NOTIFY_fluctuation_detection_user2";
    public static final String NOTIFY_OUT_OF_AREA_USER2 = "NOTIFY_out_of_area_user2";
    public static final String NOTIFY_DRIVING_REPORT_USER2 = "NOTIFY_driving_report_user2";
    public static final String NOTIFY_ADVICE_USER2 = "NOTIFY_advice_user2";
    public static final String NOTIFY_NOTIFICATION_USER2 = "NOTIFY_notification_user2";
    public static final String NOTIFY_WEATHER_INFO_USER2 = "NOTIFY_weather_info_user2";
    public static final String NOTIFY_ROAD_KILL_USER2 = "NOTIFY_road_kill_user2";
    public static final String NOTIFY_LOCATION_INFO_USER2 = "NOTIFY_location_info_user2";
    public static final String SYSSET_NOTIFY_VOL_USER2 = "SYSSET_notify_vol_user2";
    public static final String SYSSET_PLAYBACK_VOL_USER2 = "SYSSET_playback_vol_user2";
    public static final String SYSSET_MONITOR_BRIGHTNESS_USER2 = "SYSSET_monitor_brightness_user2";
    public static final String SYSSET_POWERSAVE_TIME_USER2 = "SYSSET_powersave_time_user2";
    public static final String SYSSET_POWERSAVE_ACTION_USER2 = "SYSSET_powersave_action_user2";
    public static final String SYSSET_LANGUAGE_USER2 = "SYSSET_language_user2";
    public static final String SYSSET_SET_LASTUPDATE_DAYS_USER2 = "SYSSET_set_lastupdate_days_user2";
    public static final String COMM_EMERGENCY_AUTO_USER2 = "COMM_emergency_auto_user2";


    public static final String SYSSET_USER_ID_USER3 = "SYSSET_user_id_user3";
    public static final String SYSSET_USER_NAME_USER3 = "SYSSET_user_name_user3";
    public static final String ADAS_FCWS_USER3 = "ADAS_FCWS_user3";
    public static final String ADAS_LDS_USER3 = "ADAS_LDS_user3";
    public static final String ADAS_DELAY_START_USER3 = "ADAS_delay_start_user3";
    public static final String ADAS_PEDESTRIAN_COLLISION_USER3 = "ADAS_pedestrian_collision_user3";
    public static final String NOTIFY_REVERSE_RUN_USER3 = "NOTIFY_reverse_run_user3";
    public static final String NOTIFY_SPEED_LIMIT_AREA_USER3 = "NOTIFY_speed_limit_area_user3";
    public static final String NOTIFY_STOP_USER3 = "NOTIFY_stop_user3";
    public static final String NOTIFY_FREQ_ACCIDENT_AREA_USER3 = "NOTIFY_freq_accident_area_user3";
    public static final String NOTIFY_DRIVING_TIME_USER3 = "NOTIFY_driving_time_user3";
    public static final String NOTIFY_INTENSE_DRIVING_USER3 = "NOTIFY_Intense_driving_user3";
    public static final String NOTIFY_ABNORMAL_HANDING_USER3 = "NOTIFY_abnormal_handing_user3";
    public static final String NOTIFY_FLUCTUATION_DETECTION_USER3 = "NOTIFY_fluctuation_detection_user3";
    public static final String NOTIFY_OUT_OF_AREA_USER3 = "NOTIFY_out_of_area_user3";
    public static final String NOTIFY_DRIVING_REPORT_USER3 = "NOTIFY_driving_report_user3";
    public static final String NOTIFY_ADVICE_USER3 = "NOTIFY_advice_user3";
    public static final String NOTIFY_NOTIFICATION_USER3 = "NOTIFY_notification_user3";
    public static final String NOTIFY_WEATHER_INFO_USER3 = "NOTIFY_weather_info_user3";
    public static final String NOTIFY_ROAD_KILL_USER3 = "NOTIFY_road_kill_user3";
    public static final String NOTIFY_LOCATION_INFO_USER3 = "NOTIFY_location_info_user3";
    public static final String SYSSET_NOTIFY_VOL_USER3 = "SYSSET_notify_vol_user3";
    public static final String SYSSET_PLAYBACK_VOL_USER3 = "SYSSET_playback_vol_user3";
    public static final String SYSSET_MONITOR_BRIGHTNESS_USER3 = "SYSSET_monitor_brightness_user3";
    public static final String SYSSET_POWERSAVE_TIME_USER3 = "SYSSET_powersave_time_user3";
    public static final String SYSSET_POWERSAVE_ACTION_USER3 = "SYSSET_powersave_action_user3";
    public static final String SYSSET_LANGUAGE_USER3 = "SYSSET_language_user3";
    public static final String SYSSET_SET_LASTUPDATE_DAYS_USER3 = "SYSSET_set_lastupdate_days_user3";
    public static final String COMM_EMERGENCY_AUTO_USER3 = "COMM_emergency_auto_user3";


    public static final String SYSSET_USER_ID_USER4 = "SYSSET_user_id_user4";
    public static final String SYSSET_USER_NAME_USER4 = "SYSSET_user_name_user4";
    public static final String ADAS_FCWS_USER4 = "ADAS_FCWS_user4";
    public static final String ADAS_LDS_USER4 = "ADAS_LDS_user4";
    public static final String ADAS_DELAY_START_USER4 = "ADAS_delay_start_user4";
    public static final String ADAS_PEDESTRIAN_COLLISION_USER4 = "ADAS_pedestrian_collision_user4";
    public static final String NOTIFY_REVERSE_RUN_USER4 = "NOTIFY_reverse_run_user4";
    public static final String NOTIFY_SPEED_LIMIT_AREA_USER4 = "NOTIFY_speed_limit_area_user4";
    public static final String NOTIFY_STOP_USER4 = "NOTIFY_stop_user4";
    public static final String NOTIFY_FREQ_ACCIDENT_AREA_USER4 = "NOTIFY_freq_accident_area_user4";
    public static final String NOTIFY_DRIVING_TIME_USER4 = "NOTIFY_driving_time_user4";
    public static final String NOTIFY_INTENSE_DRIVING_USER4 = "NOTIFY_Intense_driving_user4";
    public static final String NOTIFY_ABNORMAL_HANDING_USER4 = "NOTIFY_abnormal_handing_user4";
    public static final String NOTIFY_FLUCTUATION_DETECTION_USER4 = "NOTIFY_fluctuation_detection_user4";
    public static final String NOTIFY_OUT_OF_AREA_USER4 = "NOTIFY_out_of_area_user4";
    public static final String NOTIFY_DRIVING_REPORT_USER4 = "NOTIFY_driving_report_user4";
    public static final String NOTIFY_ADVICE_USER4 = "NOTIFY_advice_user4";
    public static final String NOTIFY_NOTIFICATION_USER4 = "NOTIFY_notification_user4";
    public static final String NOTIFY_WEATHER_INFO_USER4 = "NOTIFY_weather_info_user4";
    public static final String NOTIFY_ROAD_KILL_USER4 = "NOTIFY_road_kill_user4";
    public static final String NOTIFY_LOCATION_INFO_USER4 = "NOTIFY_location_info_user4";
    public static final String SYSSET_NOTIFY_VOL_USER4 = "SYSSET_notify_vol_user4";
    public static final String SYSSET_PLAYBACK_VOL_USER4 = "SYSSET_playback_vol_user4";
    public static final String SYSSET_MONITOR_BRIGHTNESS_USER4 = "SYSSET_monitor_brightness_user4";
    public static final String SYSSET_POWERSAVE_TIME_USER4 = "SYSSET_powersave_time_user4";
    public static final String SYSSET_POWERSAVE_ACTION_USER4 = "SYSSET_powersave_action_user4";
    public static final String SYSSET_LANGUAGE_USER4 = "SYSSET_language_user4";
    public static final String SYSSET_SET_LASTUPDATE_DAYS_USER4 = "SYSSET_set_lastupdate_days_user4";
    public static final String COMM_EMERGENCY_AUTO_USER4 = "COMM_emergency_auto_user4";


    public static final String SYSSET_USER_ID_USER5 = "SYSSET_user_id_user5";
    public static final String SYSSET_USER_NAME_USER5 = "SYSSET_user_name_user5";
    public static final String ADAS_FCWS_USER5 = "ADAS_FCWS_user5";
    public static final String ADAS_LDS_USER5 = "ADAS_LDS_user5";
    public static final String ADAS_DELAY_START_USER5 = "ADAS_delay_start_user5";
    public static final String ADAS_PEDESTRIAN_COLLISION_USER5 = "ADAS_pedestrian_collision_user5";
    public static final String NOTIFY_REVERSE_RUN_USER5 = "NOTIFY_reverse_run_user5";
    public static final String NOTIFY_SPEED_LIMIT_AREA_USER5 = "NOTIFY_speed_limit_area_user5";
    public static final String NOTIFY_STOP_USER5 = "NOTIFY_stop_user5";
    public static final String NOTIFY_FREQ_ACCIDENT_AREA_USER5 = "NOTIFY_freq_accident_area_user5";
    public static final String NOTIFY_DRIVING_TIME_USER5 = "NOTIFY_driving_time_user5";
    public static final String NOTIFY_INTENSE_DRIVING_USER5 = "NOTIFY_Intense_driving_user5";
    public static final String NOTIFY_ABNORMAL_HANDING_USER5 = "NOTIFY_abnormal_handing_user5";
    public static final String NOTIFY_FLUCTUATION_DETECTION_USER5 = "NOTIFY_fluctuation_detection_user5";
    public static final String NOTIFY_OUT_OF_AREA_USER5 = "NOTIFY_out_of_area_user5";
    public static final String NOTIFY_DRIVING_REPORT_USER5 = "NOTIFY_driving_report_user5";
    public static final String NOTIFY_ADVICE_USER5 = "NOTIFY_advice_user5";
    public static final String NOTIFY_NOTIFICATION_USER5 = "NOTIFY_notification_user5";
    public static final String NOTIFY_WEATHER_INFO_USER5 = "NOTIFY_weather_info_user5";
    public static final String NOTIFY_ROAD_KILL_USER5 = "NOTIFY_road_kill_user5";
    public static final String NOTIFY_LOCATION_INFO_USER5 = "NOTIFY_location_info_user5";
    public static final String SYSSET_NOTIFY_VOL_USER5 = "SYSSET_notify_vol_user5";
    public static final String SYSSET_PLAYBACK_VOL_USER5 = "SYSSET_playback_vol_user5";
    public static final String SYSSET_MONITOR_BRIGHTNESS_USER5 = "SYSSET_monitor_brightness_user5";
    public static final String SYSSET_POWERSAVE_TIME_USER5 = "SYSSET_powersave_time_user5";
    public static final String SYSSET_POWERSAVE_ACTION_USER5 = "SYSSET_powersave_action_user5";
    public static final String SYSSET_LANGUAGE_USER5 = "SYSSET_language_user5";
    public static final String SYSSET_SET_LASTUPDATE_DAYS_USER5 = "SYSSET_set_lastupdate_days_user5";
    public static final String COMM_EMERGENCY_AUTO_USER5 = "COMM_emergency_auto_user5";


}
