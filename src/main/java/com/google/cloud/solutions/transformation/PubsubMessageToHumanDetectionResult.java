package com.google.cloud.solutions.transformation;

import java.util.ArrayList;
import java.util.List;

import com.google.cloud.solutions.common.Detection;
import com.google.cloud.solutions.common.DeviceInfo;
import com.google.cloud.solutions.common.HumanDetectionResult;
import com.google.cloud.solutions.utils.PubsubMessageUtil;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.beam.sdk.io.gcp.pubsub.PubsubMessage;
import org.apache.beam.sdk.transforms.DoFn;

public class PubsubMessageToHumanDetectionResult extends DoFn<PubsubMessage, HumanDetectionResult> {
    
    private static final long serialVersionUID = 1L;

    private static final String PERSON_PREFIX = "person_";
    private static final String LABEL_POSTFIX = "_label";
    private static final String SCORE_POSTFIX = "_score";
    private static final String X1_POSTFIX = "_x1";
    private static final String Y1_POSTFIX = "_y1";
    private static final String X2_POSTFIX = "_x2";
    private static final String Y2_POSTFIX = "_y2";

    @ProcessElement
    public void processElement(@Element PubsubMessage message, OutputReceiver<HumanDetectionResult> receiver) {
        DeviceInfo deviceInfo = PubsubMessageUtil.extractDeviceInfo(message);

        JsonObject payloadJson = new JsonParser().parse(new String(message.getPayload())).getAsJsonObject();
        JsonArray detectionsJson = payloadJson.get("Detection Results").getAsJsonArray();

        detectionsJson.forEach(detectionJson -> {
            HumanDetectionResult result = new HumanDetectionResult();
            result.setDeviceInfo(deviceInfo);
            result.setTimestampStr(detectionJson.getAsJsonObject().get("ts").getAsString());
            int count = detectionJson.getAsJsonObject().get("count").getAsInt();
            result.setCount(count);
            List<Detection> detections = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                detections.add(extractDetection(detectionJson, i+1));                
            }
            result.setDetections(detections);
            receiver.output(result);
        });
    }

    private Detection extractDetection(JsonElement detectionJson, int index) {
        Detection detection = new Detection();
        detection.setLabel(detectionJson.getAsJsonObject().get(PERSON_PREFIX+index+LABEL_POSTFIX).getAsString());
        detection.setScore(detectionJson.getAsJsonObject().get(PERSON_PREFIX+index+SCORE_POSTFIX).getAsString());
        detection.setX1(detectionJson.getAsJsonObject().get(PERSON_PREFIX+index+X1_POSTFIX).getAsInt());
        detection.setY1(detectionJson.getAsJsonObject().get(PERSON_PREFIX+index+Y1_POSTFIX).getAsInt());
        detection.setX2(detectionJson.getAsJsonObject().get(PERSON_PREFIX+index+X2_POSTFIX).getAsInt());
        detection.setY2(detectionJson.getAsJsonObject().get(PERSON_PREFIX+index+Y2_POSTFIX).getAsInt());
        return detection;
    }
}
