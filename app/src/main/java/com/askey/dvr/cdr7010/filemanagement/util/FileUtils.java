package com.askey.dvr.cdr7010.filemanagement.util;

import java.io.File;

/**
 * 项目名称：filemanagement
 * 类描述：
 * 创建人：skysoft  charles.bai
 * 创建时间：2018/5/2 13:28
 * 修改人：skysoft
 * 修改时间：2018/5/2 13:28
 * 修改备注：
 */
public class FileUtils {

    private static final String TAG = "FileUtils";

    public static  boolean fileIsExists(String path){
        try{
            File file=new File(path);
            if(!file.exists()){
                return false;
            }
        }catch (Exception e) {
            // TODO: handle exception
            return false;
        }
        return true;
    }


}
