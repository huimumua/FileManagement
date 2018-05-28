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
public class AskeySettingInitAsyncTask extends AsyncTask<Void, Integer, Boolean> {
    private static final String LOG_TAG = AskeySettingInitAsyncTask.class.getSimpleName();
    private String userId ;
    private ContentResolver contentResolver;

    public AskeySettingInitAsyncTask(String userId){
        contentResolver = FileManagerApplication.getAppContext().getContentResolver();
        int defaultUser = Settings.Global.getInt(contentResolver, AskeySettings.Global.SYSSET_DEFAULT_USER, 0);
        int selectUser = Settings.Global.getInt(contentResolver, AskeySettings.Global.SYSSET_SELECT_USER, 0);

        if(userId.equals(AskeySettings.Global.SYSSET_USER_ID_USER1)){

        }else if(userId.equals(AskeySettings.Global.SYSSET_USER_ID_USER2)){

        }else if(userId.equals(AskeySettings.Global.SYSSET_USER_ID_USER3)){

        }else if(userId.equals(AskeySettings.Global.SYSSET_USER_ID_USER4)){

        }else if(userId.equals(AskeySettings.Global.SYSSET_USER_ID_USER5)){

        }
    }


    @Override
    protected Boolean doInBackground(Void... voids) {
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(Boolean ready) {

        super.onPostExecute(ready);
    }

}