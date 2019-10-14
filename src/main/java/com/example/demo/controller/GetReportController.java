package com.example.demo.controller;

import com.alibaba.fastjson.JSON;
import com.example.demo.mapper.ResponseAppIdMapper;
import com.example.demo.mapper.ResponseMapper;
import com.example.demo.util.AES;
import com.example.demo.util.GZIPUtils;
import com.example.demo.util.JsonHelper;
import com.example.demo.util.open.OpenAndPut;
import com.example.demo.util.request.ReportRequest;
import com.example.demo.util.response.ReportResponse;
import com.example.demo.util.response.ReportResponseReturn;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

@Controller
public class GetReportController {
    @Autowired
    HttpServletRequest request;

    @PostMapping("/inter/getReport")
    public byte[] getReport(@RequestBody byte[] data) {
        String code = "SUCCESS";
        String result = null;
        String gzip = request.getHeader("gzip");
        String encode = request.getHeader("encode");
        String appId = request.getHeader("appId");
        OpenAndPut openAndPut = new OpenAndPut();
        result = openAndPut.open(data, code, gzip, encode);
        /*try {//解密
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
        }*/
        ReportRequest param = JSON.parseObject(result, ReportRequest.class);
        long requestTime = param.getRequestTime();
        long timePeriod = Long.parseLong(param.getRequestValidPeriod() + "000");
        long currentTime = System.currentTimeMillis();
        if (appId == null) {
            code = "ERROR_APPID"; //	AppId错误
        } else if (requestTime + timePeriod < currentTime) {
            code = "ERROR_REQUEST_NO_VALID";//请求超时【安全接口专用】
        } else if (param.getNumber() > 500) {
            code = "ERROR_MOBILE_NUMBER";//号码数量不能超过500
        }
        // 指定全局配置文件
        String resource = "mybatis-config.xml";
        // 读取配置文件
        InputStream inputStream = null;
        try {
            inputStream = Resources.getResourceAsStream(resource);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 构建sqlSessionFactory
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        // 获取sqlSession
        SqlSession sqlSession = sqlSessionFactory.openSession();
        ResponseMapper responseMapper = sqlSession.getMapper(ResponseMapper.class);
        ResponseAppIdMapper responseAppIdMapper = sqlSession.getMapper(ResponseAppIdMapper.class);
/*        System.out.println("------------------");
        String appId = "123";
        long offset = 0;
        int number = 2;
        String encode = "UTF-8";
        String gzip = "on";
        System.out.println("------------------");*/
        int number = param.getNumber();
        //todo
        List<ReportResponseReturn> reportResponseList;
        synchronized (this) {
            long offset = responseAppIdMapper.findOffset(appId);
            reportResponseList = responseMapper.findByAppIdAndOffset(appId, offset, number);
            offset = offset + reportResponseList.size();
            responseAppIdMapper.updateOffset(appId, offset);
        }
        sqlSession.commit();
        sqlSession.close();
        String reportResponse = JsonHelper.toJsonString(reportResponseList) + "#" + code;
        return openAndPut.put(reportResponse, encode, gzip);
        /*byte[] bytes = null;
        try {//转换为字节数组
            bytes = reportResponse.getBytes(encode);
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
        return AES.encrypt(bytes, "1111111111111111".getBytes(), "AES/ECB/PKCS5Padding");*/
    }
}
