package com.askey.dvr.cdr7010.filemanagement.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * 项目名称：filemanagement
 * 类描述：
 * 创建人：skysoft  charles.bai
 * 创建时间：2018/4/26 9:19
 * 修改人：skysoft
 * 修改时间：2018/4/26 9:19
 * 修改备注：
 */
public class StringUtil {

    public static String toUtf8(String str) {
        String result = null;
        try {
            result = new String(str.getBytes("UTF-8"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Get XML String of utf-8
     *
     * @return XML-Formed string
     */
    public static String getUTF8String(String xml) {
        // A StringBuffer Object
        StringBuffer sb = new StringBuffer();
        sb.append(xml);
        String str = "";
        String strUTF8="";
        try {
            str = new String(sb.toString().getBytes("utf-8"));
            strUTF8 = URLEncoder.encode(str, "utf-8");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // return to String Formed
        return strUTF8;
    }


}
