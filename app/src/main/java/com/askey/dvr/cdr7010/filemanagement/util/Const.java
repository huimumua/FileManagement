package com.askey.dvr.cdr7010.filemanagement.util;


import android.os.Environment;

/***
 * 常量配置类
 * @Company: Chengdu Skysoft Info&Tech Co., Ltd.
 * @Copyright ©2014-2018 成都天软信息技术有限公司
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

    public static  String SDCARD_PATH = Environment.getExternalStorageDirectory().toString();

    public static final String BACK_SLASH_1= "/";

    public static boolean SDCARD_IS_EXIST = false;
    public static boolean SDCARD_INIT_SUCCESS = false;

    public static final String NORMAL_DIR = "NORMAL";
    public static final String EVENT_DIR = "EVENT";
    public static final String CAMERA2_DIR = "CAMERA2";
    public static final String PICTURE_DIR = "PICTURE";
    public static final String SYSTEM_DIR = "SYSTEM";
    public static final String NMEA_EVENT_DIR = "NMEA_EVENT";
    public static final String NMEA_NORMAL_DIR = "NMEA_NORMAL";
    public static final String NMEA_CAMERA_DIR = "NMEA_CAMERA";
    public static final String PARKING_DIR = "PARKING";

    public static final int TYPE_EVENT_DIR = 0;
    public static final int TYPE_NORMAL_DIR = 1;
    public static final int TYPE_CAMERA2_DIR = 2;
    public static final int TYPE_PICTURE_DIR = 3;
    public static final int TYPE_SYSTEM_DIR = 4;
    public static final int TYPE_NMEA_EVENT_DIR = 5;
    public static final int TYPE_NMEA_NORMAL_DIR = 6;
    public static final int TYPE_NMEA_CAMERA_DIR = 7;
    public static final int TYPE_PARKING_DIR = 8;

    public static int CURRENT_SDCARD_SIZE = 0; // unit GB
    public static final String SDCARD_SIZE_UNIT ="GB" ;
    public static final long SDCARD_RESERVE_SPACE = 1*1024*1024*100 ;//sdcard预留空间大小

    public static final int SDCARD_SIZE_4Gb = 4; // unit GB
    public static final int SDCARD_MAX_NORMAL_FILE_SIZE_4Gb = 1000; // unit 个文件
    public static final int SDCARD_MAX_EVENT_FILE_SIZE_4Gb = 10; // unit 个文件
    public static final int SDCARD_MAX_PICTURE_FILE_SIZE_4Gb = 30; // unit 个文件

    public static final int SDCARD_SIZE_8Gb = 8; // unit GB
    public static final int SDCARD_MAX_NORMAL_FILE_SIZE_8Gb = 1000; // unit 个文件
    public static final int SDCARD_MAX_EVENT_FILE_SIZE_8Gb = 20; // unit 个文件
    public static final int SDCARD_MAX_PICTURE_FILE_SIZE_8Gb = 60; // unit 个文件

    public static final int SDCARD_SIZE_16Gb = 16; // unit GB
    public static final int SDCARD_MAX_NORMAL_FILE_SIZE_16Gb = 1000; // unit 个文件
    public static final int SDCARD_MAX_EVENT_FILE_SIZE_16Gb = 40; // unit 个文件
    public static final int SDCARD_MAX_PICTURE_FILE_SIZE_16Gb = 120; // unit 个文件

    public static final int SDCARD_SIZE_32Gb = 32; // unit GB
    public static final int SDCARD_MAX_NORMAL_FILE_SIZE_32Gb = 1000; // unit 个文件
    public static final int SDCARD_MAX_EVENT_FILE_SIZE_32Gb = 80; // unit 个文件
    public static final int SDCARD_MAX_PICTURE_FILE_SIZE_32Gb = 240; // unit 个文件

    public static final int SDCARD_SIZE_64Gb = 64; // unit GB
    public static final int SDCARD_MAX_NORMAL_FILE_SIZE_64Gb = 1000; // unit 个文件
    public static final int SDCARD_MAX_EVENT_FILE_SIZE_64Gb = 160; // unit 个文件
    public static final int SDCARD_MAX_PICTURE_FILE_SIZE_64Gb = 480; // unit 个文件

    public static final int SDCARD_SIZE_128Gb = 128; // unit GB
    public static final int SDCARD_MAX_NORMAL_FILE_SIZE_128Gb = 1000; // unit 个文件
    public static final int SDCARD_MAX_EVENT_FILE_SIZE_128Gb = 320; // unit 个文件
    public static final int SDCARD_MAX_PICTURE_FILE_SIZE_128Gb = 960; // unit 个文件

    public static final int SDCARD_SIZE_256Gb = 256; // unit GB
    public static final int SDCARD_MAX_NORMAL_FILE_SIZE_256Gb = 1000; // unit 个文件
    public static final int SDCARD_MAX_EVENT_FILE_SIZE_256Gb = 640; // unit 个文件
    public static final int SDCARD_MAX_PICTURE_FILE_SIZE_256Gb = 1920; // unit 个文件

    public static final int SDCARD_SIZE_512Gb = 512; // unit GB
    public static final int SDCARD_MAX_NORMAL_FILE_SIZE_512Gb = 1000; // unit 个文件
    public static final int SDCARD_MAX_EVENT_FILE_SIZE_512Gb = 1000; // unit 个文件
    public static final int SDCARD_MAX_PICTURE_FILE_SIZE_512Gb = 3000; // unit 个文件

    public static boolean IS_SDCARD_FOLDER_LIMIT =false;
    public static final String CMD_SHOW_REACH_EVENT_FILE_LIMIT ="show_reach_event_file_limit";//超过限制
    public static final String CMD_SHOW_UNREACH_EVENT_FILE_LIMIT = "show_unreach_event_file_limit";//限制解除
    public static final String CMD_SHOW_REACH_PARKING_FILE_LIMIT = "show_reach_parking_file_limit";//超过限制
    public static final String CMD_SHOW_UNREACH_PARKING_FILE_LIMIT = "show_unreach_parking_file_limit";//限制解除
    public static final String CMD_SHOW_REACH_NORMAL_FILE_LIMIT ="show_reach_normal_file_limit";//超过限制
    public static final String CMD_SHOW_UNREACH_NORMAL_FILE_LIMIT = "show_unreach_normal_file_limit";//限制解除
    public static final String CMD_SHOW_REACH_PICTURE_FILE_LIMIT ="show_reach_picture_file_limit";//超过限制
    public static final String CMD_SHOW_UNREACH_PICTURE_FILE_LIMIT = "show_unreach_picture_file_limit";//限制解除
    public static final String CMD_SHOW_REACH_SYSTEM_FILE_LIMIT ="show_reach_system_file_limit";//超过限制
    public static final String CMD_SHOW_UNREACH_SYSTEM_FILE_LIMIT = "show_unreach_system_file_limit";//限制解除

    public static boolean IS_SDCARD_FULL_LIMIT =false;
    public static final String CMD_SHOW_SDCARD_FULL_LIMIT ="show_sdcard_full_limit";//sdcard空间不足
    public static final String CMD_SHOW_UNREACH_SDCARD_FULL_LIMIT = "show_unreach_sdcard_full_limit";//sdcard空间不足解除

    public static final String ACTION_SDCARD_STATUS = "action_sdcard_status";
    public static final String CMD_SHOW_SDCARD_NOT_SUPPORTED ="show_sdcard_not_supported";//sdcard格式不支持需要formate
    public static final String CMD_SHOW_SDCARD_INIT_FAIL ="show_sdcard_init_fail";//init 失败
    public static final String CMD_SHOW_SDCARD_INIT_SUCC = "show_sdcard_init_success";//init成功
    public static final String CMD_SHOW_SDCARD_UNRECOGNIZABLE ="show_sdcard_unrecognizable";//不被识别

}
