package com.askey.dvr.cdr7010.filemanagement.util;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.storage.StorageManager;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.TextView;

import com.askey.dvr.cdr7010.filemanagement.R;
import com.askey.dvr.cdr7010.filemanagement.application.FileManagerApplication;
import com.askey.dvr.cdr7010.filemanagement.controller.FileManager;
import com.askey.platform.storage.AskeyStorageManager;
import com.askey.platform.storage.DiskInfo;
import com.askey.platform.storage.StorageEventListener;

/**
 * Created by test on 2017/3/2.
 */

public class SdcardUtils {
    private static final String LOG_TAG = SdcardUtils.class.getSimpleName();
    private static Context mContext;


    public static boolean isSDCardValid(Context context) {
        AskeyStorageManager storageManager =AskeyStorageManager.getInstance(context);
        for (DiskInfo disk :storageManager.getDisks()) {
            Logg.d(LOG_TAG, "isSDCardValid: disk " + disk.sysPath);
            if (disk.isSd()) {
                Logg.d(LOG_TAG, "isSDCardValid: sdcard disk, volumeCount = " + disk.volumeCount + ", size = " + disk.size);
                if (disk.volumeCount== 0 && disk.size > 0)
                    return false;

                return true;
            }
        }
        return false;
    }

    public static boolean sdcardAvailable() {
        // sdcard 存在  格式正确  且init成功才算可用
        if(isSDCardValid(FileManagerApplication.getAppContext())){
            return FileManager.getSingInstance().sdcardInit();
        }
        return false;
    }

    private static void formatSDCard(final Context context) {
        final AskeyStorageManager storageManager =AskeyStorageManager.getInstance(context);
        for (final DiskInfo disk :storageManager.getDisks()) {
            Logg.d(LOG_TAG, "formatSDCard: disk " + disk.sysPath);
            if (disk.isSd()) {
//                Logg.d(LOG_TAG, "formatSDCard: sdcard disk, volumeCount = " + disk.getvolumeCount() + ", size = " + disk.getSize());
//                if (disk.getvolumeCount() == 0 && disk.getSize() > 0) {
//                    // No supported volumes found, give user option to format
                    Logg.d(LOG_TAG, "formatSDCard: format " + disk.sysPath);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                storageManager.partitionPublic(disk.id);
                            } catch (Exception e) {
                                Logg.w(LOG_TAG, "formatSDCard: format thread error. " + e.getMessage());
                            }
                        }
                    }).start();
                    return;
//                }
            }
        }
    }

    private static StorageEventListener mStorageEventListener = new StorageEventListener() {
        @Override
        public void onDiskScanned(DiskInfo disk, int volumeCount) {
            Logg.i(LOG_TAG, "onDiskScanned: "+ disk.toString());
            Logg.i(LOG_TAG, "onDiskScanned: volumeCount=" + volumeCount);
            if (disk.isSd()) {
                Logg.d(LOG_TAG, "onDiskScanned: sdcard disk, volumeCount = " + volumeCount + ", size = " + disk.size);
                if (volumeCount == 0 && disk.size > 0) {
                    // format
                    BroadcastUtils.sendMyBroadcast(FileManagerApplication.getAppContext(),
                            Const.ACTION_SDCARD_STATUS,Const.CMD_SHOW_SDCARD_NOT_SUPPORTED);
                    dialog(mContext);
                }
            }
        }

        @Override
        public void onDiskDestroyed(DiskInfo disk) {
            Logg.d(LOG_TAG, "onDiskDestroyed: " + disk.toString());
            if (disk.isSd() && isShown && null != mView) {
                mWindowManager.removeView(mView);
                isShown = false;
                BroadcastUtils.sendMyBroadcast(FileManagerApplication.getAppContext(),
                        Const.ACTION_SDCARD_STATUS,Const.CMD_SHOW_SDCARD_SUPPORTED);
            }
        }

        @Override
        public void onVolumeForgotten(String fsUuid) {
            Logg.i(LOG_TAG, "onVolumeForgotten: fsUuid=" + fsUuid);
            super.onVolumeForgotten(fsUuid);
        }

        @Override
        public void onStorageStateChanged(String path, String oldState, String newState) {
//            super.onStorageStateChanged(path, oldState, newState);
            Logg.i(LOG_TAG, "onStorageStateChanged: path=" + path);
            Logg.i(LOG_TAG, "onStorageStateChanged: oldState=" + oldState);
            Logg.i(LOG_TAG, "onStorageStateChanged: newState=" + newState);

            if(newState .equals("6")){
                // format
                BroadcastUtils.sendMyBroadcast(FileManagerApplication.getAppContext(),
                        Const.ACTION_SDCARD_STATUS,Const.CMD_SHOW_SDCARD_NOT_SUPPORTED);
//                dialog(mContext);
            }
        }

        @Override
        public void onUsbMassStorageConnectionChanged(boolean connected) {
            Logg.i(LOG_TAG, "onUsbMassStorageConnectionChanged: connected=" + connected);
            super.onUsbMassStorageConnectionChanged(connected);
        }


    };
    private static WindowManager mWindowManager;
    private static Boolean isShown = false;
    private static View mView = null;

    private static void dialog(final Context context) {
        if (isShown && null != mView) {
            mWindowManager.removeView(mView);
            isShown = false;
        }
        isShown = true;
        mView = null;
        mWindowManager = (WindowManager) mContext
                .getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        int flags = WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
        params.flags = flags;
        params.format = PixelFormat.TRANSLUCENT;
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        params.gravity = Gravity.CENTER;
        mView = LayoutInflater.from(context).inflate(R.layout.format_sdcard_activity, null);
        TextView tv_activity_title = (TextView) mView.findViewById(R.id.tv_activity_title);
        tv_activity_title.setText(R.string.format_sdcard_activity);
        RadioButton rbStartFormat = (RadioButton) mView.findViewById(R.id.rb_start_format);
        RadioButton rbCancelFormat = (RadioButton) mView.findViewById(R.id.rb_cancel_format);
        rbStartFormat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWindowManager.removeView(mView);
                isShown = false;
                formatSDCard(context);
            }
        });
        rbStartFormat.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                switch (i) {
                    case KeyEvent.KEYCODE_BACK:
                        mWindowManager.removeView(mView);
                        isShown = false;
                        return true;
                    default:
                        return false;
                }
            }
        });
        rbCancelFormat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWindowManager.removeView(mView);
                isShown = false;
            }
        });
        rbCancelFormat.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                switch (i) {
                    case KeyEvent.KEYCODE_BACK:
                        mWindowManager.removeView(mView);
                        isShown = false;
                        return true;
                    default:
                        return false;
                }
            }
        });
        mWindowManager.addView(mView, params);
    }

    public static void registerStorageEventListener(Context context) {
        AskeyStorageManager storageManager =AskeyStorageManager.getInstance(context);
        mContext = context;
        storageManager.registerListener(mStorageEventListener);

    }

    public static void unRegisterStorageEventListener(Context context) {
        AskeyStorageManager storageManager =AskeyStorageManager.getInstance(context);
        storageManager.unregisterListener(mStorageEventListener);
    }




}
