package com.askey.dvr.cdr7010.filemanagement.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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

    /**
     * 获取文件的创建时间  DATE_ADDED可能不准确有待讨论
     * */
    public static long getFileCreateTime(String name) {
        Logg.i(TAG,"=====name==1111==="+name);
        if(name.contains("_UNKNOWN")){
            name = name.replace("_UNKNOWN","");
        }
        Logg.i(TAG,"=====name==2222==="+name);
        String time;
        if(name.contains("_2")){
            time = name.split("_2")[0];
        }else{
            time = name.substring(0,name.length()-4);
        }
        long  result = 0;
        try {
            if(time.length()==12){
                int year = Integer.valueOf(name.substring(0,2));
                if(year>=70){
                    time = "19"+time;
                }else{
                    time = "20"+time;//强行固定20世纪
                }
                Logg.i(TAG,"=====time====="+time);
                result = Long.valueOf(dateToStamp(time));
            }else if(time.length()==14){
//                result = stringToLong(time,"yyyyMMddHHmmss");
                result = Long.valueOf(dateToStamp(time));
            }
        } catch (ParseException e) {
            e.printStackTrace();
            Logg.e(TAG,"getFileCreateTime->ParseException="+e.getMessage());
        }
        return result;
    }

    /**
     * 获取文件的创建时间  DATE_ADDED可能不准确有待讨论
     * */
    public static long getFileCreateTime(String name,String DATE_ADDED) {
        Logg.i(TAG,"=====name==1111==="+name);
        if(name.contains("_UNKNOWN")){
            name = name.replace("_UNKNOWN","");
        }
        Logg.i(TAG,"=====name==2222==="+name);
        String time;
        if(name.contains("_2")){
            time = name.split("_2")[0];
        }else{
            time = name.substring(0,name.length()-4);
        }
        long  result = 0;
        try {
            if(time.length()==12){
//                result = stringToLong(time,"yyyyMMddHHmmss");
                String createTime = longToString(Long.valueOf(DATE_ADDED),"yyyyMMddHHmmss");
                Logg.i(TAG,"=====createTime====="+createTime);
                time = createTime.substring(0,2)+time;
//                time = "20"+time;//强行固定20世纪
                Logg.i(TAG,"=====time====="+time);
                result = Long.valueOf(dateToStamp(time));
            }else if(time.length()==14){
//                result = stringToLong(time,"yyyyMMddHHmmss");
                result = Long.valueOf(dateToStamp(time));
            }
        } catch (ParseException e) {
            e.printStackTrace();
            Logg.e(TAG,"getFileCreateTime->ParseException="+e.getMessage());
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


    /**
     * 时间增加一分钟
     * **/
    public static String timeAddOneMinute(String strTime)
            throws ParseException {
        SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMddHHmmss");
        Date dt=sdf.parse(strTime);
        Calendar rightNow = Calendar.getInstance();
        rightNow.setTime(dt);
        rightNow.add(Calendar.MINUTE,1);//分钟加一
        Date dt1=rightNow.getTime();
        String reStr = sdf.format(dt1);
        return reStr;
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
    public static Date stringToDate1(String strTime)
            throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("YYMMDDhhmmss");
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

    public static String dateToStamp1(String s) throws ParseException {
        String res;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyMMddHHmmss");
        Date date = simpleDateFormat.parse(s);
        long ts = date.getTime();
        Logg.i(TAG,"====dateToStamp1==ts======"+ts);
        res = String.valueOf(ts);
        Logg.i(TAG,"====dateToStamp1========"+stringToDate(res));
        return res;
    }

    // formatType格式为yyyy-MM-dd HH:mm:ss//yyyy年MM月dd日 HH时mm分ss秒
    // data Date类型的时间
    public static String dateToString(Date data, String formatType) {
        return new SimpleDateFormat(formatType).format(data);
    }

    // currentTime要转换的long类型的时间
    // formatType要转换的string类型的时间格式
    public static String longToString(long currentTime, String formatType)
            throws ParseException {
        Date date = longToDate(currentTime, formatType); // long类型转成Date类型
        String strTime = dateToString(date, formatType); // date类型转成String
        return strTime;
    }

    // strTime要转换的string类型的时间，formatType要转换的格式yyyy-MM-dd HH:mm:ss//yyyy年MM月dd日
    // HH时mm分ss秒，
    // strTime的时间格式必须要与formatType的时间格式相同
    public static Date stringToDate(String strTime, String formatType)
            throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat(formatType);
        Date date = null;
        date = formatter.parse(strTime);
        return date;
    }

    // currentTime要转换的long类型的时间
    // formatType要转换的时间格式yyyy-MM-dd HH:mm:ss//yyyy年MM月dd日 HH时mm分ss秒
    public static Date longToDate(long currentTime, String formatType)
            throws ParseException {
        Date dateOld = new Date(currentTime); // 根据long类型的毫秒数生命一个date类型的时间
        String sDateTime = dateToString(dateOld, formatType); // 把date类型的时间转换为string
        Date date = stringToDate(sDateTime, formatType); // 把String类型转换为Date类型
        return date;
    }

    // strTime要转换的String类型的时间
    // formatType时间格式
    // strTime的时间格式和formatType的时间格式必须相同
    public static long stringToLong(String strTime, String formatType)
            throws ParseException {
        Date date = stringToDate(strTime, formatType); // String类型转成date类型
        if (date == null) {
            return 0;
        } else {
            long currentTime = dateToLong(date); // date类型转成long类型
            return currentTime;
        }
    }

    /**
     * 时间增加一分钟
     * **/
    public static String changeSecondTime(String strTime,int changeSecondTime)
            throws ParseException {
        SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMddHHmmss");
        Date dt=sdf.parse(strTime);
        Calendar rightNow = Calendar.getInstance();
        rightNow.setTime(dt);
        rightNow.add(Calendar.SECOND,changeSecondTime);
        Date dt1=rightNow.getTime();
        String reStr = sdf.format(dt1);
        return reStr;
    }

    public static String stamps2Time(long stamps){
        Date date = new Date(stamps);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        return dateFormat.format(date);
    }

}
