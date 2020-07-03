package com.google.cloud.solutions.transformation;

import com.google.cloud.solutions.common.UnParsedMessage;
import com.google.cloud.solutions.utils.PubsubMessageUtil;

import org.apache.beam.sdk.io.gcp.pubsub.PubsubMessage;
import org.apache.beam.sdk.transforms.DoFn;

public class PubsubMessageToUnParsedMessage extends DoFn<PubsubMessage, UnParsedMessage> {

    private static final long serialVersionUID = 1L;

    @ProcessElement
    public void processElement(@Element PubsubMessage message, OutputReceiver<UnParsedMessage> receiver) {
        UnParsedMessage unParsedMessage = new UnParsedMessage();
        unParsedMessage.setDeviceInfo(PubsubMessageUtil.extractDeviceInfo(message));
        unParsedMessage.setMessage(new String(message.getPayload()));
        
        receiver.output(unParsedMessage);
    }
}