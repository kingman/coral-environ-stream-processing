package com.google.cloud.solutions.utils;

import java.util.ArrayList;
import java.util.List;

import com.google.api.services.bigquery.model.TableRow;
import com.google.cloud.solutions.common.Detection;
import com.google.cloud.solutions.common.DeviceInfo;
import com.google.cloud.solutions.common.HumanDetectionResult;
import com.google.cloud.solutions.common.MeasurementSummary;

public class TableRowMapperUtil {

    private static void mapDeviceInfo(DeviceInfo deviceInfo, TableRow tableRow) {
        tableRow
        .set("DeviceNumId", deviceInfo.getDeviceNumId())
        .set("DeviceId", deviceInfo.getDeviceId())
        .set("RegistryId", deviceInfo.getDeviceRegistryId());
    }

    public static void mapMeasurementSummary(MeasurementSummary summary, TableRow tableRow) {
        mapDeviceInfo(summary.getDeviceInfo(), tableRow);
        tableRow
        .set("PeriodStart", summary.getStart())
        .set("PeriodEnd", summary.getEnd())
        .set("MaxValue", summary.getMax())
        .set("MinValue", summary.getMin())
        .set("Average", summary.getAverage());
    }

    public static void mapHumanDetectionResult(HumanDetectionResult result, TableRow tableRow) {
        mapDeviceInfo(result.getDeviceInfo(), tableRow);
        tableRow
        .set("TimeStamp", result.getTimestampStr())
        .set("Count", result.getCount());

        List<TableRow> detections = new ArrayList<>();
        for (Detection detection : result.getDetections()) {
            TableRow detectionRecord = new TableRow();
            mapDetection(detection, detectionRecord);
            detections.add(detectionRecord);
        }
        tableRow.set("Detections", detections);
    }

    private static void mapDetection(Detection detection, TableRow tableRow) {
        tableRow
        .set("Label", detection.getLabel())
        .set("Score", detection.getScore())
        .set("X1", detection.getX1())
        .set("Y1", detection.getY1())
        .set("X2", detection.getX2())
        .set("Y2", detection.getY2());
    }

}