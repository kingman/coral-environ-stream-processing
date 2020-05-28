package com.google.cloud.solutions.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.api.services.bigquery.model.TableFieldSchema;
import com.google.api.services.bigquery.model.TableSchema;
import com.google.cloud.solutions.common.DeviceInfo;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.google.common.collect.ImmutableList;

public class TableSchemaLoader {
    private static final String TABLE_SCHEMA_METADATA_KEY = "table-schema";
    private static Map<String, TableSchema> schemaCache = new HashMap<>();

    public static TableSchema getSchema(DeviceInfo deviceInfo) {
        final String devicePath = getCacheKey(deviceInfo);

        if (schemaCache.containsKey(devicePath)) {
            return schemaCache.get(devicePath);
        }

        String schemaStr = fetchSchema(deviceInfo);
        if (schemaStr == null) {
            throw new RuntimeException(String.format("No table scheme find for device: %s", devicePath));
        }
        TableSchema schema = createScheme(schemaStr);
        schemaCache.put(devicePath, schema);
        return schemaCache.get(devicePath);

    }

    private static String getCacheKey(DeviceInfo deviceInfo) {
        return String.format("projects/%s/locations/%s/registries/%s/devices/%s", deviceInfo.getProjectId(),
                deviceInfo.getDeviceRegistryLocation(), deviceInfo.getDeviceRegistryId(), deviceInfo.getDeviceId());

    }

    private static String fetchSchema(DeviceInfo deviceInfo) {
        try {
            return GCPIoTCoreUtil
                    .getDeviceMetadata(deviceInfo.getDeviceId(), deviceInfo.getProjectId(),
                            deviceInfo.getDeviceRegistryLocation(), deviceInfo.getDeviceRegistryId())
                    .get(TABLE_SCHEMA_METADATA_KEY);
        } catch (Exception e) {
            return null;
        }
    }

    private static TableSchema createScheme(String schemaStr) {
        JsonArray fields = new JsonParser().parse(schemaStr).getAsJsonArray();
        List<TableFieldSchema> fieldSchemas = new ArrayList<>();

        fields.forEach(field -> {
            JsonObject fieldObj = field.getAsJsonObject();
            fieldSchemas.add(new TableFieldSchema().setName(fieldObj.get("name").getAsString())
                    .setType(fieldObj.get("type").getAsString()).setMode(fieldObj.get("mode").getAsString()));
        });
        return new TableSchema().setFields(ImmutableList.copyOf(fieldSchemas));
    }
}