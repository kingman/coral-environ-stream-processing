package com.google.cloud.solutions.utils;

import com.google.cloud.solutions.common.DeviceInfo;

import org.apache.beam.sdk.io.gcp.pubsub.PubsubMessage;

public class PubsubMessageUtil {
    public static DeviceInfo extractDeviceInfo(PubsubMessage message) {
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setDeviceNumId(message.getAttribute("deviceNumId"));
        deviceInfo.setDeviceId(message.getAttribute("deviceId"));
        deviceInfo.setDeviceRegistryId(message.getAttribute("deviceRegistryId"));
        deviceInfo.setDeviceRegistryLocation(message.getAttribute("deviceRegistryLocation"));
        deviceInfo.setProjectId(message.getAttribute("projectId"));
        deviceInfo.setSubFolder(message.getAttribute("subFolder"));
        return deviceInfo;
    }
}