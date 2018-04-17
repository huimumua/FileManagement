package com.askey.dvr.cdr7010.filemanagement.controller;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.askey.dvr.cdr7010.filemanagement.ItemData;
import com.askey.dvr.cdr7010.filemanagement.application.FileManagerApplication;
import com.askey.dvr.cdr7010.filemanagement.util.Const;
import com.askey.dvr.cdr7010.filemanagement.util.Logg;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 项目名称：filemanagement
 * 类描述：
 * 创建人：skysoft  charles.bai
 * 创建时间：2018/4/9 19:58
 * 修改人：skysoft
 * 修改时间：2018/4/9 19:58
 * 修改备注：
 */
public class MediaScanner {

    private static final String TAG = MediaScanner.class.getSimpleName();

    /**
     * Intent.ACTION_MEDIA_SCANNER_SCAN_FILE：
     * 扫描指定文件
     * */
    public static void scanFileAsync(Context ctx, String filePath) {
        Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File file = new File(filePath);
        Uri uri = Uri.fromFile(file);
//        Uri uri = FileProvider.getUriForFile(ctx, BuildConfig.APPLICATION_ID, file);
        scanIntent.setData(uri);
        ctx.sendBroadcast(scanIntent);
    }

    public static final String ACTION_MEDIA_SCANNER_SCAN_DIR = "android.intent.action.MEDIA_SCANNER_SCAN_DIR";

    /**
     * android.intent.action.MEDIA_SCANNER_SCAN_DIR
     * 扫描指定目录
     * */
    public static void scanDirAsync(Context ctx, String dir) {
        Intent scanIntent = new Intent(ACTION_MEDIA_SCANNER_SCAN_DIR);
        File file = new File(dir);
        Uri uri = Uri.fromFile(file);
//        Uri uri = FileProvider.getUriForFile(ctx, BuildConfig.APPLICATION_ID, file);
        scanIntent.setData(uri);
        ctx.sendBroadcast(scanIntent);
    }

    /**
     * 根据类型获取视频文件列表
     * NORMAL EVENT PARKING
     * */
    public static List<String> getAllFileList(String type) {
        if(Const.PICTURE_DIR.equals(type) ){
            return getPictureList();
        }else{
            return getVideoList(type);
        }
    }

    public static List<ItemData> getAllFiles(String type) {
        if(Const.PICTURE_DIR.equals(type) ){
            return getPictures();
        }else{
            return getVideos(type);
        }
    }


    public static ArrayList <String> getVideoList(String type){
        ArrayList <String> fileList = new ArrayList<String>();
        ArrayList <String> list = getAllVideoList();
        Logg.i(TAG,"====list.size()===="+list.size());
        Logg.i(TAG,"====type===="+type);
        for (String path : list ){
            if( path.contains(type) ){
                Logg.i(TAG,"====path===="+path);
                fileList.add(path);
            }
        }
        return fileList;
    }

    public static ArrayList <ItemData> getVideos(String type){
        ArrayList <ItemData> fileList = new ArrayList<ItemData>();
        ArrayList <ItemData> list = getVideos();
        Logg.i(TAG,"====list.size()===="+list.size());
        Logg.i(TAG,"====type===="+type);
        for (ItemData path : list ){
            if( path.getFilePath().contains(type) ){
                Logg.i(TAG,"====path===="+path.getFilePath());
                fileList.add(path);
            }
        }
        return fileList;
    }

