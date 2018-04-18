package com.askey.dvr.cdr7010.filemanagement.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 项目名称：filemanagement
 * 类描述：
 * 创建人：skysoft  charles.bai
 * 创建时间：2018/4/18 13:18
 * 修改人：skysoft
 * 修改时间：2018/4/18 13:18
 * 修改备注：
 */
public class DateUtil {
    private static final String TAG = "DateUtil";

    public static long getFileCreateTime(String name) {
        String time;
        if(name.contains("_2")){
            time = name.split("_2")[0];
        }else{
            time = name.substring(0,name.length()-4);
        }
        long  result = 0;
        try {
            result = Long.valueOf(dateToStamp(time));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return result;
    }

    // strTime要转换的String类型的时间
    // formatType时间格式
    // strTime的时间格式和formatType的时间格式必须相同
    public static long stringToLong(String strTime)
            throws ParseException {
        Date date = stringToDate(strTime); // String类型转成date类型
        if (date == null) {
            return 0;
        } else {
            long currentTime = dateToLong(date); // date类型转成long类型
            return currentTime;
        }
    }


    // strTime要转换的string类型的时间，formatType要转换的格式yyyy-MM-dd HH:mm:ss//yyyy年MM月dd日
    // HH时mm分ss秒，
    // strTime的时间格式必须要与formatType的时间格式相同
    public static Date stringToDate(String strTime)
            throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("YYYYMMDDhhmmss");
        Date date = null;
        date = formatter.parse(strTime);
        return date;
    }

    // date要转换的date类型的时间
    public static long dateToLong(Date date) {
        return date.getTime();
    }
    /*
       * 将时间转换为时间戳
       */
    public static String dateToStamp(String s) throws ParseException {
        String res;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = simpleDateFormat.parse(s);
        long ts = date.getTime();
        res = String.valueOf(ts);
        return res;
    }

}
