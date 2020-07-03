package com.google.cloud.solutions.transformation;

import com.google.api.services.bigquery.model.TableSchema;
import com.google.cloud.solutions.common.DeviceInfo;
import com.google.cloud.solutions.common.UnParsedMessage;
import com.google.cloud.solutions.utils.TableDestinationLoader;
import com.google.cloud.solutions.utils.TableSchemaLoader;

import org.apache.beam.sdk.io.gcp.bigquery.DynamicDestinations;
import org.apache.beam.sdk.io.gcp.bigquery.TableDestination;
import org.apache.beam.sdk.values.ValueInSingleWindow;

public class UnParsedMessageToTableDestination extends DynamicDestinations<UnParsedMessage, DeviceInfo> {

    private static final long serialVersionUID = 1L;
    private static final String MESSAGE_TYPE = "unknown-message";

    @Override
    public DeviceInfo getDestination(ValueInSingleWindow<UnParsedMessage> element) {
        return element.getValue().getDeviceInfo();
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