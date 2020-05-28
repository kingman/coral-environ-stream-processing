package com.google.cloud.solutions.transformation;

import com.google.api.services.bigquery.model.TableReference;
import com.google.api.services.bigquery.model.TableSchema;
import com.google.cloud.solutions.common.DeviceInfo;
import com.google.cloud.solutions.common.MeasurementSummary;
import com.google.cloud.solutions.utils.TableSchemaLoader;

import org.apache.beam.sdk.io.gcp.bigquery.DynamicDestinations;
import org.apache.beam.sdk.io.gcp.bigquery.TableDestination;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.ValueInSingleWindow;

public class MeasurementSummaryToTableDestination
        extends DynamicDestinations<KV<String, MeasurementSummary>, DeviceInfo> {

    private static final long serialVersionUID = 120337140386439827L;

    @Override
    public DeviceInfo getDestination(ValueInSingleWindow<KV<String, MeasurementSummary>> element) {
        return element.getValue().getValue().getDeviceInfo();
    }

    @Override
    public TableDestination getTable(DeviceInfo destination) {
        return new TableDestination(
                new TableReference().setProjectId(destination.getProjectId()).setDatasetId("foglamp") // TODO make
                                                                                                      // dynamic
                        .setTableId("dynamic_written"), // TODO make dynamic
                "Table  dynamic_written");
    }

    @Override
    public TableSchema getSchema(DeviceInfo destination) {
        return TableSchemaLoader.getSchema(destination);
    }

}