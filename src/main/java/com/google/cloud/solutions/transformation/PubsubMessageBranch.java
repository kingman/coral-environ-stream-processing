package com.google.cloud.solutions.transformation;

import com.google.cloud.solutions.utils.JsonSchemaValidator;

import org.apache.beam.sdk.io.gcp.pubsub.PubsubMessage;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.values.TupleTag;

public class PubsubMessageBranch extends DoFn<PubsubMessage, PubsubMessage> {

    private static final long serialVersionUID = 1L;
    private final TupleTag<PubsubMessage> metricTag;
    private final TupleTag<PubsubMessage> inferenceTag;
    private final TupleTag<PubsubMessage> unknownMessageTag;
    private final JsonSchemaValidator metricValidator;
    private final JsonSchemaValidator inferenceValidator;


    public PubsubMessageBranch(TupleTag<PubsubMessage> metricTag, TupleTag<PubsubMessage> inferenceTag,
            TupleTag<PubsubMessage> unknownMessageTag) {
        this.metricTag = metricTag;
        this.inferenceTag = inferenceTag;
        this.unknownMessageTag = unknownMessageTag;
        this.metricValidator = new JsonSchemaValidator("metrics-schema");
        this.inferenceValidator = new JsonSchemaValidator("inference-schema");
    }

    @ProcessElement
    public void processElement(ProcessContext context) {
        if(metricValidator.apply(context.element())) {
            context.output(metricTag, context.element());
        } else if (inferenceValidator.apply(context.element())) {
            context.output(inferenceTag, context.element());
        } else {
            context.output(unknownMessageTag, context.element());
        }
    }

}