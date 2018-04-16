package com.askey.dvr.cdr7010.filemanagement.util;


import android.os.Environment;

/***
 * 常量配置类
 * Company: Chengdu Skysoft Info&Tech Co., Ltd.
 * Copyright ©2014-2018 成都天软信息技术有限公司
 * @since:JDK1.7
 * @version:1.0
 * @see
 * @author charles
 ***/
public class Const {

    /**
     * 是否是DEBUG模式
     */
    public static final boolean DEBUG = true;

    public static final String SDCARD_PATH = Environment.getExternalStorageDirectory().toString();

    public static boolean SDCARD_IS_EXIST = false;

    public static final String NORMAL_DIR = "NORMAL";
    public static final String EVENT_DIR = "EVENT";
    public static final String PARKING_DIR = "PARKING";
    public static final String PICTURE_DIR = "PICTURE";
    public static final String MANUAL_DIR = "MANUAL";
    public static final String SYSTEM_DIR = "SYSTEM";

}
