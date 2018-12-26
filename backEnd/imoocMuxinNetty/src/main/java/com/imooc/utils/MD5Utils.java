package com.imooc.utils;

import org.apache.commons.codec.binary.Base64;

import java.security.MessageDigest;

/**
 * Created by donezio on 12/22/18.
 */
public class MD5Utils {
    /**
     * @Description: encrypt using md5
     */
    public static String getMD5Str(String strValue) throws Exception {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        String newstr = Base64.encodeBase64String(md5.digest(strValue.getBytes()));
        return newstr;
    }

    public static void main(String[] args) {
        try {
            String md5 = getMD5Str("imooc");
            System.out.println(md5);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
