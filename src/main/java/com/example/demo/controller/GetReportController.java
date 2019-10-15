package com.example.demo.controller;

import com.alibaba.fastjson.JSON;
import com.example.demo.mapper.ResponseAppIdMapper;
import com.example.demo.mapper.ResponseMapper;
import com.example.demo.util.JsonHelper;
import com.example.demo.util.dto.ReportOffset;
import com.example.demo.util.getsqlsession.SqlSessionUtil;
import com.example.demo.util.open.OpenAndPut;
import com.example.demo.util.request.ReportRequest;
import com.example.demo.util.response.ReportResponseReturn;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
public class GetReportController {
    @Autowired
    HttpServletRequest request;
    @Resource
    HttpServletResponse response;

    @PostMapping("/inter/getReport")
    @ResponseBody
    public byte[] getReport(@RequestBody byte[] data) {
        String code = "SUCCESS";
        String result = null;
        String gzip = request.getHeader("gzip");
        String encode = request.getHeader("encode");
        String appId = request.getHeader("appId");
        OpenAndPut openAndPut = new OpenAndPut();
        result = openAndPut.open(data, code, gzip, encode);
        ReportRequest param = JSON.parseObject(result, ReportRequest.class);
        long currentTime = System.currentTimeMillis();
        long timePeriod = Long.parseLong(param.getRequestValidPeriod() + "000");
        long requestTime = param.getRequestTime();
        SqlSessionUtil sessionUtil = new SqlSessionUtil();
        SqlSession sqlSession = sessionUtil.getSqlSession();
        ResponseMapper responseMapper = sqlSession.getMapper(ResponseMapper.class);
        ResponseAppIdMapper responseAppIdMapper = sqlSession.getMapper(ResponseAppIdMapper.class);
        ReportOffset reportOffset = responseAppIdMapper.findAppId(appId);
        if (reportOffset == null) {
            code = "ERROR_APPID";//AppId错误
            response.setHeader("result", code);
            return null;
        } else if (requestTime + timePeriod < currentTime) {
            code = "ERROR_REQUEST_NO_VALID";//请求超时
            response.setHeader("result", code);
            return null;
        } else if (param.getNumber() > 500) {
            code = "ERROR_MOBILE_NUMBER";//号码数量过多
            response.setHeader("result", code);
            return null;
        }
        int number = param.getNumber();
        List<ReportResponseReturn> reportResponseList;
        long offset = responseAppIdMapper.findOffset(appId);
        reportResponseList = responseMapper.findByAppIdAndOffset(appId, offset, number);
        offset = offset + reportResponseList.size();
        responseAppIdMapper.updateOffset(appId, offset);
        sqlSession.commit();
        sqlSession.close();
        response.setHeader("result", code);
        String reportResponse = JsonHelper.toJsonString(reportResponseList);
        return openAndPut.put(reportResponse, encode, gzip);
    }
}
