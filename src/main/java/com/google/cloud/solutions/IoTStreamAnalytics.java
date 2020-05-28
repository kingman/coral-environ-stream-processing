package com.google.cloud.solutions;

import java.util.List;
import java.util.Map;

import com.google.api.services.bigquery.model.TableReference;
import com.google.api.services.bigquery.model.TableRow;
import com.google.api.services.bigquery.model.TableSchema;
import com.google.cloud.solutions.common.DeviceInfo;
import com.google.cloud.solutions.common.Measurement;
import com.google.cloud.solutions.common.MeasurementSummary;
import com.google.cloud.solutions.transformation.MeasurementToDeviceInfoMap;
import com.google.cloud.solutions.transformation.MeasurementToMeasurementSummary;
import com.google.cloud.solutions.transformation.PubsubMessageToMeasurement;
import com.google.cloud.solutions.utils.JsonSchemaValidator;
import com.google.cloud.solutions.utils.MeasurementKeyGenerator;
import com.google.cloud.solutions.utils.MeasurementTimestampGenerator;
import com.google.cloud.solutions.utils.TableSchemaLoader;
import com.google.common.collect.ImmutableList;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO.Write.CreateDisposition;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO.Write.WriteDisposition;
import org.apache.beam.sdk.io.gcp.bigquery.DynamicDestinations;
import org.apache.beam.sdk.io.gcp.bigquery.TableDestination;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.options.StreamingOptions;
import org.apache.beam.sdk.options.Validation.Required;
import org.apache.beam.sdk.transforms.Combine;
import org.apache.beam.sdk.transforms.Filter;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.WithKeys;
import org.apache.beam.sdk.transforms.WithTimestamps;
import org.apache.beam.sdk.transforms.windowing.SlidingWindows;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionView;
import org.apache.beam.sdk.values.ValueInSingleWindow;
import org.joda.time.Duration;

public class IoTStreamAnalytics {

        public interface IoTStreamAnalyticsOptions extends PipelineOptions, StreamingOptions {
                @Description("The Cloud Pub/Sub topic to read from.")
                @Required
                String getInputTopic();

                void setInputTopic(String value);

                @Description("The window size in number of seconds of which the average value is calculated on.")
                @Default.Integer(30)
                Integer getWindowSize();

                void setWindowSize(Integer value);

                @Description("The frequence of new window begins in number of seconds.")
                @Default.Integer(10)
                Integer getWindowFrequency();

                void setWindowFrequency(Integer value);

                @Description("The out put table. Fully-qualified BigQuery table name: [project_id]:[dataset_id].[table_id]")
                @Required
                String getOutputTable();

                void setOutputTable(String value);
        }

        public static void main(String[] args) {
                IoTStreamAnalyticsOptions options = PipelineOptionsFactory.fromArgs(args).withValidation()
                                .as(IoTStreamAnalyticsOptions.class);
                options.setStreaming(true);

                Pipeline pipeline = Pipeline.create(options);

                PCollection<Measurement> windowedMetrics = pipeline
                                .apply("Read IoT Core events",
                                                PubsubIO.readMessagesWithAttributes()
                                                                .fromTopic(options.getInputTopic()))
                                .apply("Validate metrics against schema", Filter.by(JsonSchemaValidator::validate))
                                .apply("Flatten each measurement", ParDo.of(new PubsubMessageToMeasurement()))
                                .apply("Set event timestamp",
                                                WithTimestamps.<Measurement>of(new MeasurementTimestampGenerator())
                                                                .withAllowedTimestampSkew(Duration.standardMinutes(10)))
                                .apply("Apply sliding windowing", Window.<Measurement>into(SlidingWindows
                                                .of(Duration.standardSeconds(options.getWindowSize()))
                                                .every(Duration.standardSeconds(options.getWindowFrequency()))));

                windowedMetrics.apply("Create key for device and metric type combination",
                                WithKeys.of(new MeasurementKeyGenerator()))
                                .apply("Create window summary",
                                                Combine.<String, Measurement, MeasurementSummary>perKey(
                                                                new MeasurementToMeasurementSummary()))
                                .apply("Write result to BigQuery dynamically", BigQueryIO
                                                .<KV<String, MeasurementSummary>>write()
                                                .to(new DynamicDestinations<KV<String, MeasurementSummary>, DeviceInfo>() {

                                                        @Override
                                                        public DeviceInfo getDestination(
                                                                        ValueInSingleWindow<KV<String, MeasurementSummary>> element) {
                                                                return element.getValue().getValue().getDeviceInfo();
                                                        }

                                                        @Override
                                                        public TableDestination getTable(DeviceInfo destination) {
                                                                return new TableDestination(new TableReference()
                                                                                .setProjectId(destination
                                                                                                .getProjectId())
                                                                                .setDatasetId("foglamp") //TODO make dynamic
                                                                                .setTableId(options.getOutputTable()),
                                                                                "Table " + options.getOutputTable());
                                                        }

                                                        @Override
                                                        public TableSchema getSchema(DeviceInfo destination) {
                                                                return TableSchemaLoader.getSchema(destination);
                                                        }

                                                }).withFormatFunction((element) -> {
                                                        MeasurementSummary summary = element.getValue();
                                                        String metricType = element.getKey().split(":")[1];

                                                        return new TableRow().set("DeviceNumId", summary.getDeviceInfo().getDeviceNumId())
                                                                        .set("DeviceId", summary.getDeviceInfo().getDeviceId())
                                                                        .set("RegistryId", summary.getDeviceInfo().getDeviceRegistryId()) 
                                                                        .set("MetricType", metricType)
                                                                        .set("PeriodStart", summary.getStart())
                                                                        .set("PeriodEnd", summary.getEnd())
                                                                        .set("MaxValue", summary.getMax())
                                                                        .set("MinValue", summary.getMin())
                                                                        .set("Average", summary.getAverage());

                                                }).withCreateDisposition(CreateDisposition.CREATE_IF_NEEDED)
                                                .withWriteDisposition(WriteDisposition.WRITE_APPEND));

                pipeline.run();

        }
}