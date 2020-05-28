package com.google.cloud.solutions.transformation;

import com.google.api.services.bigquery.model.TableRow;
import com.google.cloud.solutions.common.MeasurementSummary;

import org.apache.beam.sdk.transforms.SerializableFunction;
import org.apache.beam.sdk.values.KV;

public class TableRowMapper implements SerializableFunction<KV<String, MeasurementSummary>, TableRow> {

    private static final long serialVersionUID = 1L;

    @Override
    public TableRow apply(KV<String, MeasurementSummary> input) {
        MeasurementSummary summary = input.getValue();
        String metricType = input.getKey().split(":")[1];

        return new TableRow().set("DeviceNumId", summary.getDeviceInfo().getDeviceNumId())
                .set("DeviceId", summary.getDeviceInfo().getDeviceId())
                .set("RegistryId", summary.getDeviceInfo().getDeviceRegistryId()).set("MetricType", metricType)
                .set("PeriodStart", summary.getStart()).set("PeriodEnd", summary.getEnd())
                .set("MaxValue", summary.getMax()).set("MinValue", summary.getMin())
                .set("Average", summary.getAverage()); //TODO: add dynamtic mapping
    }

}