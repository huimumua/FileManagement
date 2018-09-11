package com.askey.dvr.cdr7010.filemanagement.util;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;

import com.askey.dvr.cdr7010.filemanagement.application.FileManagerApplication;
import com.askey.platform.AskeySettings;

import org.json.JSONException;
import org.json.JSONObject;

public class JsonUtil {

    private static String userTag = "_user";
    private static ContentResolver contentResolver;


    /*
        组装上传的json
     */
    public static String settingsJson(Context context) {
        try {
            contentResolver = context.getContentResolver();
            int num = getIntSettingValue(AskeySettings.Global.SYSSET_USER_NUM, 1);
            JSONObject setting = new JSONObject();
            setting.put("imei", getImei((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)));
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

    private static void putUserNumSetting(JSONObject setting, int num) throws JSONException {
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

    private static String getStringSettingValue(String key) {
        return Settings.Global.getString(contentResolver, key);
    }

    private static int getIntSettingValue(String key, int def) {
        return Settings.Global.getInt(contentResolver, key, def);
    }

    private static String getImei(TelephonyManager manager) {
        if (ActivityCompat.checkSelfPermission(FileManagerApplication.getAppContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        return manager.getDeviceId() == null ? "null" : manager.getDeviceId();
    }
}
