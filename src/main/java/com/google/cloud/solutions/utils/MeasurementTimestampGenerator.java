package com.google.cloud.solutions.utils;

import com.google.cloud.solutions.common.Measurement;

import org.apache.beam.sdk.transforms.SerializableFunction;
import org.joda.time.Instant;

public class MeasurementTimestampGenerator implements SerializableFunction<Measurement, Instant> {

    private static final long serialVersionUID = 1L;

    @Override
    public Instant apply(Measurement input) {
        return input.getTimestamp();
    }
    
}