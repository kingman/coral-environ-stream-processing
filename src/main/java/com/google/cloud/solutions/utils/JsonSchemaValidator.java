package com.google.cloud.solutions.utils;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.beam.sdk.io.gcp.pubsub.PubsubMessage;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;

public class JsonSchemaValidator {

    private static final String SCHEMA_METADATA_KEY = "metrics-schema";
    private static Map<String, Schema> schemaCache = new HashMap<>();

    public static Boolean validate(PubsubMessage message) {
        Schema schema = getSchema(message);
        if (schema == null) { // not schema available to validate against
            return true;
        }
        try {
            JSONObject jsonSubject = new JSONObject(new JSONTokener(new ByteArrayInputStream(message.getPayload())));
            schema.validate(jsonSubject);
        } catch (ValidationException e) {
            return false;
        }
        return true;
    }

    private static Schema getSchema(PubsubMessage message) {
        final String devicePath = getCacheKey(message);

        if (schemaCache.containsKey(devicePath)) {
            return schemaCache.get(devicePath);
        }

        String schemaString = getSchemaString(message);
        if (schemaString == null) { // not schema available to validate against
            schemaCache.put(devicePath, null);
            return null;
        }
        JSONObject jsonSchema = new JSONObject(new JSONTokener(new ByteArrayInputStream(schemaString.getBytes())));
        Schema schema = SchemaLoader.load(jsonSchema);
        schemaCache.put(devicePath, schema);
        return schemaCache.get(devicePath);
    }

    private static String getCacheKey(PubsubMessage message) {
        return String.format("projects/%s/locations/%s/registries/%s/devices/%s", message.getAttribute("projectId"),
                message.getAttribute("deviceRegistryLocation"), message.getAttribute("deviceRegistryId"),
                message.getAttribute("deviceId"));

    }

    private static String getSchemaString(PubsubMessage message) {
        try {
            return GCPIoTCoreUtil
                    .getDeviceMetadata(message.getAttribute("deviceId"), message.getAttribute("projectId"),
                            message.getAttribute("deviceRegistryLocation"), message.getAttribute("deviceRegistryId"))
                    .get(SCHEMA_METADATA_KEY);
        } catch (Exception e) {
            return null;
        }

    }

}