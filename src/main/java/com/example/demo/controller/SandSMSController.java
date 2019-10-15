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

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;


@Controller
public class SandSMSController {
    @Autowired
    HttpServletRequest request;
    @Resource
    HttpServletResponse response;

    @PostMapping("/inter/sendPersonalitySMS")
    @ResponseBody
    public byte[] sendPersonalitySMS(@RequestBody byte[] data) {
        String code = "SUCCESS";
        String result;
        String gzip = request.getHeader("gzip");
        String encode = request.getHeader("encode");
        String appId = request.getHeader("appId");
        OpenAndPut openAndPut = new OpenAndPut();
        result = openAndPut.open(data, code, gzip, encode);
        PersonalityParamsDTO param = JSON.parseObject(result, PersonalityParamsDTO.class);
        long currentTime = System.currentTimeMillis();
        long requestTime = Long.parseLong(param.getRequestTime());
        long timePeriod = Long.parseLong(param.getRequestValidPeriod() + "000");
        SqlSessionUtil sessionUtil = new SqlSessionUtil();
        SqlSession sqlSession = sessionUtil.getSqlSession();
        ResponseMapper responseMapper = sqlSession.getMapper(ResponseMapper.class);
        ResponseAppIdMapper responseAppIdMapper = sqlSession.getMapper(ResponseAppIdMapper.class);
        ReportOffset reportOffset = responseAppIdMapper.findAppId(appId);
        if (reportOffset == null) {
            code = "ERROR_APPID"; //	AppId错误
        } else if (requestTime + timePeriod < currentTime) {
            code = "ERROR_REQUEST_NO_VALID";//请求超时【安全接口专用】
        } else if (param.getSmses().length > 500) {
            code = "ERROR_MOBILE_NUMBER";//号码数量过多
        }
        List<SmsResponse> smsResponseList = new LinkedList<>();
        List<ReportResponse> reportResponseList = new LinkedList<>();
        for (CustomSmsIdAndMobileAndContent content : param.getSmses()) {
            SmsResponse smsResponse = new SmsResponse();
            ReportResponse reportResponse = new ReportResponse();
            //send接口返回数据
            smsResponse.setMobile(content.getMobile());
            smsResponse.setCustomSmsId(content.getCustomSmsId());
            smsResponse.setSmsId(String.valueOf(System.currentTimeMillis()));
            smsResponseList.add(smsResponse);
            //getReport接口返回数据
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
        sqlSession.commit();
        sqlSession.close();
        response.setHeader("result", code);
        String smsResponse = JsonHelper.toJsonString(smsResponseList);
        return openAndPut.put(smsResponse, encode, gzip);
    }
}
