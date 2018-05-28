package com.askey.dvr.cdr7010.filemanagement.askeysettings;

import android.os.AsyncTask;

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
    private static final String LOG_TAG = AskeySettingSyncTask.class.getSimpleName();

    public AskeySettingSyncTask() {
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

