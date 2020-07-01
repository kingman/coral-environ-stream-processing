package com.google.cloud.solutions.transformation;

import com.google.api.services.bigquery.model.TableRow;
import com.google.cloud.solutions.common.MeasurementSummary;
import com.google.cloud.solutions.utils.TableRowMapperUtil;

import org.apache.beam.sdk.transforms.SerializableFunction;
import org.apache.beam.sdk.values.KV;

public class MeasurementSummaryTableRowMapper implements SerializableFunction<KV<String, MeasurementSummary>, TableRow> {

    private static final long serialVersionUID = 1L;

    @Override
    public TableRow apply(KV<String, MeasurementSummary> input) {
        MeasurementSummary summary = input.getValue();
        String metricType = input.getKey().split(":")[1];

        TableRow tableRow = new TableRow();
        tableRow.set("MetricType", metricType);
        TableRowMapperUtil.mapMeasurementSummary(summary, tableRow);

        return tableRow;
    }

}