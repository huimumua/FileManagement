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

    /**
     * 检测sdcard空间是否充足
     * */
    public static boolean  checkSDcardIsFull(){
        try {
            //找到sdcard的位置
            File directory = Environment.getExternalStorageDirectory();
            if(directory.exists()){
                //硬盘的描述类
                StatFs statFs = new StatFs(directory.getAbsolutePath());
                //每块的大小
                long blockSize = statFs.getBlockSize();
                Logg.i(TAG,"==blockSize=="+blockSize);
                //可用块的数量
                int availableBlocks = statFs.getAvailableBlocks();
                Logg.i(TAG,"==availableBlocks=="+availableBlocks);
                //可用空间      以字节为单位
                long availableSdcardSize = blockSize*availableBlocks;
                Logg.i(TAG,"==availableSdcardSize==" + availableSdcardSize );
                if(availableSdcardSize <= Const.SDCARD_RESERVE_SPACE){//这里预留100M
                    return true;
                }
            }
        }catch (Exception e){
            Logg.e(TAG,"-> getCurentSdcardInfo -> Exception"+e.getMessage());
        }
        return false;
    }
    public static int getCurentSdcardInfo(Context context ) {
        try {
            //找到sdcard的位置
            File directory = Environment.getExternalStorageDirectory();
            if(directory.exists()){
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
                    totalSize = Const.SDCARD_SIZE_4Gb;
                }else if(totalSdcardSize>6 && totalSdcardSize<8){
                    totalSize = Const.SDCARD_SIZE_8Gb;
                }else if(totalSdcardSize>14 && totalSdcardSize<16){
                    totalSize = Const.SDCARD_SIZE_16Gb;
                }else if(totalSdcardSize>28 && totalSdcardSize<32){
                    totalSize = Const.SDCARD_SIZE_32Gb;
                }else if(totalSdcardSize>56 && totalSdcardSize<64){
                    totalSize = Const.SDCARD_SIZE_64Gb;
                }else if(totalSdcardSize>110 && totalSdcardSize<128){
                    totalSize = Const.SDCARD_SIZE_128Gb;
                }else if(totalSdcardSize>245 && totalSdcardSize<256){
                    totalSize = Const.SDCARD_SIZE_256Gb;
                }else if(totalSdcardSize>491 && totalSdcardSize<512){
                    totalSize = Const.SDCARD_SIZE_512Gb;
                }
                return totalSize;
            }
        }catch (Exception e){
            Logg.e(TAG,"-> getCurentSdcardInfo -> Exception"+e.getMessage());
        }
        return 0;
    }


}
