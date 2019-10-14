package com.example.demo.controller;

import com.alibaba.fastjson.JSON;
import com.example.demo.mapper.ResponseAppIdMapper;
import com.example.demo.mapper.ResponseMapper;
import com.example.demo.util.JsonHelper;
import com.example.demo.util.dto.CustomSmsIdAndMobileAndContent;
import com.example.demo.util.dto.PersonalityParamsDTO;
import com.example.demo.util.dto.ReportOffset;
import com.example.demo.util.getsqlsession.SqlSessionUtil;
import com.example.demo.util.open.OpenAndPut;
import com.example.demo.util.response.ReportResponse;
import com.example.demo.util.response.SmsResponse;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;


@Controller
public class SandSMSController {
    @Autowired
    HttpServletRequest request;
    /*@Autowired
    private ResponseMapper responseMapper;
    @Autowired
    private ResponseAppIdMapper responseAppIdMapper;*/

    @PostMapping("/inter/sendPersonalitySMS")
    @ResponseBody
    public byte[] sendPersonalitySMS(@RequestBody byte[] data) {
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
        PersonalityParamsDTO param = JSON.parseObject(result, PersonalityParamsDTO.class);
        long requestTime = Long.parseLong(param.getRequestTime());
        long timePeriod = Long.parseLong(param.getRequestValidPeriod() + "000");
        long currentTime = System.currentTimeMillis();
        if (appId == null) {
            code = "ERROR_APPID"; //	AppId错误
        } else if (requestTime + timePeriod < currentTime) {
            code = "ERROR_REQUEST_NO_VALID";//请求超时【安全接口专用】
        } else if (param.getSmses().length > 500) {
            code = "ERROR_MOBILE_NUMBER";//号码数量过多
        }
       /* System.out.println("-------------------");
        String appId = "123";
        PersonalityParamsDTO param = new PersonalityParamsDTO();
        param.setExtendedCode("extendedCode");
        CustomSmsIdAndMobileAndContent c1 = new CustomSmsIdAndMobileAndContent();
        c1.setContent("haha");
        c1.setCustomSmsId("11");
        c1.setMobile("15538867822");
        CustomSmsIdAndMobileAndContent c2 = new CustomSmsIdAndMobileAndContent();
        c2.setContent("haha22");
        c2.setCustomSmsId("22");
        c2.setMobile("15538867822");
        CustomSmsIdAndMobileAndContent[] sms = new CustomSmsIdAndMobileAndContent[]{c1,c2};
        param.setSmses(sms);
        String encode = "UTF-8";
        String gzip = "on";
        System.out.println("-----------------------");*/
        /*// 指定全局配置文件
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
        SqlSession sqlSession = sqlSessionFactory.openSession();*/
        SqlSessionUtil sessionUtil = new SqlSessionUtil();
        SqlSession sqlSession = sessionUtil.getSqlSession();
        ResponseMapper responseMapper = sqlSession.getMapper(ResponseMapper.class);
        ResponseAppIdMapper responseAppIdMapper = sqlSession.getMapper(ResponseAppIdMapper.class);

        List<SmsResponse> smsResponseList = new LinkedList<>();
        List<ReportResponse> reportResponseList = new LinkedList<>();
        for (CustomSmsIdAndMobileAndContent content : param.getSmses()) {
            SmsResponse smsResponse = new SmsResponse();
            ReportResponse reportResponse = new ReportResponse();
            //接口返回报告
            smsResponse.setMobile(content.getMobile());
            smsResponse.setCustomSmsId(content.getCustomSmsId());
            smsResponse.setSmsId(String.valueOf(System.currentTimeMillis()));
            smsResponseList.add(smsResponse);
            //返回getReport
            reportResponse.setMobile(content.getMobile());
            reportResponse.setSmsId(String.valueOf(System.currentTimeMillis()));
            reportResponse.setCustomSmsId(content.getCustomSmsId());
            reportResponse.setReceiveTime(String.valueOf(LocalDateTime.now()));
            reportResponse.setSubmitTime(reportResponse.getReceiveTime());
            reportResponse.setExtendedCode(param.getExtendedCode());
            reportResponse.setAppId(appId);
            int num = (int) (Math.random() * 10);
            if (num >= 4) {
                //成功
                reportResponse.setState("DELIVRD");
                reportResponse.setDesc("成功");
            } else if (num == 3) {//黑名单失败
                reportResponse.setState("FAIL_BLACK_LIST");
                reportResponse.setDesc("黑名单失败");
            } else if (num == 2) {//拦截失败
                reportResponse.setState("FAIL_INTERCEPT");
                reportResponse.setDesc("拦截失败");
            } else if (num == 1) {//运营商响应失败
                reportResponse.setState("FAIL_RESPONSE");
                reportResponse.setDesc("运营商响应失败");
            } else if (num == 0) {//用户退订
                reportResponse.setState("FAIL_UNSUBSCRIBE");
                reportResponse.setDesc("用户退订");
            }
            reportResponseList.add(reportResponse);
        }
        responseMapper.insertByBatch(reportResponseList);
        synchronized (this) {
            ReportOffset reportOffset = responseAppIdMapper.findAppId(appId);
            if (reportOffset == null) {
                ReportOffset reportOffset1 = new ReportOffset();
                reportOffset1.setAppId(appId);
                reportOffset1.setOffset(0L);
                responseAppIdMapper.insert(reportOffset1);
            }
        }
        sqlSession.commit();
        sqlSession.close();
        String smsResponse = JsonHelper.toJsonString(smsResponseList) + "#" + code;
        return openAndPut.put(smsResponse, encode, gzip);
        /*byte[] bytes = null;
        try {//转换为字节数组
            bytes = smsResponse.getBytes(encode);
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
