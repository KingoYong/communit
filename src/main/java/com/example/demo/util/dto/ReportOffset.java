package com.example.demo.util.dto;

/**
 * 返回状态的offset
 */
public class ReportOffset {
    private String appId;
    private long offset;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }
}
