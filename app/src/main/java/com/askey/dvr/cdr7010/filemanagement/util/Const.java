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

    public static final String DVR_DIR = "DVR";
    public static final String NORMAL_DIR = "NORMAL";
    public static final String EVENT_DIR = "EVENT";
    public static final String PARKING_DIR = "PARKING";
    public static final String PICTURE_DIR = "PICTURE";
    public static final String MANUAL_DIR = "MANUAL";
    public static final String SYSTEM_DIR = "SYSTEM";

    public static int CURRENT_SDCARD_SIZE = 0; // unit GB
    public static final String SDCARD_SIZE_UNIT ="GB" ;
    public static final String BACK_SLASH_1= "/";

    public static final int SDCARD_SIZE_4Gb = 4; // unit GB
    public static final int SDCARD_MAX_NORMAL_FILE_SIZE_4Gb = 1000; // unit 个文件
    public static final int SDCARD_MAX_EVENT_FILE_SIZE_4Gb = 10; // unit 个文件
    public static final int SDCARD_MAX_PARKING_FILE_SIZE_4Gb = 10; // unit 个文件
    public static final int SDCARD_MAX_PICTURE_FILE_SIZE_4Gb = 50; // unit 个文件

    public static final int SDCARD_SIZE_8Gb = 8; // unit GB
    public static final int SDCARD_MAX_NORMAL_FILE_SIZE_8Gb = 1000; // unit 个文件
    public static final int SDCARD_MAX_EVENT_FILE_SIZE_8Gb = 20; // unit 个文件
    public static final int SDCARD_MAX_PARKING_FILE_SIZE_8Gb = 20; // unit 个文件
    public static final int SDCARD_MAX_PICTURE_FILE_SIZE_8Gb = 100; // unit 个文件

    public static final int SDCARD_SIZE_16Gb = 16; // unit GB
    public static final int SDCARD_MAX_NORMAL_FILE_SIZE_16Gb = 1000; // unit 个文件
    public static final int SDCARD_MAX_EVENT_FILE_SIZE_16Gb = 40; // unit 个文件
    public static final int SDCARD_MAX_PARKING_FILE_SIZE_16Gb = 40; // unit 个文件
    public static final int SDCARD_MAX_PICTURE_FILE_SIZE_16Gb = 200; // unit 个文件

    public static final int SDCARD_SIZE_32Gb = 32; // unit GB
    public static final int SDCARD_MAX_NORMAL_FILE_SIZE_32Gb = 1000; // unit 个文件
    public static final int SDCARD_MAX_EVENT_FILE_SIZE_32Gb = 80; // unit 个文件
    public static final int SDCARD_MAX_PARKING_FILE_SIZE_32Gb = 80; // unit 个文件
    public static final int SDCARD_MAX_PICTURE_FILE_SIZE_32Gb = 400; // unit 个文件

    public static final int SDCARD_SIZE_64Gb = 64; // unit GB
    public static final int SDCARD_MAX_NORMAL_FILE_SIZE_64Gb = 1000; // unit 个文件
    public static final int SDCARD_MAX_EVENT_FILE_SIZE_64Gb = 160; // unit 个文件
    public static final int SDCARD_MAX_PARKING_FILE_SIZE_64Gb = 160; // unit 个文件
    public static final int SDCARD_MAX_PICTURE_FILE_SIZE_64Gb = 800; // unit 个文件

    public static final int SDCARD_SIZE_128Gb = 128; // unit GB
    public static final int SDCARD_MAX_NORMAL_FILE_SIZE_128Gb = 1000; // unit 个文件
    public static final int SDCARD_MAX_EVENT_FILE_SIZE_128Gb = 320; // unit 个文件
    public static final int SDCARD_MAX_PARKING_FILE_SIZE_128Gb = 320; // unit 个文件
    public static final int SDCARD_MAX_PICTURE_FILE_SIZE_128Gb = 1000; // unit 个文件

    public static final int SDCARD_SIZE_256Gb = 256; // unit GB
    public static final int SDCARD_MAX_NORMAL_FILE_SIZE_256Gb = 1000; // unit 个文件
    public static final int SDCARD_MAX_EVENT_FILE_SIZE_256Gb = 640; // unit 个文件
    public static final int SDCARD_MAX_PARKING_FILE_SIZE_256Gb = 640; // unit 个文件
    public static final int SDCARD_MAX_PICTURE_FILE_SIZE_256Gb = 1000; // unit 个文件

    public static final int SDCARD_SIZE_512Gb = 512; // unit GB
    public static final int SDCARD_MAX_NORMAL_FILE_SIZE_512Gb = 1000; // unit 个文件
    public static final int SDCARD_MAX_EVENT_FILE_SIZE_512Gb = 1000; // unit 个文件
    public static final int SDCARD_MAX_PARKING_FILE_SIZE_512Gb = 1000; // unit 个文件
    public static final int SDCARD_MAX_PICTURE_FILE_SIZE_512Gb = 1000; // unit 个文件

    public static final String ACTION_SDCARD_NORMAL_MAX_FILE = "action_sdcard_normal_max_file";
    public static final String ACTION_SDCARD_EVENT_MAX_FILE = "action_sdcard_event_max_file";
    public static final String ACTION_SDCARD_PARKING_MAX_FILE = "action_sdcard_parking_max_file";
    public static final String ACTION_SDCARD_PICTURE_MAX_FILE = "action_sdcard_picture_max_file";
    public static final String ACTION_SDCARD_FULL_MAX_FILE = "action_sdcard_full";
    
}
