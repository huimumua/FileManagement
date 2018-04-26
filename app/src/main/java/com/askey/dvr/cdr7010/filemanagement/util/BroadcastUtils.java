package com.askey.dvr.cdr7010.filemanagement.util;

import android.content.Context;
import android.content.Intent;

import java.util.Date;

/**
 * 项目名称：filemanagement
 * 类描述：
 * 创建人：skysoft  charles.bai
 * 创建时间：2018/4/24 13:53
 * 修改人：skysoft
 * 修改时间：2018/4/24 13:53
 * 修改备注：
 */
public class BroadcastUtils {

    private static final String TAG = "BroadcastUtils";


    public static void sendMyBroadcast(Context context , String action) {
        Intent intent = new Intent(action);
        context.sendBroadcast(intent);
    }

    public static void sendMyBroadcast(Context context , String action ,String broadcastStr) {
        Intent intent = new Intent(action);
        intent.putExtra("Data", broadcastStr);
        context.sendBroadcast(intent);
    }

    /**
     * 文件超过限制使用这个
     * */
    public static void sendLimitBroadcast(Context context , String cmd_ex) {
        Intent intent = new Intent();
        intent.setAction("com.askey.dvr.cdr7010.dashcam.limit");
        intent.putExtra("cmd_ex", cmd_ex);
        context.sendBroadcast(intent);
    }



}
