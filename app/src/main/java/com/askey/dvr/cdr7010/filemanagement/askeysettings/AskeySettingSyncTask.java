package com.askey.dvr.cdr7010.filemanagement.askeysettings;

import android.content.ContentResolver;
import android.os.AsyncTask;
import android.provider.Settings;
import android.util.Log;

import com.askey.dvr.cdr7010.filemanagement.application.FileManagerApplication;
import com.askey.dvr.cdr7010.filemanagement.util.DateUtil;
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
public class AskeySettingSyncTask extends AsyncTask<Void, Integer, Boolean> {
    private static final String TAG = AskeySettingSyncTask.class.getSimpleName();
    private int selectUser;
    private ContentResolver contentResolver;
    private String userId;
    private AskeySettingSyncCallback askeySettingSyncCallback;

    public AskeySettingSyncTask(AskeySettingSyncCallback askeySettingSyncCallback) {

        contentResolver = FileManagerApplication.getAppContext().getContentResolver();
        selectUser = Settings.Global.getInt(contentResolver, AskeySettings.Global.SYSSET_SELECT_USER, 0);
        Log.d(TAG, "selectUser: "+selectUser);
        this.askeySettingSyncCallback = askeySettingSyncCallback;
    }


    @Override
    protected Boolean doInBackground(Void... voids) {
        if (selectUser == 1) {
            userId = "_user1";
        } else if (selectUser == 2) {
            userId = "_user2";
        } else if (selectUser == 3) {
            userId = "_user3";
        } else if (selectUser == 4) {
            userId = "_user4";
        } else if (selectUser == 5) {
            userId = "_user5";
        }
        syncSettings(userId);
        return null;
    }

    @Override
    protected void onPreExecute() {
        if (null != askeySettingSyncCallback) {
            askeySettingSyncCallback.syncSettingsCompleted();
        }
        super.onPreExecute();
    }

