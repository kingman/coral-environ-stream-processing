package com.google.cloud.solutions.utils;

import com.google.cloud.solutions.common.Measurement;

import org.joda.time.Duration;
import org.joda.time.Instant;

public class OldMeasurementFilter {
    private final int allowedSkewMinutes;

    public OldMeasurementFilter(int allowedSkewMinutes) {
        this.allowedSkewMinutes = allowedSkewMinutes;
    }

    public Boolean apply(Measurement measurement) {
        Instant breakInstant = Instant.now().minus(Duration.standardMinutes(allowedSkewMinutes));
        return measurement.getTimestamp().isAfter(breakInstant);
    }
}