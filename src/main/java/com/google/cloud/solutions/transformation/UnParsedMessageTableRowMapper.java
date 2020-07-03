package com.google.cloud.solutions.transformation;

import com.google.api.services.bigquery.model.TableRow;
import com.google.cloud.solutions.common.UnParsedMessage;
import com.google.cloud.solutions.utils.TableRowMapperUtil;

import org.apache.beam.sdk.transforms.SerializableFunction;

public class UnParsedMessageTableRowMapper implements SerializableFunction<UnParsedMessage, TableRow> {

    private static final long serialVersionUID = 1L;

    @Override
    public TableRow apply(UnParsedMessage input) {
        TableRow tableRow = new TableRow();
        TableRowMapperUtil.mapUnParsedMessage(input, tableRow);
        return tableRow;
    }

}