    /*
            这个userId的格式必须为"_user1、2、3、4、5"
         */
    private void syncSettings(String userId) {
        //每次sync都要把通用设置的时间更新一次
        Settings.Global.putString(contentResolver, AskeySettings.Global.SYSSET_SET_LASTUPDATE_DAYS, DateUtil.stamps2Time(System.currentTimeMillis()));

        Settings.Global.putString(contentResolver, AskeySettings.Global.SYSSET_USER_NAME + userId, Settings.Global.getString(contentResolver, AskeySettings.Global.SYSSET_USER_NAME));
        Settings.Global.putInt(contentResolver, AskeySettings.Global.SYSSET_USER_ID + userId, Settings.Global.getInt(contentResolver, AskeySettings.Global.SYSSET_USER_ID, 1));
        Settings.Global.putInt(contentResolver, AskeySettings.Global.ADAS_FCWS + userId, Settings.Global.getInt(contentResolver, AskeySettings.Global.ADAS_FCWS, 1));
        Settings.Global.putInt(contentResolver, AskeySettings.Global.ADAS_LDS + userId, Settings.Global.getInt(contentResolver, AskeySettings.Global.ADAS_LDS, 1));
        Settings.Global.putInt(contentResolver, AskeySettings.Global.ADAS_DELAY_START + userId, Settings.Global.getInt(contentResolver, AskeySettings.Global.ADAS_DELAY_START, 1));
        Settings.Global.putInt(contentResolver, AskeySettings.Global.ADAS_PEDESTRIAN_COLLISION + userId, Settings.Global.getInt(contentResolver, AskeySettings.Global.ADAS_PEDESTRIAN_COLLISION, 0));
        Settings.Global.putInt(contentResolver, AskeySettings.Global.NOTIFY_REVERSE_RUN + userId, Settings.Global.getInt(contentResolver, AskeySettings.Global.NOTIFY_REVERSE_RUN, 1));
        Settings.Global.putInt(contentResolver, AskeySettings.Global.NOTIFY_SPEED_LIMIT_AREA + userId, Settings.Global.getInt(contentResolver, AskeySettings.Global.NOTIFY_SPEED_LIMIT_AREA, 1));
        Settings.Global.putInt(contentResolver, AskeySettings.Global.NOTIFY_STOP + userId, Settings.Global.getInt(contentResolver, AskeySettings.Global.NOTIFY_STOP, 1));
        Settings.Global.putInt(contentResolver, AskeySettings.Global.NOTIFY_FREQ_ACCIDENT_AREA + userId, Settings.Global.getInt(contentResolver, AskeySettings.Global.NOTIFY_FREQ_ACCIDENT_AREA, 0));
        Settings.Global.putInt(contentResolver, AskeySettings.Global.NOTIFY_DRIVING_TIME + userId, Settings.Global.getInt(contentResolver, AskeySettings.Global.NOTIFY_DRIVING_TIME, 1));
        Settings.Global.putInt(contentResolver, AskeySettings.Global.NOTIFY_INTENSE_DRIVING + userId, Settings.Global.getInt(contentResolver, AskeySettings.Global.NOTIFY_INTENSE_DRIVING, 1));
        Settings.Global.putInt(contentResolver, AskeySettings.Global.NOTIFY_ABNORMAL_HANDING + userId, Settings.Global.getInt(contentResolver, AskeySettings.Global.NOTIFY_ABNORMAL_HANDING, 0));
        Settings.Global.putInt(contentResolver, AskeySettings.Global.NOTIFY_FLUCTUATION_DETECTION + userId, Settings.Global.getInt(contentResolver, AskeySettings.Global.NOTIFY_FLUCTUATION_DETECTION, 1));
        Settings.Global.putInt(contentResolver, AskeySettings.Global.NOTIFY_OUT_OF_AREA + userId, Settings.Global.getInt(contentResolver, AskeySettings.Global.NOTIFY_OUT_OF_AREA, 0));
        Settings.Global.putInt(contentResolver, AskeySettings.Global.NOTIFY_DRIVING_REPORT + userId, Settings.Global.getInt(contentResolver, AskeySettings.Global.NOTIFY_DRIVING_REPORT, 1));
        Settings.Global.putInt(contentResolver, AskeySettings.Global.NOTIFY_ADVICE + userId, Settings.Global.getInt(contentResolver, AskeySettings.Global.NOTIFY_ADVICE, 1));
        Settings.Global.putInt(contentResolver, AskeySettings.Global.NOTIFY_NOTIFICATION + userId, Settings.Global.getInt(contentResolver, AskeySettings.Global.NOTIFY_NOTIFICATION, 1));
        Settings.Global.putInt(contentResolver, AskeySettings.Global.NOTIFY_WEATHER_INFO + userId, Settings.Global.getInt(contentResolver, AskeySettings.Global.NOTIFY_WEATHER_INFO, 1));
        Settings.Global.putInt(contentResolver, AskeySettings.Global.NOTIFY_ROAD_KILL + userId, Settings.Global.getInt(contentResolver, AskeySettings.Global.NOTIFY_ROAD_KILL, 1));
        Settings.Global.putInt(contentResolver, AskeySettings.Global.NOTIFY_LOCATION_INFO + userId, Settings.Global.getInt(contentResolver, AskeySettings.Global.NOTIFY_LOCATION_INFO, 0));
        Settings.Global.putInt(contentResolver, AskeySettings.Global.SYSSET_NOTIFY_VOL + userId, Settings.Global.getInt(contentResolver, AskeySettings.Global.SYSSET_NOTIFY_VOL, 3));
        Settings.Global.putInt(contentResolver, AskeySettings.Global.SYSSET_PLAYBACK_VOL + userId, Settings.Global.getInt(contentResolver, AskeySettings.Global.SYSSET_PLAYBACK_VOL, 3));
        Settings.Global.putInt(contentResolver, AskeySettings.Global.SYSSET_MONITOR_BRIGHTNESS + userId, Settings.Global.getInt(contentResolver, AskeySettings.Global.SYSSET_MONITOR_BRIGHTNESS, 5));
        Settings.Global.putInt(contentResolver, AskeySettings.Global.SYSSET_POWERSAVE_TIME + userId, Settings.Global.getInt(contentResolver, AskeySettings.Global.SYSSET_POWERSAVE_TIME, 10));
        Settings.Global.putInt(contentResolver, AskeySettings.Global.SYSSET_POWERSAVE_ACTION + userId, Settings.Global.getInt(contentResolver, AskeySettings.Global.SYSSET_POWERSAVE_ACTION, 0));
        Settings.Global.putInt(contentResolver, AskeySettings.Global.SYSSET_LANGUAGE + userId, Settings.Global.getInt(contentResolver, AskeySettings.Global.SYSSET_LANGUAGE, 0));
        Settings.Global.putString(contentResolver, AskeySettings.Global.SYSSET_SET_LASTUPDATE_DAYS + userId, DateUtil.stamps2Time(System.currentTimeMillis()));
        Settings.Global.putInt(contentResolver, AskeySettings.Global.COMM_EMERGENCY_AUTO + userId, Settings.Global.getInt(contentResolver, AskeySettings.Global.COMM_EMERGENCY_AUTO, 0));
    }

    public interface AskeySettingSyncCallback {
        void syncSettingsCompleted();
    }

}

