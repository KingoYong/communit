package com.example.demo.util.open;

import com.example.demo.util.AES;
import com.example.demo.util.GZIPUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class OpenAndPut {
    /**
     * 将加密压缩的字节数组 解密、解压、转化为字符串
     *
     * @param data   加密压缩的字节数组
     * @param code   请求状态码
     * @param gzip   是否启用GZIP压缩；on :是
     * @param encode 字符集
     * @return
     */
    public String open(byte[] data, String code, String gzip, String encode) {
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

    /**
     * 将字符串 转化为字节数组、压缩、加密
     *
     * @param backStr 要转化的字符串
     * @param encode  字符集
     * @param gzip    是否启用GZIP压缩；on :是
     * @return 返回处理后的字节数组
     */
    public byte[] put(String backStr, String encode, String gzip) {
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
