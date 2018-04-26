package com.askey.dvr.cdr7010.filemanagement;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.askey.dvr.cdr7010.filemanagement.broadcast.SdCardReceiver;
import com.askey.dvr.cdr7010.filemanagement.controller.FileManager;
import com.askey.dvr.cdr7010.filemanagement.controller.MediaScanner;
import com.askey.dvr.cdr7010.filemanagement.controller.SdcardManager;
import com.askey.dvr.cdr7010.filemanagement.util.Const;
import com.askey.dvr.cdr7010.filemanagement.util.Logg;
import com.askey.dvr.cdr7010.filemanagement.util.SdcardUtil;
import com.askey.dvr.cdr7010.filemanagement.util.SdcardUtils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 项目名称：filemanagement
 * 类描述：
 * 创建人：skysoft  charles.bai
 * 创建时间：2018/4/11 15:35
 * 修改人：skysoft
 * 修改时间：2018/4/11 15:35
 * 修改备注：
 */
public class MainActivity extends Activity{

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private  SdCardReceiver sdCardReceiver;
    private Context mContext;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;

        requestSdcardPermission();

        SdcardUtils.registerStorageEventListener(mContext);

        registerReceiver();



        new Thread(new Runnable() {
            @Override
            public void run() {
                Const.SDCARD_IS_EXIST = SdcardUtil.checkSdcardExist();

                if( Const.SDCARD_IS_EXIST){
                    Const.CURRENT_SDCARD_SIZE = SdcardUtil.getCurentSdcardInfo(mContext);
                }

                FileManager mFileManager =FileManager.getSingInstance();
                boolean result = mFileManager.sdcardInit();
                Logg.i(LOG_TAG,"==FH_Init==result=="+result);

                String openSdcard =mFileManager.openSdcard("180425102345.mp4", Const.EVENT_DIR);
                Logg.i(LOG_TAG,"==openSdcard==="+openSdcard);
//                String resultOpen =mFileManager.FH_Open("20180425102345_2.mp4", Const.TYPE_EVENT_DIR);
//                String resultOpen1 =mFileManager.FH_Open("180425102345_2.mp4", Const.TYPE_EVENT_DIR);
//                String resultOpen2 =mFileManager.FH_Open("180425102345.mp4", Const.TYPE_EVENT_DIR);
//                String resultOpen3 =mFileManager.FH_Open("20180425102345.mp4", Const.TYPE_EVENT_DIR);
//                String resultOpen4 =mFileManager.FH_Open("h1.code", Const.TYPE_EVENT_DIR);
//                Logg.i(LOG_TAG,"==FH_Init==resultOpen=="+resultOpen);
//                Logg.i(LOG_TAG,"==FH_Init==resultOpen1=="+resultOpen1);
//                Logg.i(LOG_TAG,"==FH_Init==resultOpen2=="+resultOpen2);
//                Logg.i(LOG_TAG,"==FH_Init==resultOpen3=="+resultOpen3);
//                Logg.i(LOG_TAG,"==FH_Init==resultOpen4=="+resultOpen4);

                boolean resultClose = mFileManager.FH_Close();
                Logg.i(LOG_TAG,"==FH_Init==resultClose=="+resultClose);
                boolean resultSync = mFileManager.FH_Sync();
                Logg.i(LOG_TAG,"==FH_Init==resultSync=="+resultSync);
                String resultLastfile = mFileManager.FH_FindOldest(Const.TYPE_NORMAL_DIR);
                Logg.i(LOG_TAG,"==FH_Init==resultLastfile=="+resultLastfile);
                boolean resultDelete = mFileManager.FH_Delete(resultLastfile);
                Logg.i(LOG_TAG,"==FH_Init==resultDelete=="+resultDelete);

                List<SdcardInfo> sdcardInfoList = SdcardManager.getSingInstance().getSdcardInfo();
                if(sdcardInfoList!=null && sdcardInfoList.size()>0){
                    Logg.i(LOG_TAG,"==getNormalCurrentSize=="+sdcardInfoList.get(0).getNormalCurrentSize());
                    Logg.i(LOG_TAG,"==getEventCurrentSize=="+sdcardInfoList.get(0).getEventCurrentSize());
                    Logg.i(LOG_TAG,"==getParkingCurrentSize=="+sdcardInfoList.get(0).getParkingCurrentSize());
                    Logg.i(LOG_TAG,"==getPictureCurrentSize=="+sdcardInfoList.get(0).getPictureCurrentSize());
                }

            }
        }).start();

    }


    private void registerReceiver() {
        sdCardReceiver =new SdCardReceiver();
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_MEDIA_MOUNTED);// sd卡被插入，且已经挂载
        intentFilter.setPriority(1000);// 设置最高优先级
        intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);// sd卡存在，但还没有挂载
        intentFilter.addAction(Intent.ACTION_MEDIA_REMOVED);// sd卡被移除
        intentFilter.addAction(Intent.ACTION_MEDIA_SHARED);// sd卡作为 USB大容量存储被共享，挂载被解除
        intentFilter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);// sd卡已经从sd卡插槽拔出，但是挂载点还没解除
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);// 开始扫描
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);// 扫描完成
        intentFilter.addDataScheme("file");
        registerReceiver(sdCardReceiver, intentFilter);// 注册监听函数
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SdcardUtils.unRegisterStorageEventListener(mContext);
        unregisterReceiver(sdCardReceiver);//取消注册
    }

    private int SDCARD_REQUEST_CODE = 10001;//SD卡读写
    @TargetApi(Build.VERSION_CODES.M)
    private void requestSdcardPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // 第一次请求权限时，用户如果拒绝，下一次请求shouldShowRequestPermissionRationale()返回true
            // 向用户解释为什么需要这个权限

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            } else {
                //申请相机权限
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, SDCARD_REQUEST_CODE);
            }
        } else {
            Logg.i(LOG_TAG, "requestSdcardPermission:true");
            //NORMAL EVENT PARKING PICTURE
            if(SdcardUtil.checkSdcardExist()){
                Logg.i(LOG_TAG,"====findFileByType====");
                List <ItemData> fileList = MediaScanner.getAllFiles(Const.NORMAL_DIR);
                Logg.i(LOG_TAG,"fileList.size()==="+fileList.size());
                for (ItemData item :fileList){
                    List <ItemData>group= item.getDirFileItem();
                    Logg.i(LOG_TAG,"group.size()==="+group.size());
                    for (ItemData roupItem :group){
                        Logg.i(LOG_TAG,"roupItem.getFileName()=="+roupItem.getFileName());
                    }
                }
            }else{
                Logg.i(LOG_TAG, "SdcardUtil.checkSdcardExist()=false");
            }

        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0) {
            if (requestCode == SDCARD_REQUEST_CODE) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    Toast.makeText(this, "SD卡权限已申请", Toast.LENGTH_SHORT).show();
                } else {
                    //用户勾选了不再询问
                    //提示用户手动打开权限
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        Toast.makeText(this, "set_sdcard_permission_faild", Toast.LENGTH_SHORT).show();
//                        speechUtils.speakText(getString(R.string.sdcard_per_disabled));

                        //NORMAL EVENT PARKING PICTURE
                        ArrayList fileList = (ArrayList) MediaScanner.getAllFileList("EVENT");
                        Logg.i(LOG_TAG,"====fileList=="+fileList);

                    }
                }
            }
        }
    }

}
