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
import com.askey.dvr.cdr7010.filemanagement.util.DateUtil;
import com.askey.dvr.cdr7010.filemanagement.util.Logg;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        try {
            Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            File file = new File(filePath);
            if(file!=null){
                Uri uri = Uri.fromFile(file);
//        Uri uri = FileProvider.getUriForFile(ctx, BuildConfig.APPLICATION_ID, file);
                scanIntent.setData(uri);
//                ctx.sendBroadcast(scanIntent);
                ctx.sendBroadcastAsUser(scanIntent, android.os.Process.myUserHandle());
            }
        }catch (Exception e){
            Logg.e(TAG,"scanFileAsync->Exception="+e.getMessage());
        }
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

    /**
     * 分别获取camera1  camera2 normal文件的个数
     * */
    public static Map getNormalFileCount() {
        ArrayList <String> list = getAllVideoList();
        int normal1Count = 0,normal2Count = 0;
        for (String path : list ){
            if(path.contains(Const.NORMAL_DIR) && path.contains("_1") ){
                normal1Count ++ ;
            }else if(path.contains(Const.NORMAL_DIR) && path.contains("_2") ){
                normal2Count ++ ;
            }
        }
        Map fileCountMap =new HashMap();
        fileCountMap.put(Const.NORMAL_1_DIR,normal1Count);
        fileCountMap.put(Const.NORMAL_2_DIR,normal2Count);
        return fileCountMap;
    }

    public static Map getAllFileCount() {
        ArrayList <String> list = getAllVideoList();
        int normalCount = 0,eventCount = 0,parkingCount = 0;
        for (String path : list ){
            if(path.contains(Const.NORMAL_DIR)){
                normalCount ++ ;
            }else if(path.contains(Const.EVENT_DIR)){
                eventCount ++ ;
            }else if(path.contains(Const.PARKING_DIR)){
                parkingCount ++ ;
            }
        }
        Map fileCountMap =new HashMap();
        fileCountMap.put(Const.NORMAL_DIR,normalCount);
        fileCountMap.put(Const.EVENT_DIR,eventCount);
        fileCountMap.put(Const.PARKING_DIR,parkingCount);
        return fileCountMap;
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
            String id=cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media._ID));
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
            if(path.endsWith(".mp4")){
                fileList.add(path);
            }

        }
        if(cursor != null && !cursor.isClosed()){
            cursor.close();
        }
        return fileList;
    }

    public static ArrayList <ItemData> getVideos(String type){
        Logg.i(TAG,"==getVideos=="+type);

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

        String currentYYYYMMDD = "1970";
        List<ItemData> group = new ArrayList<ItemData>();
        while(cursor.moveToNext())
        {
            String id = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media._ID));
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

