package com.askey.dvr.cdr7010.filemanagement.askeysettings;

import android.content.ContentResolver;
import android.os.AsyncTask;
import android.provider.Settings;

import com.askey.dvr.cdr7010.filemanagement.application.FileManagerApplication;
import com.askey.platform.AskeySettings;

/**
 * 项目名称：filemanagement
 * 类描述：
 * 创建人：skysoft  charles.bai
 * 创建时间：2018/5/24 17:06
 * 修改人：skysoft
 * 修改时间：2018/5/24 17:06
 * 修改备注：
 */
public class AskeySettingInitAsyncTask extends AsyncTask<String, Integer, Boolean> {
    private static final String LOG_TAG = AskeySettingInitAsyncTask.class.getSimpleName();
    private String userId;
    private ContentResolver contentResolver;
    private int selectUser;

    public AskeySettingInitAsyncTask() {
        contentResolver = FileManagerApplication.getAppContext().getContentResolver();
        int defaultUser = Settings.Global.getInt(contentResolver, AskeySettings.Global.SYSSET_DEFAULT_USER, 0);
        selectUser = Settings.Global.getInt(contentResolver, AskeySettings.Global.SYSSET_SELECT_USER, 0);
    }

    @Override
    protected Boolean doInBackground(String... strings) {
        userId = strings[0];
        switch (userId) {
            case AskeySettings.Global.SYSSET_USER_ID_USER1:
                initSettings("_user1");
                break;
            case AskeySettings.Global.SYSSET_USER_ID_USER2:
                initSettings("_user2");
                break;
            case AskeySettings.Global.SYSSET_USER_ID_USER3:
                initSettings("_user3");
                break;
            case AskeySettings.Global.SYSSET_USER_ID_USER4:
                initSettings("_user4");
                break;
            case AskeySettings.Global.SYSSET_USER_ID_USER5:
                initSettings("_user5");
                break;
        }
        return null;
    }

