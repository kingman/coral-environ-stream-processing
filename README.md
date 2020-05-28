# coral-environ-stream-processing
Sample code to run stream processing in Apache Beam with dynamica schema loading from Google Cloud IoT Core

To deploy the processing pipeline to Google Cloud DataFlow run:

        mvn compile exec:java \
        -Dexec.mainClass=com.google.cloud.solutions.IoTStreamAnalytics \
        -Dexec.cleanupDaemonThreads=false \
        -Dexec.args=" \
             --project=[PROJECT_ID] \
             --tempLocation=gs://[STAGE_BUCKET] \
             --streaming=true \
             --numWorkers=1 \
             --workerMachineType=n1-standard-1 \
             --inputTopic=[PUBSUB_TOPIC] \
             --windowSize=40 \
             --windowFrequency=15 \
             --outputTable=sliding_time_summary \
             --jobName=foglamp-stream \
             --runner=DataflowRunner" \
        -Pdataflow-runner

The Beam pipeline reads `metrics-schema` and `table-schema` metadata values from Cloud IoT Core, and uses `metrics-schema` to validate the input data and `table-schema` value to create BigQuery table schema.

To validate the metrics data sent by the foglamp south plugin for Coral Environmental Sensor Board set the metadata `metrics-schema` value to:

     {
       "$schema": "http://json-schema.org/draft-07/schema#",
       "$id": "http://coral.ai/schemas/environmental.json",
       "type": "object",
       "properties": {
         "enviro": {
           "type": "array",
           "items": {
             "$ref": "#/definitions/measurement"
           }
         }
       },
       "required": [
         "enviro"
       ],
       "definitions": {
         "measurement": {
           "type": "object",
           "properties": {
             "ts": {
               "type": "string"
             },
             "temperature": {
               "type": "number"
             },
             "pressure": {
               "type": "number"
             },
             "humidity": {
               "type": "number"
             },
             "ambient_light": {
               "type": "number"
             },
             "grove_analog": {
               "type": "number"
             }
           },
           "propertyNames": {
             "pattern": "^(ts|temperature|pressure|humidity|ambient_light|grove_analog)$"
           },
           "required": [
             "ts"
           ]
         }
       }
     }

And to define the BigQuery table schema for store the metrics data, set the metadata `table-schema` value to:

     [
          {"mode":"REQUIRED","name":"DeviceNumId","type":"STRING"},
          {"mode":"REQUIRED","name":"DeviceId","type":"STRING"},
          {"mode":"REQUIRED","name":"RegistryId","type":"STRING"},
          {"mode":"REQUIRED","name":"MetricType","type":"STRING"},
          {"mode":"REQUIRED","name":"PeriodStart","type":"TIMESTAMP"},
          {"mode":"REQUIRED","name":"PeriodEnd","type":"TIMESTAMP"},
          {"mode":"REQUIRED","name":"MaxValue","type":"FLOAT"},
          {"mode":"REQUIRED","name":"MinValue","type":"FLOAT"},
          {"mode":"REQUIRED","name":"Average","type":"FLOAT"}
     ]


