package com.google.cloud.solutions.transformation;

import com.google.api.services.bigquery.model.TableRow;
import com.google.cloud.solutions.common.HumanDetectionResult;
import com.google.cloud.solutions.utils.TableRowMapperUtil;

import org.apache.beam.sdk.transforms.SerializableFunction;

public class HumanDetectionResultTableRowMapper implements SerializableFunction<HumanDetectionResult, TableRow> {

    private static final long serialVersionUID = 1L;

    @Override
    public TableRow apply(HumanDetectionResult input) {
        TableRow tableRow = new TableRow();
        TableRowMapperUtil.mapHumanDetectionResult(input, tableRow);
        return tableRow;
    }

}