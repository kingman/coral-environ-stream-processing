package com.google.cloud.solutions.utils;

import com.google.cloud.solutions.common.Measurement;

import org.joda.time.Instant;

public class MeasurementFunctions {

    public static Instant extractTimeStamp(Measurement input) {
        return input.getTimestamp();
    }

    public static String generateKey(Measurement input) {
        return input.getDeviceInfo().getDeviceNumId() + ":" + input.getMetricType();
    }

}