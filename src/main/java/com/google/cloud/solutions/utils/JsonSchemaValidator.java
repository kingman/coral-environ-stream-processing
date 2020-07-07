package com.google.cloud.solutions.utils;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.beam.sdk.io.gcp.pubsub.PubsubMessage;
import org.apache.beam.sdk.transforms.SerializableFunction;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;

public class JsonSchemaValidator implements SerializableFunction<PubsubMessage, Boolean>{

    private static final long serialVersionUID = 2540822907982136981L;
    private final String schemaMetadataKey;
    private Map<String, Schema> schemaCache;

    public JsonSchemaValidator(String schemaMetadataKey) {
        this.schemaMetadataKey = schemaMetadataKey;
        this.schemaCache = new HashMap<>();
    }

    @Override
    public Boolean apply(PubsubMessage message) {
        Schema schema = getSchema(message);
        if (schema == null) { // not schema available to validate against
            return true;
        }
        try {
            JSONObject jsonSubject = new JSONObject(new JSONTokener(new ByteArrayInputStream(message.getPayload())));
            schema.validate(jsonSubject);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private Schema getSchema(PubsubMessage message) {
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

    private String getCacheKey(PubsubMessage message) {
        return String.format("projects/%s/locations/%s/registries/%s/devices/%s", message.getAttribute("projectId"),
                message.getAttribute("deviceRegistryLocation"), message.getAttribute("deviceRegistryId"),
                message.getAttribute("deviceId"));

    }

    private String getSchemaString(PubsubMessage message) {
        try {
            return GCPIoTCoreUtil
                    .getDeviceMetadata(message.getAttribute("deviceId"), message.getAttribute("projectId"),
                            message.getAttribute("deviceRegistryLocation"), message.getAttribute("deviceRegistryId"))
                    .get(schemaMetadataKey);
        } catch (Exception e) {
            return null;
        }

    }

}