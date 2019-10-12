package com.example.demo.util.dto;

import java.util.List;

/**
 * 个性化短信参数
 */
public class PersonalityParamsDTO {
    private String timerTime;
    private String extendedCode;
    private String requestTime;
    private String requestValidPeriod;
    CustomSmsIdAndMobileAndContent[] smses;

    public CustomSmsIdAndMobileAndContent[] getSmses() {
        return smses;
    }

    public void setSmses(CustomSmsIdAndMobileAndContent[] smses) {
        this.smses = smses;
    }

    public String getTimerTime() {
        return timerTime;
    }

    public void setTimerTime(String timerTime) {
        this.timerTime = timerTime;
    }

    public String getExtendedCode() {
        return extendedCode;
    }

    public void setExtendedCode(String extendedCode) {
        this.extendedCode = extendedCode;
    }

    public String getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(String requestTime) {
        this.requestTime = requestTime;
    }

    public String getRequestValidPeriod() {
        return requestValidPeriod;
    }

    public void setRequestValidPeriod(String requestValidPeriod) {
        this.requestValidPeriod = requestValidPeriod;
    }

}