    /*
        这个userId的格式必须为"_user1、2、3、4、5"
     */
    private void initSettings(String userId) {
        Settings.Global.putInt(contentResolver, AskeySettings.Global.SYSSET_SELECT_USER, Settings.Global.getInt(contentResolver, AskeySettings.Global.SYSSET_USER_ID + userId, 1));
        Settings.Global.putString(contentResolver, AskeySettings.Global.SYSSET_USER_NAME, Settings.Global.getString(contentResolver, AskeySettings.Global.SYSSET_USER_NAME + userId));
        Settings.Global.putInt(contentResolver, AskeySettings.Global.SYSSET_USER_ID, Settings.Global.getInt(contentResolver, AskeySettings.Global.SYSSET_USER_ID + userId, 1));
        Settings.Global.putInt(contentResolver, AskeySettings.Global.ADAS_FCWS, Settings.Global.getInt(contentResolver, AskeySettings.Global.ADAS_FCWS + userId, 1));
        Settings.Global.putInt(contentResolver, AskeySettings.Global.ADAS_LDS, Settings.Global.getInt(contentResolver, AskeySettings.Global.ADAS_LDS + userId, 1));
        Settings.Global.putInt(contentResolver, AskeySettings.Global.ADAS_DELAY_START, Settings.Global.getInt(contentResolver, AskeySettings.Global.ADAS_DELAY_START + userId, 1));
        Settings.Global.putInt(contentResolver, AskeySettings.Global.ADAS_PEDESTRIAN_COLLISION, Settings.Global.getInt(contentResolver, AskeySettings.Global.ADAS_PEDESTRIAN_COLLISION + userId, 0));
        Settings.Global.putInt(contentResolver, AskeySettings.Global.NOTIFY_REVERSE_RUN, Settings.Global.getInt(contentResolver, AskeySettings.Global.NOTIFY_REVERSE_RUN + userId, 1));
        Settings.Global.putInt(contentResolver, AskeySettings.Global.NOTIFY_SPEED_LIMIT_AREA, Settings.Global.getInt(contentResolver, AskeySettings.Global.NOTIFY_SPEED_LIMIT_AREA + userId, 1));
        Settings.Global.putInt(contentResolver, AskeySettings.Global.NOTIFY_STOP, Settings.Global.getInt(contentResolver, AskeySettings.Global.NOTIFY_STOP + userId, 1));
        Settings.Global.putInt(contentResolver, AskeySettings.Global.NOTIFY_FREQ_ACCIDENT_AREA, Settings.Global.getInt(contentResolver, AskeySettings.Global.NOTIFY_FREQ_ACCIDENT_AREA + userId, 0));
        Settings.Global.putInt(contentResolver, AskeySettings.Global.NOTIFY_DRIVING_TIME, Settings.Global.getInt(contentResolver, AskeySettings.Global.NOTIFY_DRIVING_TIME + userId, 1));
        Settings.Global.putInt(contentResolver, AskeySettings.Global.NOTIFY_INTENSE_DRIVING, Settings.Global.getInt(contentResolver, AskeySettings.Global.NOTIFY_INTENSE_DRIVING + userId, 1));
        Settings.Global.putInt(contentResolver, AskeySettings.Global.NOTIFY_ABNORMAL_HANDING, Settings.Global.getInt(contentResolver, AskeySettings.Global.NOTIFY_ABNORMAL_HANDING + userId, 0));
        Settings.Global.putInt(contentResolver, AskeySettings.Global.NOTIFY_FLUCTUATION_DETECTION, Settings.Global.getInt(contentResolver, AskeySettings.Global.NOTIFY_FLUCTUATION_DETECTION + userId, 1));
        Settings.Global.putInt(contentResolver, AskeySettings.Global.NOTIFY_OUT_OF_AREA, Settings.Global.getInt(contentResolver, AskeySettings.Global.NOTIFY_OUT_OF_AREA + userId, 0));
        Settings.Global.putInt(contentResolver, AskeySettings.Global.NOTIFY_DRIVING_REPORT, Settings.Global.getInt(contentResolver, AskeySettings.Global.NOTIFY_DRIVING_REPORT + userId, 1));
        Settings.Global.putInt(contentResolver, AskeySettings.Global.NOTIFY_ADVICE, Settings.Global.getInt(contentResolver, AskeySettings.Global.NOTIFY_ADVICE + userId, 1));
        Settings.Global.putInt(contentResolver, AskeySettings.Global.NOTIFY_NOTIFICATION, Settings.Global.getInt(contentResolver, AskeySettings.Global.NOTIFY_NOTIFICATION + userId, 1));
        Settings.Global.putInt(contentResolver, AskeySettings.Global.NOTIFY_WEATHER_INFO, Settings.Global.getInt(contentResolver, AskeySettings.Global.NOTIFY_WEATHER_INFO + userId, 1));
        Settings.Global.putInt(contentResolver, AskeySettings.Global.NOTIFY_ROAD_KILL, Settings.Global.getInt(contentResolver, AskeySettings.Global.NOTIFY_ROAD_KILL + userId, 1));
        Settings.Global.putInt(contentResolver, AskeySettings.Global.NOTIFY_LOCATION_INFO, Settings.Global.getInt(contentResolver, AskeySettings.Global.NOTIFY_LOCATION_INFO + userId, 0));
        Settings.Global.putInt(contentResolver, AskeySettings.Global.SYSSET_NOTIFY_VOL, Settings.Global.getInt(contentResolver, AskeySettings.Global.SYSSET_NOTIFY_VOL + userId, 3));
        Settings.Global.putInt(contentResolver, AskeySettings.Global.SYSSET_PLAYBACK_VOL, Settings.Global.getInt(contentResolver, AskeySettings.Global.SYSSET_PLAYBACK_VOL + userId, 3));
        Settings.Global.putInt(contentResolver, AskeySettings.Global.SYSSET_MONITOR_BRIGHTNESS, Settings.Global.getInt(contentResolver, AskeySettings.Global.SYSSET_MONITOR_BRIGHTNESS + userId, 5));
        Settings.Global.putInt(contentResolver, AskeySettings.Global.SYSSET_POWERSAVE_TIME, Settings.Global.getInt(contentResolver, AskeySettings.Global.SYSSET_POWERSAVE_TIME + userId, 10));
        Settings.Global.putInt(contentResolver, AskeySettings.Global.SYSSET_POWERSAVE_ACTION, Settings.Global.getInt(contentResolver, AskeySettings.Global.SYSSET_POWERSAVE_ACTION + userId, 0));
        Settings.Global.putInt(contentResolver, AskeySettings.Global.SYSSET_LANGUAGE, Settings.Global.getInt(contentResolver, AskeySettings.Global.SYSSET_LANGUAGE + userId, 0));
        Settings.Global.putString(contentResolver, AskeySettings.Global.SYSSET_SET_LASTUPDATE_DAYS, Settings.Global.getString(contentResolver, AskeySettings.Global.SYSSET_SET_LASTUPDATE_DAYS + userId));
        Settings.Global.putInt(contentResolver, AskeySettings.Global.COMM_EMERGENCY_AUTO, Settings.Global.getInt(contentResolver, AskeySettings.Global.COMM_EMERGENCY_AUTO + userId, 0));
    }

}