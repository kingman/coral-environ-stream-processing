package com.google.cloud.solutions.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.cloudiot.v1.CloudIot;
import com.google.api.services.cloudiot.v1.CloudIotScopes;
import com.google.api.services.cloudiot.v1.model.Device;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.collect.ImmutableMap;

public class GCPIoTCoreUtil {
    private static final String APP_NAME = "enviro-processor";

    private static CloudIot service;

    private static String CREDENTIAL_PATH = "";

    private static Map<String, Map<String, String>> metadataCache = new HashMap<>();

    public static CloudIot getService() {
        if (service == null) {
            try {
                initializeService();
            } catch (Exception e) {
                throw new Error("could not initial IoT Core service", e);
            }

        }
        return service;
    }

    private static void initializeService() throws GeneralSecurityException, IOException {
        GoogleCredentials credential = createCredential();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credential);
        service = new CloudIot.Builder(GoogleNetHttpTransport.newTrustedTransport(), jsonFactory, requestInitializer)
                .setApplicationName(APP_NAME).build();
    }

    private static GoogleCredentials createCredential() throws IOException {
        GoogleCredentials credential;
        if (!CREDENTIAL_PATH.isEmpty()) {
            credential = GoogleCredentials.fromStream(new FileInputStream(CREDENTIAL_PATH));
        } else {
            credential = GoogleCredentials.getApplicationDefault().createScoped(CloudIotScopes.all());
        }
        return credential.createScoped(CloudIotScopes.all());
    }

    public static Map<String, String> getDeviceMetadata(String deviceId, String projectId, String cloudRegion,
            String registryName) throws IOException {
        final String devicePath = String.format("projects/%s/locations/%s/registries/%s/devices/%s", projectId,
                cloudRegion, registryName, deviceId);
        if (metadataCache.containsKey(devicePath)) {
            return metadataCache.get(devicePath);
        }
        Device device = getDevice(devicePath);
        metadataCache.put(devicePath, ImmutableMap.copyOf(device.getMetadata()));
        return metadataCache.get(devicePath);
    }

    public static Device getDevice(String devicePath) throws IOException {
        return getService().projects().locations().registries().devices().get(devicePath).execute();
    }

    public static void setCredentialPath(String credentialPath) {
        CREDENTIAL_PATH = credentialPath;
    }
}