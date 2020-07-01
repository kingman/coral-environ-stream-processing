package com.google.cloud.solutions.utils;

import com.google.cloud.solutions.common.Measurement;

import org.apache.beam.sdk.transforms.SerializableFunction;
import org.joda.time.Duration;
import org.joda.time.Instant;

public class OldMeasurementFilter implements SerializableFunction<Measurement, Boolean>{
    private static final long serialVersionUID = -6889744369750410224L;
    private final int allowedSkewMinutes;

    public OldMeasurementFilter(int allowedSkewMinutes) {
        this.allowedSkewMinutes = allowedSkewMinutes;
    }

    @Override
    public Boolean apply(Measurement measurement) {
        Instant breakInstant = Instant.now().minus(Duration.standardMinutes(allowedSkewMinutes));
        return measurement.getTimestamp().isAfter(breakInstant);
    }
}