package com.google.cloud.solutions.common;

import java.io.Serializable;

public class UnParsedMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    private DeviceInfo deviceInfo;
    private String message;

    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(DeviceInfo deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}