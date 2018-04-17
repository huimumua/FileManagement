// IFileManagerAidlInterface.aidl
package com.askey.dvr.cdr7010.filemanagement;

import com.askey.dvr.cdr7010.filemanagement.ItemData;

// Declare any non-default types here with import statements

interface IFileManagerAidlInterface {

      /**
      *  Purpose: 1.Choice folderType to openfile
                  2.Get file_num from "Table.config"
                  3.If "Free" folder have extension for folderType, use it to open file.
         Input:  mount path, open filename,
                 folderType: Event, Manual, Normal, Parking, Picture, System
         Output: FILE Pointer
        ** If file number > folderType file_num, return NULL; ***/
     long openSdcard(String mount_path, String filename, String folderType);

     /**
     *  Purpose: Close opened file
       Input: Opened FILE Pointer
       Output: bool, true = 1, false = 0;
     * */
     boolean closeSdcard(long filePointer);

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
