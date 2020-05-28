package com.google.cloud.solutions.transformation;

import org.apache.beam.sdk.transforms.DoFn;

import com.google.cloud.solutions.common.DeviceInfo;
import com.google.cloud.solutions.common.Measurement;

import org.apache.beam.sdk.io.gcp.pubsub.PubsubMessage;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.joda.time.Instant;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.DateTimeZone;

public class PubsubMessageToMeasurement extends DoFn<PubsubMessage, Measurement> {

    private static final long serialVersionUID = 8694500821507947562L;
    private static DateTimeFormatter TIME_STAMP_FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSSSSS")
            .withZone(DateTimeZone.forID("UTC"));

    @ProcessElement
    public void processElement(@Element PubsubMessage message, OutputReceiver<Measurement> receiver) {
        DeviceInfo deviceInfo = createDeviceInfo(message);

        JsonObject payloadJson = new JsonParser().parse(new String(message.getPayload())).getAsJsonObject();
        JsonArray measurementsJson = payloadJson.get("enviro").getAsJsonArray();

        measurementsJson.forEach(measurementJson -> {

            String timestampStr = measurementJson.getAsJsonObject().get("ts").getAsString();
            Instant timestamp = Instant.parse(timestampStr, TIME_STAMP_FORMAT);

            measurementJson.getAsJsonObject().entrySet().forEach(entry -> {
                if (!entry.getKey().equalsIgnoreCase("ts")) {
                    Measurement measurement = new Measurement();
                    measurement.setDeviceInfo(deviceInfo);

                    measurement.setTimestampStr(timestampStr);
                    measurement.setTimestamp(timestamp);

                    measurement.setMetricType(entry.getKey());
                    measurement.setValue(entry.getValue().getAsDouble());
                    receiver.output(measurement); // (metric, metric.timestamp);
                }
            });
        });
    }

    private DeviceInfo createDeviceInfo(PubsubMessage message) {
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