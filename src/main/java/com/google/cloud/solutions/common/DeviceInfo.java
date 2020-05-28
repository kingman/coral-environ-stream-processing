package com.google.cloud.solutions.common;

import java.io.Serializable;

public class DeviceInfo implements Serializable {
    private static final long serialVersionUID = -6511745175411897304L;

    private String deviceNumId;
    private String deviceId;
    private String deviceRegistryId;
    private String deviceRegistryLocation;
    private String projectId;
    private String subFolder;

    public String getDeviceNumId() {
        return deviceNumId;
    }

    public void setDeviceNumId(String deviceNumId) {
        this.deviceNumId = deviceNumId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceRegistryId() {
        return deviceRegistryId;
    }

    public void setDeviceRegistryId(String deviceRegistryId) {
        this.deviceRegistryId = deviceRegistryId;
    }

    public String getDeviceRegistryLocation() {
        return deviceRegistryLocation;
    }

    public void setDeviceRegistryLocation(String deviceRegistryLocation) {
        this.deviceRegistryLocation = deviceRegistryLocation;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getSubFolder() {
        return subFolder;
    }

    public void setSubFolder(String subFolder) {
        this.subFolder = subFolder;
    }
}