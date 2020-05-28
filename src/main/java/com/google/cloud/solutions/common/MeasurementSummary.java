package com.google.cloud.solutions.common;

import java.io.Serializable;

public class MeasurementSummary implements Serializable {
    private static final long serialVersionUID = -232472685038863331L;
    private Double min;
    private Double max;
    private Double average;
    private String start;
    private String end;

    public Double getMin() {
        return min;
    }

    public void setMin(Double min) {
        this.min = min;
    }

    public Double getMax() {
        return max;
    }

    public void setMax(Double max) {
        this.max = max;
    }

    public Double getAverage() {
        return average;
    }

    public void setAverage(Double average) {
        this.average = average;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }
}