package com.example.demo.controller;

import com.example.demo.mapper.AppMapper;
import com.example.demo.util.dto.AppParamDto;
import com.example.demo.util.getsqlsession.SqlSessionUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class AppSendController {
    @PostMapping("/third/push/v1/batch")
    @ResponseBody
    public Map<String, Object> getApp(@RequestBody byte[] data) {
        Map<String, Object> map = new HashMap<String, Object>();
        String str = new String(data);
        String alert = str.split("&")[0].split("=")[1];
        if (StringUtils.isBlank(alert)) {
            map.put("code", 400);
            map.put("reason", "alert参数为空");
            return map;
        } else if (alert.length() > 4000) {
            map.put("code", 400);
            map.put("reason","长度超4000");
            return map;
        }

        AppParamDto appParamDto = new AppParamDto();
        String[] strs = str.split("&");
        for (String s : strs) {
            String[] split = s.split("=");
            if ("alert".equals(split[0])) {
                appParamDto.setAlert(split[1]);
            } else if ("userIds".equals(split[0])) {
                appParamDto.setIds(split[1]);
            }else if ("title".equals(split[0])) {
                appParamDto.setTitle(split[1]);
            }else if ("timestamp".equals(split[0])) {
                appParamDto.setTimestamp(split[1]);
            } else {
                appParamDto.setMd5(split[1]);
            }
        }
        int num = (int) (Math.random() * 10);
        if (num >= 2) {
            //成功
            appParamDto.setCode("200");
            appParamDto.setState("success");
            map.put("code", 200);
        } else {//服务器响应失败
            appParamDto.setCode("500");
            appParamDto.setState("服务器响应失败");
            map.put("code", 500);
            map.put("reason","服务器响应失败");
        }
        SqlSessionUtil sessionUtil = new SqlSessionUtil();
        SqlSession sqlSession = sessionUtil.getSqlSession();
        AppMapper appMapper = sqlSession.getMapper(AppMapper.class);
        appMapper.insert(appParamDto);
        sqlSession.commit();
        sqlSession.close();
        return map;
    }
}
