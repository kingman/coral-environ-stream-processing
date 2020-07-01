package com.google.cloud.solutions;

import com.google.cloud.solutions.common.HumanDetectionResult;
import com.google.cloud.solutions.common.Measurement;
import com.google.cloud.solutions.common.MeasurementSummary;
import com.google.cloud.solutions.transformation.HumanDetectionResultTableRowMapper;
import com.google.cloud.solutions.transformation.HumanDetectionResultToTableDestination;
import com.google.cloud.solutions.transformation.MeasurementSummaryTableRowMapper;
import com.google.cloud.solutions.transformation.MeasurementSummaryToTableDestination;
import com.google.cloud.solutions.transformation.MeasurementToMeasurementSummary;
import com.google.cloud.solutions.transformation.PubsubMessageBranch;
import com.google.cloud.solutions.transformation.PubsubMessageToHumanDetectionResult;
import com.google.cloud.solutions.transformation.PubsubMessageToMeasurement;
import com.google.cloud.solutions.utils.MeasurementKeyGenerator;
import com.google.cloud.solutions.utils.MeasurementTimestampGenerator;
import com.google.cloud.solutions.utils.OldMeasurementFilter;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO.Write.CreateDisposition;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO.Write.WriteDisposition;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubMessage;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.options.StreamingOptions;
import org.apache.beam.sdk.options.Validation.Required;
import org.apache.beam.sdk.options.ValueProvider;
import org.apache.beam.sdk.transforms.Combine;
import org.apache.beam.sdk.transforms.Filter;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.WithKeys;
import org.apache.beam.sdk.transforms.WithTimestamps;
import org.apache.beam.sdk.transforms.windowing.SlidingWindows;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionTuple;
import org.apache.beam.sdk.values.TupleTag;
import org.apache.beam.sdk.values.TupleTagList;
import org.joda.time.Duration;

public class IoTStreamAnalytics {

        public interface IoTStreamAnalyticsOptions extends PipelineOptions, StreamingOptions {
                @Description("The Cloud Pub/Sub topic to read from.")
                @Required
                ValueProvider<String> getInputTopic();

                void setInputTopic(ValueProvider<String> value);

                @Description("The window size in number of seconds of which the average value is calculated on.")
                @Default.Integer(30)
                Integer getWindowSize();

                void setWindowSize(Integer value);

                @Description("The frequence of new window begins in number of seconds.")
                @Default.Integer(10)
                Integer getWindowFrequency();

                void setWindowFrequency(Integer value);

                @Description("The maxium time in minutes a message are allow to be late, based on the message's event timestamp")
                @Default.Integer(10)
                Integer getAllowedTimestampSkew();

                void setAllowedTimestampSkew(Integer value);
        }

        public static void main(String[] args) {
                IoTStreamAnalyticsOptions options = PipelineOptionsFactory.fromArgs(args).withValidation()
                                .as(IoTStreamAnalyticsOptions.class);
                options.setStreaming(true);

                Pipeline pipeline = Pipeline.create(options);
                final TupleTag<PubsubMessage> metricTag = new TupleTag<PubsubMessage>() {};
                final TupleTag<PubsubMessage> inferenceTag = new TupleTag<PubsubMessage>() {};
                final TupleTag<PubsubMessage> unknownMessageTag = new TupleTag<PubsubMessage>() {};

                PCollectionTuple metricsPipeline = pipeline
                .apply("Read IoT Core events", PubsubIO.readMessagesWithAttributes().fromTopic(options.getInputTopic()))
                .apply("Branch the input stream based on message schema", ParDo.of(new PubsubMessageBranch(metricTag, inferenceTag, unknownMessageTag))
                .withOutputTags(metricTag, TupleTagList.of(inferenceTag).and(unknownMessageTag))
                );

                metricsProcessing(metricsPipeline.get(metricTag), options);
                inferenceDataProcessing(metricsPipeline.get(inferenceTag), options);

                pipeline.run();
        }

        private static void metricsProcessing(PCollection<PubsubMessage> metricsPipeline, IoTStreamAnalyticsOptions options) {
                final OldMeasurementFilter oldMeasurementFilter = new OldMeasurementFilter(options.getAllowedTimestampSkew());

                metricsPipeline.apply("Flatten each measurement", ParDo.of(new PubsubMessageToMeasurement()))
                .apply("Filter out messages that are too late", Filter.by(oldMeasurementFilter::apply))
                .apply("Set event timestamp", WithTimestamps.<Measurement>of(new MeasurementTimestampGenerator())
                        .withAllowedTimestampSkew(Duration.standardMinutes(options.getAllowedTimestampSkew())))
                .apply("Apply sliding windowing",
                        Window.<Measurement>into(SlidingWindows
                        .of(Duration.standardSeconds(options.getWindowSize()))
                        .every(Duration.standardSeconds(options.getWindowFrequency()))))
                .apply("Create key for device and metric type combination", WithKeys.of(new MeasurementKeyGenerator()))
                .apply("Create window summary", Combine.<String, Measurement, MeasurementSummary>perKey(
                                                                new MeasurementToMeasurementSummary()))
                .apply("Write result to BigQuery dynamically", BigQueryIO.<KV<String, MeasurementSummary>>write()
                        .to(new MeasurementSummaryToTableDestination())
                        .withFormatFunction(new MeasurementSummaryTableRowMapper())
                        .withCreateDisposition(CreateDisposition.CREATE_IF_NEEDED)
                        .withWriteDisposition(WriteDisposition.WRITE_APPEND));
        }

        private static void inferenceDataProcessing(PCollection<PubsubMessage> inferenceResultPipeline, IoTStreamAnalyticsOptions options) {
                inferenceResultPipeline.apply("Extract inference results", ParDo.of(new PubsubMessageToHumanDetectionResult()))
                .apply("Write detection result to BigQuery", BigQueryIO.<HumanDetectionResult>write()
                .to(new HumanDetectionResultToTableDestination())
                .withFormatFunction(new HumanDetectionResultTableRowMapper())
                .withCreateDisposition(CreateDisposition.CREATE_IF_NEEDED)
                .withWriteDisposition(WriteDisposition.WRITE_APPEND));
        }
}