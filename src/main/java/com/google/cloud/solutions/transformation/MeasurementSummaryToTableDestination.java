package com.google.cloud.solutions.transformation;

import java.util.Map;

import com.google.api.services.bigquery.model.TableSchema;
import com.google.cloud.solutions.common.DeviceInfo;
import com.google.cloud.solutions.common.MeasurementSummary;

import org.apache.beam.sdk.io.gcp.bigquery.DynamicDestinations;
import org.apache.beam.sdk.io.gcp.bigquery.TableDestination;
import org.apache.beam.sdk.values.ValueInSingleWindow;

public class MeasurementSummaryToTableDestination extends DynamicDestinations<Map<String, MeasurementSummary>, DeviceInfo> {

    
    @Override
    public TableDestination getTable(DeviceInfo destination) {
        return null;
    }

    @Override
    public TableSchema getSchema(DeviceInfo destination) {
        return null;
    }

    @Override
    public DeviceInfo getDestination(ValueInSingleWindow<Map<String, MeasurementSummary>> element) {
        return null;
    }

}