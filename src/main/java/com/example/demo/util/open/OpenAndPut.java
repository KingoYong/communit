package com.example.demo.util.open;

import com.example.demo.util.AES;
import com.example.demo.util.GZIPUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class OpenAndPut {
    public String open(byte[] data,String code,String gzip,String encode){
        String result = "";
        try {//解密
            data = AES.decrypt(data, "1111111111111111".getBytes(), "AES/ECB/PKCS5Padding");
        } catch (Exception e) {
            e.printStackTrace();
            code = "ERROR_ENCRYPTION";//解密失败【安全接口专用】
        }
        if ("on".equals(gzip)) {
            try {//解压
                data = GZIPUtils.decompress(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {//转换为字符串
            result = new String(data, encode);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }

    public byte[] put(String backStr,String encode,String gzip){
        byte[] bytes = null;
        try {//转换为字节数组
            bytes = backStr.getBytes(encode);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if ("on".equals(gzip)) {
            try {//压缩
                bytes = GZIPUtils.compress(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return AES.encrypt(bytes, "1111111111111111".getBytes(), "AES/ECB/PKCS5Padding");
    }
}
