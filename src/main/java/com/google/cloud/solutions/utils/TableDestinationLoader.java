package com.google.cloud.solutions.utils;

import java.util.HashMap;
import java.util.Map;

import com.google.api.services.bigquery.model.TableReference;
import com.google.cloud.solutions.common.DeviceInfo;

import org.apache.beam.sdk.io.gcp.bigquery.TableDestination;

public class TableDestinationLoader {
    private static final String DESTINATION_TABLE_METADATA_PREFIX = "destination-table-";
    private static final String DESTINATION_DATASET_METADATA_PREFIX = "destination-dataset-";
    private static Map<String, TableDestination> destinationCache = new HashMap<>();

    public static TableDestination getDestination(DeviceInfo deviceInfo, String messageType) {
        final String devicePath = getCacheKey(deviceInfo, messageType);
        if (destinationCache.containsKey(devicePath)) {
            return destinationCache.get(devicePath);
        }

        String table = fetchMetadata(deviceInfo, DESTINATION_TABLE_METADATA_PREFIX+messageType);
        if (table == null) {
            throw new RuntimeException(String.format("No destination table find for device: %s", devicePath));
        }

        String dataset = fetchMetadata(deviceInfo, DESTINATION_DATASET_METADATA_PREFIX+messageType);
        if (dataset == null) {
            throw new RuntimeException(String.format("No destination dataset find for device: %s", devicePath));
        }

        TableDestination tableDestination = new TableDestination(
                new TableReference().setProjectId(deviceInfo.getProjectId()).setDatasetId(dataset).setTableId(table),
                "Dynamically loaded destination");

        destinationCache.put(devicePath, tableDestination);

        return destinationCache.get(devicePath);
    }

    private static String fetchMetadata(DeviceInfo deviceInfo, String metadataKey) {
        try {
            return GCPIoTCoreUtil.getDeviceMetadata(deviceInfo.getDeviceId(), deviceInfo.getProjectId(),
                    deviceInfo.getDeviceRegistryLocation(), deviceInfo.getDeviceRegistryId()).get(metadataKey);
        } catch (Exception e) {
            return null;
        }
    }

    private static String getCacheKey(DeviceInfo deviceInfo, String messageType) {
        return String.format("projects/%s/locations/%s/registries/%s/devices/%s/%s", deviceInfo.getProjectId(),
                deviceInfo.getDeviceRegistryLocation(), deviceInfo.getDeviceRegistryId(), deviceInfo.getDeviceId(), messageType);

    }
}