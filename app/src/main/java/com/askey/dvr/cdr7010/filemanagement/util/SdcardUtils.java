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

import com.askey.android.platform_library.DiskInfoExtend;
import com.askey.android.platform_library.PlatformLibrary;
import com.askey.android.platform_library.StorageEventListenerExtend;
import com.askey.android.platform_library.StorageUtils;
import com.askey.android.platform_library.VolumeInfoExtend;
import com.askey.dvr.cdr7010.filemanagement.R;
import com.askey.dvr.cdr7010.filemanagement.application.FileManagerApplication;

/**
 * Created by test on 2017/3/2.
 */

public class SdcardUtils {
    private static final String LOG_TAG = SdcardUtils.class.getSimpleName();
    private static Context mContext;


    public static boolean isSDCardValid(Context context) {
//        StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        PlatformLibrary mPlatformLibrary = new PlatformLibrary(context);
        StorageUtils mStorageUtils = mPlatformLibrary.getStorageManager();
        for (DiskInfoExtend disk :mStorageUtils.getDisksExtend()) {
            Logg.d(LOG_TAG, "isSDCardValid: disk " + disk.getSysPath());
            if (disk.isSd()) {
                Logg.d(LOG_TAG, "isSDCardValid: sdcard disk, volumeCount = " + disk.getvolumeCount() + ", size = " + disk.getSize());
                if (disk.getvolumeCount() == 0 && disk.getSize() > 0)
                    return false;

                return true;
            }
        }

        return false;
    }

    private static void formatSDCard(Context context) {
        final StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        PlatformLibrary mPlatformLibrary = new PlatformLibrary(FileManagerApplication.getAppContext());
        final StorageUtils mStorageUtils = mPlatformLibrary.getStorageManager();
        for (final DiskInfoExtend disk : mStorageUtils.getDisksExtend()) {
            Logg.d(LOG_TAG, "formatSDCard: disk " + disk.getSysPath());
            if (disk.isSd()) {
//                Logg.d(LOG_TAG, "formatSDCard: sdcard disk, volumeCount = " + disk.getvolumeCount() + ", size = " + disk.getSize());
//                if (disk.getvolumeCount() == 0 && disk.getSize() > 0) {
//                    // No supported volumes found, give user option to format
                    Logg.d(LOG_TAG, "formatSDCard: format " + disk.getSysPath());
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                mStorageUtils.partitionPublic(disk.getId());
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

    private static StorageEventListenerExtend mStorageEventListener = new StorageEventListenerExtend() {
        @Override
        public void onDiskScanned(DiskInfoExtend disk, int volumeCount) {
            Logg.i(LOG_TAG, "onDiskScanned: ");
            if (disk.isSd()) {
//                Logg.d(LOG_TAG, "onDiskScanned: sdcard disk, volumeCount = " + volumeCount + ", size = " + disk.getSize());
//                if (volumeCount == 0 && disk.getSize() > 0) {
//                    // format
//                    dialog(mContext);
//                }
            }
        }

        @Override
        public void onDiskDestroyed(DiskInfoExtend disk) {
            Logg.d(LOG_TAG, "onDiskDestroyed: " + disk.toString());
            if (disk.isSd() && isShown && null != mView) {
                mWindowManager.removeView(mView);
                isShown = false;
            }
        }

        @Override
        public void onVolumeStateChanged(VolumeInfoExtend vol, int oldState, int newState) {
            Logg.d(LOG_TAG, "onVolumeStateChanged: newState = " + newState);
            if(newState == 6 /*VolumeInfo.STATE_UNMOUNTABLE*/){
                // format
                dialog(mContext);
            }
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
//        StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        PlatformLibrary mPlatformLibrary = new PlatformLibrary(FileManagerApplication.getAppContext());
        StorageUtils mStorageUtils = mPlatformLibrary.getStorageManager();
        mContext = context;
        mStorageUtils.registerListener(mStorageEventListener);

    }

    public static void unRegisterStorageEventListener(Context context) {
//        StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        PlatformLibrary mPlatformLibrary = new PlatformLibrary(FileManagerApplication.getAppContext());
        StorageUtils mStorageUtils = mPlatformLibrary.getStorageManager();
        mStorageUtils.unregisterListener(mStorageEventListener);
    }

}
