package com.google.cloud.solutions.common;

import java.io.Serializable;
import java.util.List;

public class HumanDetectionResult implements Serializable {
    private static final long serialVersionUID = 5515621156850803416L;
    private DeviceInfo deviceInfo;
    private String timestampStr;
    private Integer count;
    private List<Detection> detections;

    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(DeviceInfo deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public String getTimestampStr() {
        return timestampStr;
    }

    public void setTimestampStr(String timestampStr) {
        this.timestampStr = timestampStr;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public List<Detection> getDetections() {
        return detections;
    }

    public void setDetections(List<Detection> detections) {
        this.detections = detections;
    }
        
}