    public static ArrayList <String> getAllVideoList(){
        ArrayList <String> fileList = new ArrayList<String>();
        //使用content provider查询所有的视频信息
        ContentResolver resolver= FileManagerApplication.getAppContext().getContentResolver();
        String []projection = { MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.DATA};
        String orderBy = MediaStore.Video.Media.DISPLAY_NAME+ " DESC";
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor=resolver.query(uri, null, null, null, orderBy);

        while(cursor.moveToNext())
        {
            //获取视频的名称
            String name=cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME));
            //获取视频的大小
            String size=cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.SIZE));
            //视频修改时间
            String DATE_MODIFIED=cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATE_MODIFIED));
            //视频创建时间
            String DATE_ADDED=cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATE_ADDED));
            //获取视频的路径
            String path=cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
            //视频时长
            String DURATION=cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DURATION));

            fileList.add(path);
        }
        return fileList;
    }

    public static ArrayList <ItemData> getVideos(){
        ArrayList <ItemData> fileList = new ArrayList<ItemData>();
        //使用content provider查询所有的视频信息
        ContentResolver resolver= FileManagerApplication.getAppContext().getContentResolver();
        String []projection = { MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.DATA};
        String orderBy = MediaStore.Video.Media.DISPLAY_NAME+ " DESC";
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor=resolver.query(uri, null, null, null, orderBy);

        while(cursor.moveToNext())
        {
            //获取视频的名称
            String name=cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME));
            //获取视频的大小
            String size=cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.SIZE));
            //视频修改时间
            String DATE_MODIFIED=cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATE_MODIFIED));
            //视频创建时间
            String DATE_ADDED=cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATE_ADDED));
            //获取视频的路径
            String path=cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
            //视频时长
            String DURATION=cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DURATION));

            ItemData itemData= new ItemData();
            itemData.setFileTime(Long.valueOf(DATE_ADDED));
            itemData.setFilePath(path);
            itemData.setFileName(name);
            itemData.setDir(false);
            fileList.add(itemData);
        }
        return fileList;
    }


    /**
     * 根据类型获取图片文件列表
     * */
    public static ArrayList <String> getPictureList(){
        ArrayList <String> fileList = new ArrayList<String>();
        //使用content provider查询所有的图片信息
        ContentResolver resolver= FileManagerApplication.getAppContext().getContentResolver();
        String []projection = { MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.DATA};
        String orderBy = MediaStore.Images.Media.DISPLAY_NAME+ " DESC";
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor=resolver.query(uri, null, null, null, orderBy);

        while(cursor.moveToNext())
        {
            //获取图片的名称
            String name=cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
            //获取图片的大小
            String size=cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.SIZE));
            //获取图片的路径
            String path=cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            if( Const.PICTURE_DIR.equals(path) ){
                fileList.add(path);
            }

        }
        return fileList;
    }

    public static ArrayList <ItemData> getPictures(){
        ArrayList <ItemData> fileList = new ArrayList<ItemData>();
        //使用content provider查询所有的图片信息
        ContentResolver resolver= FileManagerApplication.getAppContext().getContentResolver();
        String []projection = { MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.DATA};
        String orderBy = MediaStore.Images.Media.DISPLAY_NAME + " DESC";
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor=resolver.query(uri, null, null, null, orderBy);

        while(cursor.moveToNext())
        {
            //获取图片的名称
            String name=cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
            //获取图片的大小
            String size=cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.SIZE));
            //获取图片的路径
            String path=cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            String time=cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED));
            if( Const.PICTURE_DIR.equals(path) ){
                ItemData itemData = new  ItemData();
                itemData.setDir(false);
                itemData.setFileName(name);
                itemData.setFilePath(path);
                itemData.setFileTime(Long.valueOf(time));
                fileList.add(itemData);
            }
        }
        return fileList;
    }

    /**
     * 删除文件，可以是文件或文件夹
     *
     * @param fileName
     *            要删除的文件名
     * @return 删除成功返回true，否则返回false
     */
    public static boolean delete(String fileName) {
        File file = new File(fileName);
        if (!file.exists()) {
            Logg.e(TAG,"-->delete --> delete "+fileName+" not exists");
            return false;
        } else {
            if (file.isFile())
                return deleteFile(fileName);
            else
                return deleteDirectory(fileName);
        }
    }

    /**
     * 删除单个文件
     *
     * @param fileName
     *            要删除的文件的文件名
     * @return 单个文件删除成功返回true，否则返回false
     */
    public static boolean deleteFile(String fileName) {
        File file = new File(fileName);
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile()) {
            boolean result = FileManager.getSingInstance().FH_Delete("",fileName);;
            Logg.i(TAG,"-->deleteFile --> FH_Delete "+result+" result =="+result);
            if (file.delete()) {
                Logg.i(TAG,"-->deleteFile --> delete "+fileName+" success");
                //这里清除ContentProvader数据库
                syncDeleteFile(file);
                return true;
            } else {
                Logg.e(TAG,"-->deleteFile --> delete "+fileName+" failed");
                return false;
            }
        } else {
            Logg.e(TAG,"-->deleteFile --> delete "+fileName+" not exists");
            return false;
        }
    }

    private static void syncDeleteFile(File file) {
        //删除多媒体数据库中的数据
        String filePath = file.getPath();
        if(filePath.endsWith(".mp4")){
            int res = FileManagerApplication.getAppContext().getContentResolver().delete(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    MediaStore.Video.Media.DATA + "= \"" + filePath+"\"",
                    null);
            if (res>0){
//                    file.delete();
                Logg.i(TAG, "-->syncDeleteFile-->success");
            }else{
                Logg.e(TAG, "-->syncDeleteFile-->failed");
            }
        }else if (filePath.endsWith(".jpg")||filePath.endsWith(".png")||filePath.endsWith(".bmp")){
            int res = FileManagerApplication.getAppContext().getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    MediaStore.Images.Media.DATA + "= \"" + filePath+"\"",
                    null);
            if (res>0){
//                    file.delete();
                Logg.i(TAG, "-->syncDeleteFile-->success");
            }else{
                Logg.e(TAG, "-->syncDeleteFile-->failed");
            }
        }
    }

    /**
     * 删除目录及目录下的文件
     *
     * @param dir
     *            要删除的目录的文件路径
     * @return 目录删除成功返回true，否则返回false
     */
    public static boolean deleteDirectory(String dir) {
        // 如果dir不以文件分隔符结尾，自动添加文件分隔符
        if (!dir.endsWith(File.separator))
            dir = dir + File.separator;
        File dirFile = new File(dir);
        // 如果dir对应的文件不存在，或者不是一个目录，则退出
        if ((!dirFile.exists()) || (!dirFile.isDirectory())) {
            Logg.e(TAG,"-->deleteDirectory --> delete "+dir+" not exists");
            return false;
        }
        boolean flag = true;
        // 删除文件夹中的所有文件包括子目录
        File[] files = dirFile.listFiles();
        for (int i = 0; i < files.length; i++) {
            // 删除子文件
            if (files[i].isFile()) {
                flag = deleteFile(files[i].getAbsolutePath());
                if (!flag)
                    break;
            }
            // 删除子目录
            else if (files[i].isDirectory()) {
                flag = deleteDirectory(files[i]
                        .getAbsolutePath());
                if (!flag)
                    break;
            }
        }
        if (!flag) {
            Logg.e(TAG,"-->deleteDirectory --> delete "+dir+" failed");
            return false;
        }
        // 删除当前目录
        if (dirFile.delete()) {
            Logg.i(TAG,"-->deleteDirectory --> delete "+dir+" success");
            return true;
        } else {
            return false;
        }
    }

    /**文件重命名
     * @param path 文件目录
     * @param oldname  原来的文件名
     * @param newname 新文件名
     */
    public static void renameFile(String path,String oldname,String newname){
        if(!oldname.equals(newname)){//新的文件名和以前文件名不同时,才有必要进行重命名
            File oldfile=new File(path+"/"+oldname);
            File newfile=new File(path+"/"+newname);
            if(!oldfile.exists()){
                return;//重命名文件不存在
            }
            if(newfile.exists())//若在该目录下已经有一个文件和新文件名相同，则不允许重命名
                Logg.e(TAG,"-->renameFile --> "+newname+" file has exists");
            else{
                oldfile.renameTo(newfile);
            }
        }else{
            Logg.e(TAG,"-->renameFile --> The new file name is the same as the old file name....");
        }
    }


}