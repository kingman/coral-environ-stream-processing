package com.google.cloud.solutions.transformation;

import com.google.api.services.bigquery.model.TableSchema;
import com.google.cloud.solutions.common.DeviceInfo;
import com.google.cloud.solutions.common.MeasurementSummary;
import com.google.cloud.solutions.utils.TableDestinationLoader;
import com.google.cloud.solutions.utils.TableSchemaLoader;

import org.apache.beam.sdk.io.gcp.bigquery.DynamicDestinations;
import org.apache.beam.sdk.io.gcp.bigquery.TableDestination;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.ValueInSingleWindow;

public class MeasurementSummaryToTableDestination
        extends DynamicDestinations<KV<String, MeasurementSummary>, DeviceInfo> {

    private static final long serialVersionUID = 120337140386439827L;
    private static final String MESSAGE_TYPE = "measurement";

    @Override
    public DeviceInfo getDestination(ValueInSingleWindow<KV<String, MeasurementSummary>> element) {
        return element.getValue().getValue().getDeviceInfo();
    }

    @Override
    public TableDestination getTable(DeviceInfo deviceInfo) {
        return TableDestinationLoader.getDestination(deviceInfo, MESSAGE_TYPE);
    }

    @Override
    public TableSchema getSchema(DeviceInfo deviceInfo) {
        return TableSchemaLoader.getSchema(deviceInfo, MESSAGE_TYPE);
    }

}