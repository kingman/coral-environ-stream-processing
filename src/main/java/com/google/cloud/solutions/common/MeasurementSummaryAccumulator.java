package com.google.cloud.solutions.common;

import java.io.Serializable;

import org.joda.time.Duration;
import org.joda.time.Instant;

public class MeasurementSummaryAccumulator implements Serializable {
    private static final long serialVersionUID = -3978327933098560442L;

    private DeviceInfo deviceInfo;
    private double min;
    private double max;
    private double total;
    private int count;
    private String startStr;
    private Instant start;
    private String endStr;
    private Instant end;

    public MeasurementSummaryAccumulator() {
        this.min = Double.MAX_VALUE;
        this.max = Double.MIN_VALUE;
        this.total = 0.0;
        this.count = 0;
        this.start = Instant.now().plus(Duration.standardDays(10));
        this.end = Instant.EPOCH;
    }

    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(DeviceInfo deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getStartStr() {
        return startStr;
    }

    public void setStartStr(String startStr) {
        this.startStr = startStr;
    }

    public Instant getStart() {
        return start;
    }

    public void setStart(Instant start) {
        this.start = start;
    }

    public String getEndStr() {
        return endStr;
    }

    public void setEndStr(String endStr) {
        this.endStr = endStr;
    }

    public Instant getEnd() {
        return end;
    }

    public void setEnd(Instant end) {
        this.end = end;
    }

}