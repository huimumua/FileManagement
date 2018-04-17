// IFileManagerAidlInterface.aidl
package com.askey.dvr.cdr7010.filemanagement;

import com.askey.dvr.cdr7010.filemanagement.ItemData;

// Declare any non-default types here with import statements

interface IFileManagerAidlInterface {

     /**
     * openSdcard
     * */
     boolean openSdcard();

     /**
     * closeSdcard
     * */
     boolean closeSdcard();

    /**
    * Type : NORMAL EVENT PARKING PICTURE SYSTEM
    * */
   List <String> getAllFilesByType(String type);

    /**
    * Type : NORMAL EVENT PARKING PICTURE SYSTEM
    * */
   List <ItemData> getAllFileByType(String type);

    /**
    * path : file Absolute Path
    * */
    boolean deleteFile(String path);

    /**
    * pathList : files Absolute Path
    * */
//    boolean deleteFileByGroup(ArrayList pathList);

    /**
    * pathList : files Absolute Path
    * */
    boolean deleteFileByFolder(String path);



}