//            Bitmap bitmap = MediaStore.Images.Thumbnails.getThumbnail(resolver,
//                    Long.valueOf(id), MediaStore.Images.Thumbnails.MICRO_KIND, null);
            Logg.i(TAG,"==name=="+name);
            if(Const.NORMAL_1_DIR.equals(type) ) {
                Logg.i(TAG, "==Const.NORMAL_1_DIR.equals(type)==");
//                normalFileGrouping(path, name, DATE_MODIFIED, id, currentYYYYMMDD,  fileList, group);
                if(!name.contains("_2")){
                    if (path.contains(Const.SDCARD_PATH + Const.BACK_SLASH_1 + Const.NORMAL_DIR) && path.endsWith(".mp4")) {
                        if (name.length() == 16 || name.length() == 18 || name.length() == 24 || name.length() == 26) {
                            File mFile = new File(path);
                            if (mFile.exists()) {
                                DATE_MODIFIED = String.valueOf(mFile.lastModified());
                                Logg.i(TAG, "==path==" + path);
                                String yyyyMMDD = name.substring(0, 6);
                                long fileCreateTime = DateUtil.getFileCreateTime(name);
                                if (currentYYYYMMDD.equals(yyyyMMDD) && group.size() < 10) {
                                    ItemData groupitem = new ItemData();
                                    groupitem.setFileTime(fileCreateTime);
                                    groupitem.setFilePath(path);
                                    groupitem.setFileName(name);
                                    groupitem.setDir(false);
                                    groupitem.setMediaID(Integer.valueOf(id));
                                    group.add(groupitem);
                                } else {
                                    currentYYYYMMDD = yyyyMMDD;
                                    group = new ArrayList<ItemData>();
                                    ItemData groupitem = new ItemData();
                                    groupitem.setFileTime(fileCreateTime);
                                    groupitem.setFilePath(path);
                                    groupitem.setFileName(name);
                                    groupitem.setDir(false);
                                    groupitem.setMediaID(Integer.valueOf(id));
                                    group.add(groupitem);

                                    ItemData itemData = new ItemData();
                                    itemData.setFileTime(fileCreateTime);
                                    itemData.setFilePath(path);
                                    itemData.setFileName(name);
                                    itemData.setDir(true);
                                    itemData.setDirFileItem(group);
                                    fileList.add(itemData);
                                }
                            }
                        }
                    }
                }
//                fileGrouping(path, Const.NORMAL_DIR, name, DATE_MODIFIED, id, currentYYYYMMDD,  fileList, group);
            }else if(Const.NORMAL_2_DIR.equals(type) ){
                Logg.i(TAG,"==Const.NORMAL_2_DIR.equals(type)==");
                if(name.contains("_2")){
                    if( path.contains(Const.SDCARD_PATH+Const.BACK_SLASH_1+Const.NORMAL_DIR)  && path.endsWith(".mp4")){
                        if(name.length()==16 || name.length()==18  || name.length() == 24 || name.length() == 26){
                            File mFile = new File(path);
                            if(mFile.exists()){
                                DATE_MODIFIED = String.valueOf(mFile.lastModified());
                                Logg.i(TAG,"==path=="+path);
                                String yyyyMMDD = name.substring(0,6);
                                long fileCreateTime = DateUtil.getFileCreateTime(name);
                                if(currentYYYYMMDD.equals(yyyyMMDD) && group.size()<10){
                                    ItemData groupitem = new ItemData();
                                    groupitem.setFileTime(fileCreateTime);
                                    groupitem.setFilePath(path);
                                    groupitem.setFileName(name);
                                    groupitem.setDir(false);
                                    groupitem.setMediaID(Integer.valueOf(id));
                                    group.add(groupitem);
                                }else{
                                    currentYYYYMMDD = yyyyMMDD;
                                    group= new ArrayList<ItemData>();
                                    ItemData groupitem = new ItemData();
                                    groupitem.setFileTime(fileCreateTime);
                                    groupitem.setFilePath(path);
                                    groupitem.setFileName(name);
                                    groupitem.setDir(false);
                                    groupitem.setMediaID(Integer.valueOf(id));
                                    group.add(groupitem);

                                    ItemData itemData= new ItemData();
                                    itemData.setFileTime(fileCreateTime);
                                    itemData.setFilePath(path);
                                    itemData.setFileName(name);
                                    itemData.setDir(true);
                                    itemData.setDirFileItem(group);
                                    fileList.add(itemData);
                                }
                            }
                        }
                    }
//                    fileGrouping(path, Const.NORMAL_DIR, name, DATE_MODIFIED, id, currentYYYYMMDD,  fileList, group);
                }
            }else{
                Logg.i(TAG,"==Const.NORMAL_2_DIR.equals(type)=2222=");
                if( path.contains(Const.SDCARD_PATH+Const.BACK_SLASH_1+type)  && path.endsWith(".mp4")){
                    if(name.length()==16 || name.length()==18  || name.length() == 24 || name.length() == 26){
                        File mFile = new File(path);
                        if(mFile.exists()){
                            DATE_MODIFIED = String.valueOf(mFile.lastModified());
                            Logg.i(TAG,"==path=="+path);
                            long fileCreateTime = DateUtil.getFileCreateTime(name);
                            ItemData itemData= new ItemData();
                            itemData.setFileTime(fileCreateTime);
                            itemData.setFilePath(path);
                            itemData.setFileName(name);
                            itemData.setDir(false);
                            itemData.setMediaID(Integer.valueOf(id));
                            fileList.add(itemData);
                        }
                    }
                }
            }
        }
        if(cursor != null && !cursor.isClosed()){
            cursor.close();
        }
        return fileList;
    }

    private static void normalFileGrouping(String path,String name,String DATE_MODIFIED,String id,String currentYYYYMMDD, ArrayList <ItemData> fileList,List<ItemData> group) {
        if( path.contains(Const.SDCARD_PATH+Const.BACK_SLASH_1+Const.NORMAL_DIR)  && path.endsWith(".mp4")){
            if(name.length()==16 || name.length()==18  || name.length() == 24 || name.length() == 26){
                File mFile = new File(path);
                if(mFile.exists()){
                    DATE_MODIFIED = String.valueOf(mFile.lastModified());
                    Logg.i(TAG,"==path=="+path);
                    String yyyyMMDD = name.substring(0,6);
                    long fileCreateTime = DateUtil.getFileCreateTime(name);
                    if(currentYYYYMMDD.equals(yyyyMMDD) && group.size()<10){
                        ItemData groupitem = new ItemData();
                        groupitem.setFileTime(fileCreateTime);
                        groupitem.setFilePath(path);
                        groupitem.setFileName(name);
                        groupitem.setDir(false);
                        groupitem.setMediaID(Integer.valueOf(id));
                        group.add(groupitem);
                    }else{
                        currentYYYYMMDD = yyyyMMDD;
                        group= new ArrayList<ItemData>();
                        ItemData groupitem = new ItemData();
                        groupitem.setFileTime(fileCreateTime);
                        groupitem.setFilePath(path);
                        groupitem.setFileName(name);
                        groupitem.setDir(false);
                        groupitem.setMediaID(Integer.valueOf(id));
                        group.add(groupitem);

                        ItemData itemData= new ItemData();
                        itemData.setFileTime(fileCreateTime);
                        itemData.setFilePath(path);
                        itemData.setFileName(name);
                        itemData.setDir(true);
                        itemData.setDirFileItem(group);
                        fileList.add(itemData);
                    }
                }
            }
        }
    }

    private static void fileGrouping(String path,String type,String name,String DATE_MODIFIED,String id,String currentYYYYMMDD, ArrayList <ItemData> fileList,List<ItemData> group) {
        if( path.contains(Const.SDCARD_PATH+Const.BACK_SLASH_1+type)  && path.endsWith(".mp4")){
            if(name.length()==16 || name.length()==18  || name.length() == 24 || name.length() == 26){
                File mFile = new File(path);
                if(mFile.exists()){
                    DATE_MODIFIED = String.valueOf(mFile.lastModified());
                    Logg.i(TAG,"==path=="+path);
                    if(!type.equals(Const.NORMAL_DIR)){
                        long fileCreateTime = DateUtil.getFileCreateTime(name);
                        ItemData itemData= new ItemData();
                        itemData.setFileTime(fileCreateTime);
                        itemData.setFilePath(path);
                        itemData.setFileName(name);
                        itemData.setDir(false);
                        itemData.setMediaID(Integer.valueOf(id));
                        fileList.add(itemData);
                    }else{
                        String yyyyMMDD = name.substring(0,6);
                        long fileCreateTime = DateUtil.getFileCreateTime(name);

                        if(currentYYYYMMDD.equals(yyyyMMDD) && group.size()<10){
                            ItemData groupitem = new ItemData();
                            groupitem.setFileTime(fileCreateTime);
                            groupitem.setFilePath(path);
                            groupitem.setFileName(name);
                            groupitem.setDir(false);
                            groupitem.setMediaID(Integer.valueOf(id));
                            group.add(groupitem);
                        }else{
                            currentYYYYMMDD = yyyyMMDD;
                            group= new ArrayList<ItemData>();
                            ItemData groupitem = new ItemData();
                            groupitem.setFileTime(fileCreateTime);
                            groupitem.setFilePath(path);
                            groupitem.setFileName(name);
                            groupitem.setDir(false);
                            groupitem.setMediaID(Integer.valueOf(id));
                            group.add(groupitem);

                            ItemData itemData= new ItemData();
                            itemData.setFileTime(fileCreateTime);
                            itemData.setFilePath(path);
                            itemData.setFileName(name);
                            itemData.setDir(true);
//                        itemData.setMediaID(Integer.valueOf(id));
                            itemData.setDirFileItem(group);
                            fileList.add(itemData);
                        }
                    }
                }
            }
        }
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
            String id=cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media._ID));
            //获取图片的名称
            String name=cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
            //获取图片的大小
            String size=cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.SIZE));
            //获取图片的路径
            String path=cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            if( path.contains(Const.SDCARD_PATH+Const.BACK_SLASH_1+Const.PICTURE_DIR) ){
                fileList.add(path);
            }
        }
        if(cursor != null && !cursor.isClosed()){
            cursor.close();
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
            String id=cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media._ID));
            //获取图片的名称
            String name=cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
            //获取图片的大小
            String size=cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.SIZE));
            //获取图片的路径
            String path=cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            if( path.contains(Const.SDCARD_PATH+Const.BACK_SLASH_1+Const.PICTURE_DIR) ){
                File mFile = new File(path);
                if(mFile.exists()){
                   String DATE_MODIFIED = String.valueOf(mFile.lastModified());
                    long fileCreateTime = DateUtil.getFileCreateTime(name);
                    ItemData itemData= new ItemData();
                    itemData.setFileTime(fileCreateTime);
                    itemData.setFilePath(path);
                    itemData.setFileName(name);
                    itemData.setDir(false);
                    itemData.setMediaID(Integer.valueOf(id));
                    fileList.add(itemData);
                }

            }
        }
        if(cursor != null && !cursor.isClosed()){
            cursor.close();
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
    public static boolean delete(String fileName,String type) {
        File file = new File(fileName);
        if (!file.exists()) {
            Logg.e(TAG,"-->delete --> delete "+fileName+" not exists");
            return false;
        } else {
            if (file.isFile())
                return deleteFile(fileName,type);
            else
                return deleteDirectory(fileName,type);
        }
    }

    /**
     * 删除一组数据
     * **/
    public static boolean deleteFileByGroup(List <String> pathArray) {
        if(pathArray.size()>0){
            for (String path : pathArray){
                if(!deleteFile(path,true)){
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * 删除单个文件
     *
     * @param fileName
     *            要删除的文件的文件名
     * @return 单个文件删除成功返回true，否则返回false
     */
    public static boolean deleteFile(String fileName,boolean isSync) {
        File file = new File(fileName);
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile()) {
            String filePath = file.getPath();
            String file_name = file.getName();
            boolean result = false;
            if(file_name.contains("_2")){
                result = FileManager.getSingInstance().FH_Delete(fileName,2);
            }else{
                result = FileManager.getSingInstance().FH_Delete(fileName,1);
            }
            Logg.i(TAG,"-->deleteFile --> FH_Delete fileName"+fileName+" result =="+result);
            if(!fileName.contains(".jpg")){
                String nmeaPath = null;
                String hashPath = null;
                if(fileName.contains("EVENT")){
                    nmeaPath = fileName.replaceAll("mp4", "nmea").replaceAll("EVENT", "SYSTEM/NMEA/EVENT");
                    hashPath = fileName.replaceAll("mp4", "hash").replaceAll("EVENT", "HASH_EVENT");
                }else if(fileName.contains("NORMAL")){
                    nmeaPath = fileName.replaceAll("mp4", "nmea").replaceAll("NORMAL", "SYSTEM/NMEA/NORMAL");
                    hashPath = fileName.replaceAll("mp4", "hash").replaceAll("NORMAL", "HASH_NORMAL");
                }
                //SYSTEM/NMEA/NORMAL
                deleteNmeaFile(nmeaPath);
                //这里还需要删除所创建的hash文件
                deleteNmeaFile(hashPath);
            }

            if (result) {
                //这里清除ContentProvader数据库
                try {
                    if(isSync){
                        FileManager.getSingInstance().FH_Sync();
                    }
                    syncDeleteFile(filePath);
//                scanFileAsync(FileManagerApplication.getAppContext(),fileName);
                }catch (Exception e){
                    Logg.e(TAG,"syncDeleteFile->Exception->"+e.getMessage());
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 删除单个文件
     *
     * @param fileName
     *            要删除的文件的文件名
     * @return 单个文件删除成功返回true，否则返回false
     */
    public static boolean deleteFile(String fileName,String type) {
        if(type.equals(Const.NORMAL_1_DIR)){
            if(!fileName.contains("_2")){
                return deleteFile(fileName,true) ;
            }
            return true;
        }
        if(type.equals(Const.NORMAL_2_DIR)){
            if(fileName.contains("_2")){
                return deleteFile(fileName,true) ;
            }
            return true;
        }
        return deleteFile(fileName,true) ;
    }

    private static void deleteNmeaFile(String filePath) {
        boolean fileResult = false;
        if(filePath!=null){
            File mFile = new File(filePath);
            // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
            if (mFile.exists()) {
                String fileName = mFile.getName();
                if(fileName.contains("_2")){
                    fileResult = FileManager.getSingInstance().FH_Delete(filePath,2);
                }else{
                    fileResult = FileManager.getSingInstance().FH_Delete(filePath,1);
                }
            }
            Logg.i(TAG,"-->deleteFile --> FH_Delete filePath"+filePath+" fileResult =="+fileResult);
        }
    }

    public static void syncDeleteFile(String filePath) {
        //删除多媒体数据库中的数据
        try {
            ContentResolver contentResolver = FileManagerApplication.getAppContext().getContentResolver();
            if(filePath.endsWith(".mp4")){
                int res = contentResolver.delete(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        MediaStore.Video.Media.DATA + "= \"" + filePath+"\"",
                        null);

                Logg.i(TAG, "-->syncDeleteFile-->"+res);
            }else if (filePath.endsWith(".jpg")||filePath.endsWith(".png")||filePath.endsWith(".bmp")){
                int res = contentResolver.delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        MediaStore.Images.Media.DATA + "= \"" + filePath+"\"",
                        null);
                // res > 0  success  else fail
                Logg.i(TAG, "-->syncDeleteFile-->"+res);
            }
        }catch (Exception e){
            Logg.e(TAG, "-->syncDeleteFile-->Exception"+e.getMessage());
        }

    }

    public static boolean deleteFileByFolder(String type) {
        String path ="";
        if(type.equals(Const.NORMAL_DIR)){
            path = Const.SDCARD_PATH+Const.BACK_SLASH_1+Const.NORMAL_DIR;
        }else if(type.equals(Const.EVENT_DIR)){
            path = Const.SDCARD_PATH+Const.BACK_SLASH_1+Const.EVENT_DIR;
        }else if(type.equals(Const.PARKING_DIR)){
            path = Const.SDCARD_PATH+Const.BACK_SLASH_1+Const.PARKING_DIR;
        }else if(type.equals(Const.PICTURE_DIR)){
            path = Const.SDCARD_PATH+Const.BACK_SLASH_1+Const.PICTURE_DIR;
        }else if(type.equals(Const.NORMAL_1_DIR ) || type.equals(Const.NORMAL_2_DIR) ){
            path = Const.SDCARD_PATH+Const.BACK_SLASH_1+Const.NORMAL_DIR;
        }
        Logg.i(TAG,"=deleteFileByFolder==path=="+path);
        return deleteDirectoryByType(path,type);
    }
    /**
     * 删除目录及目录下的文件
     *
     * @param dir
     *            要删除的目录的文件路径
     * @return 目录删除成功返回true，否则返回false
     */
    public static boolean deleteDirectory(String dir,String type) {
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
                flag = deleteFile(files[i].getAbsolutePath(),type);
                if (!flag)
                    break;
            }
            // 删除子目录
            else if (files[i].isDirectory()) {
                flag = deleteDirectory(files[i]
                        .getAbsolutePath(),type);
                if (!flag)
                    break;
            }else{
                String str = files[i].getAbsolutePath();
                        File dirFile1 = new File(str);
                Logg.i(TAG,"=====files.length==222=="+str);
                try {
                    Logg.i(TAG,"=====files.length==222=="+dirFile1.delete());
                }catch (Exception e){
                    Logg.e(TAG,"=====files.length==222=="+e.getMessage());
                }
            }

        }
        if (!flag) {
            Logg.e(TAG,"-->deleteDirectory --> delete "+dir+" failed");
            return false;
        }
        return dirFile.delete();
    }

    public static boolean deleteDirectoryByType(String dir,String type) {
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
        if(files.length>0){
            for (int i = 0; i < files.length; i++) {
                // 删除子文件
                if (files[i].isFile()) {
                    flag = deleteFile(files[i].getAbsolutePath(),type);
                    if (!flag){
                        break;
                    }
                }else if (files[i].isDirectory()) { // 删除子目录
                    flag = deleteDirectory(files[i].getAbsolutePath(),type);
                    if (!flag){
                        break;
                    }
                }else{
                    String str = files[i].getAbsolutePath();
                    File dirFile1 = new File(str);
                    Logg.i(TAG,"=====files.length==222=="+str);
                    try {
                        Logg.i(TAG,"=====files.length==222=="+dirFile1.delete());
                    }catch (Exception e){
                        Logg.e(TAG,"=====files.length==222=="+e.getMessage());
                    }
                }
            }
        }

        if (!flag) {
            Logg.e(TAG,"-->deleteDirectory --> delete "+dir+" failed");
            return false;
        }
        return true;
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
