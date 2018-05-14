package com.askey.dvr.cdr7010.filemanagement.util;

import android.net.ParseException;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 项目名称：filemanagement
 * 类描述：
 * 创建人：skysoft  charles.bai
 * 创建时间：2018/5/10 17:14
 * 修改人：skysoft
 * 修改时间：2018/5/10 17:14
 * 修改备注：
 */
public class TimeUtils {
    private static final String LOG_TAG = TimeUtils.class.getSimpleName();

    public static String getCurrentTime(){
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String date=sdf.format(new java.util.Date());
        return  date;
    }
    /***
     *根据时间字符串获取毫秒数
     */
    public static  long getTimeMillis(String strTime) {
        long returnMillis = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        Date d = null;
        try {
            try {
                d = sdf.parse(strTime);
            } catch (java.text.ParseException e) {
                e.printStackTrace();
            }
            returnMillis = d.getTime();
        } catch (ParseException e) {
            Logg.e(LOG_TAG,"==getTimeMillis=ParseException="+e.getMessage());
        }
        return returnMillis;
    }

    /**
     * 传入开始时间和结束时间字符串来计算消耗时长
     * */
    public static  String getTimeExpend(String startTime, String endTime){
        //传入字串类型 2016/06/28 08:30
        long longStart = getTimeMillis(startTime); //获取开始时间毫秒数
        long longEnd = getTimeMillis(endTime);  //获取结束时间毫秒数
        long longExpend = longEnd - longStart;  //获取时间差

        long longHours = longExpend / (60 * 60 * 1000); //根据时间差来计算小时数
        long longMinutes = (longExpend - longHours * (60 * 60 * 1000)) / (60 * 1000);   //根据时间差来计算分钟数

        return longHours + ":" + longMinutes;
    }

    /***
     *传入结束时间和消耗时长来计算开始时间
     * **/
    public static  String getTimeString(String endTime, String expendTime){
        //传入字串类型 end:2016/06/28 08:30 expend: 03:25
        long longEnd = getTimeMillis(endTime);
        String[] expendTimes = expendTime.split(":");   //截取出小时数和分钟数
        long longExpend = Long.parseLong(expendTimes[0]) * 60 * 60 * 1000 + Long.parseLong(expendTimes[1]) * 60 * 1000;
        SimpleDateFormat sdfTime = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        return sdfTime.format(new Date(longEnd - longExpend));
    }


}
