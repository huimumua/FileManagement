package com.askey.dvr.cdr7010.filemanagement.util;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.Formatter;

import java.io.File;

/**
 * 项目名称：filemanagement
 * 类描述：
 * 创建人：skysoft  charles.bai
 * 创建时间：2018/4/12 19:27
 * 修改人：skysoft
 * 修改时间：2018/4/12 19:27
 * 修改备注：
 */
public class SdcardUtil {

    private static final String TAG = "SdcardUtil";

    public String getSDPath(){
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(android.os.Environment.MEDIA_MOUNTED); //判断sd卡是否存在
        if (sdCardExist)
        {
            sdDir = Environment.getExternalStorageDirectory();//获取跟目录
        }
        return sdDir.toString();
    }

    public static boolean checkSdcardExist(){
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
        {
            return true;
        }
        return false;
    }

    public static String formatSize(Context context ,String target_size) {
        return Formatter.formatFileSize(context, Long.valueOf(target_size));
    }

    public static int getCurentSdcardInfo(Context context ) {
        //找到sdcard的位置
        File directory = Environment.getExternalStorageDirectory();
        //硬盘的描述类
        StatFs statFs = new StatFs(directory.getAbsolutePath());
        //获取硬盘分的块的数量
        int blockCount = statFs.getBlockCount();
        Logg.i(TAG,"==blockCount=="+blockCount);
        //每块的大小
        long blockSize = statFs.getBlockSize();
        Logg.i(TAG,"==blockSize=="+blockSize);
        //可用块的数量
        int availableBlocks = statFs.getAvailableBlocks();
        Logg.i(TAG,"==availableBlocks=="+availableBlocks);
        //sdcard的总容量   以字节为单位
        long sdcardSize = blockCount*blockSize;
        String currentSdcardSize = SdcardUtil.formatSize(context, String.valueOf(sdcardSize));
        Logg.i(TAG,"==currentSdcardSize=="+ currentSdcardSize);
        //可用空间      以字节为单位
        long availableSdcardSize = blockSize*availableBlocks;
        String availableSize = SdcardUtil.formatSize(context, String.valueOf(availableSdcardSize));
        Logg.i(TAG,"==availableSdcardSize==" + availableSize );

        int totalSize = 0;
        float totalSdcardSize = Float.parseFloat(currentSdcardSize.substring(0,currentSdcardSize.length()-3));
        if(totalSdcardSize>3 && totalSdcardSize<4){
            totalSize = 4;
        }else if(totalSdcardSize>6 && totalSdcardSize<8){
            totalSize = 8;
        }else if(totalSdcardSize>15 && totalSdcardSize<16){
            totalSize = 16;
        }else if(totalSdcardSize>28 && totalSdcardSize<32){
            totalSize = 32;
        }else if(totalSdcardSize>56 && totalSdcardSize<64){
            totalSize = 64;
        }else if(totalSdcardSize>110 && totalSdcardSize<128){
            totalSize = 128;
        }else if(totalSdcardSize>245 && totalSdcardSize<256){
            totalSize = 256;
        }else if(totalSdcardSize>491 && totalSdcardSize<512){
            totalSize = 512;
        }
        return totalSize;
    }



}
