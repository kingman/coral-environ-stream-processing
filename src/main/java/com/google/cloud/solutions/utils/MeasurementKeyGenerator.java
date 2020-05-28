package com.google.cloud.solutions.utils;

import com.google.cloud.solutions.common.Measurement;

import org.apache.beam.sdk.transforms.SerializableFunction;

public class MeasurementKeyGenerator implements SerializableFunction<Measurement, String> {

    private static final long serialVersionUID = 1L;

    @Override
    public String apply(Measurement input) {
        return input.getDeviceInfo().getDeviceNumId() + ":" + input.getMetricType();
    }

}