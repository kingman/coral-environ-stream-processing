# coral-environ-stream-processing
Sample code to run stream processing in Apache Beam with dynamica schema loading from Google Cloud IoT Core.
The schema use in this example works with the sensor metrics generated by the [Foglamp Coral Environmental Sensor Board plugin](https://github.com/kingman/foglamp-south-coral-enviro).

Execute following steps to run this sample.

## Get the tutorial source code in Cloud Shell

1.  In the GCP Console, [open Cloud Shell](http://console.cloud.google.com/?cloudshell=true).
1.  Clone the source code repository:

        cd "$HOME"
        git clone https://github.com/kingman/coral-environ-stream-processing.git
1. Generate [application default credentials](https://cloud.google.com/sdk/gcloud/reference/auth/application-default):

        gcloud auth application-default login --quiet

    The output is similar to the following:

        Go to the following link in your browser:
        https://accounts.google.com/o/oauth2/auth?code_challenge=...
        Enter verification code:

1. In a browser window, open the URL that is displayed in the output from generating the application default credentials (the preceding step).

1. Select **Allow** to continue.

1. Copy the code on the screen and enter it into Cloud Shell.
      
    The output is similar to the following:

        /tmp/tmp.xxxxxxxxxx/application_default_credentials.json

    **Note** the path to the `application_default_credentials.json` file. You use this path to set an environment variable in the next section.

## Setting environment variables

Before you can provision the necessary infrastructure for this tutorial, you need to initialize and export the following environment variables:

1. Create an environment variable that stores your Google Cloud project ID

        export GOOGLE_CLOUD_PROJECT=${DEVSHELL_PROJECT_ID}

1. Create an environment variable that stores your Google Cloud region

        export GOOGLE_CLOUD_REGION=[REGION]

    Replace the following:
    * **[REGION]** Your preferred region of operation, can be one of the following `us-central1`, `europe-west1`, or `asia-east1`

1. Create an environment variable that stores your Google Cloud zone

        export GOOGLE_CLOUD_ZONE=[ZONE]

    Replace the following:
    * **[ZONE]** Your preferred zone of operation, use following table to determine the possible values to set:

        |Region|Zone|
        |-|-|
        |us-central1|us-central1-a, us-central1-b, us-central1-c, us-central1-f|
        |europe-west1|europe-west1-b, europe-west1-c, europe-west1-d|
        |asia-east1|asia-east1-a, asia-east1-b, asia-east1-c|

1. Create an environment variable that stores path to the default Google Cloud application default credentials, which is the value you noted in the preceding section:

        export GOOGLE_APPLICATION_CREDENTIALS=[PATH]

    Replace the following:
    * **[PATH]** path to the application_default_credentials.json file

1. Setting the rest of the environment variables by running the `set-env.sh` script:

        cd "$HOME"/coral-environ-stream-processing/scripts
        . set-env.sh

## Provisioning the environment

You need to run the `generate-tf-backend.sh` shell script that generates the [Terraform backend configuration](https://www.terraform.io/docs/backends/index.html), the necessary Google Cloud service accounts, and the Cloud Storage bucket to store information about the [Terraform remote state](https://www.terraform.io/docs/state/remote.html).

* In Cloud Shell, provision your build environment:

        cd "$HOME"/coral-environ-stream-processing
        scripts/generate-tf-backend.sh

    The script is idempotent and safe to run multiple times.

    After you run the script successfully for the first time, the output is similar to the following:

        Generating the descriptor to hold backend data in terraform/backend.tf
        terraform {
            backend "gcs" {
                bucket  = "tf-state-bucket-foglamp-lab-provision"
                prefix  = "terraform/state"
            }
        }

## Create Dataflow template
To deploy the Dataflow stream processing job through Terraform, the Dataflow job needs to be staged as [Dataflow templates](https://cloud.google.com/dataflow/docs/guides/templates/overview)

* In Cloud Shell, create and stage the Dataflow template:

        cd "$HOME"/coral-environ-stream-processing
        scripts/build-dataflow-template.sh

## Create backend resources
The Terraform template file `terraform/main.tf` defines the resources that are created for this tutorial. By running Terraform with that descriptor, you create the following Google Cloud resources:

* A Pub/Sub [topic](https://cloud.google.com/pubsub/docs/overview#data_model) where [Cloud IoT Core](https://cloud.google.com/iot/docs/how-tos/mqtt-bridge#publishing_telemetry_events) bridges all the IoT event messages
* A Cloud IoT [device registry](https://cloud.google.com/iot/docs/concepts/devices#device_registries)
* A BigQuery [dataset](https://cloud.google.com/bigquery/docs/datasets-intro) where the processed data is stored
* A Dataflow [Streaming pipeline](https://cloud.google.com/dataflow/docs/concepts/streaming-pipelines) that processes the event messages delievered to the Pub/Sub topic

In Cloud Shell, do the following:

1. Initiate terraform

        cd "$HOME"/coral-environ-stream-processing/terraform
        terraform init

1. Run the [terraform apply](https://www.terraform.io/docs/commands/apply.html) command to create the resources in your Cloud project:

        terraform apply

1. To continue with running the command, enter **yes**.

## Add the public key to IoT Core device

In order for your device to connect with Cloud IoT Core [device](https://cloud.google.com/iot/docs/concepts/devices) your device's public key needs to be configured to the corresponding Cloud IoT Core device.

1. Manually add the public key of your device through [Cloud Console](http://console.cloud.google.com/iot)

## Verify the result data

Once your device is set up and Foglamp Coral Environmental Sensor Board plugin start to streaming data you can verify the aggregated result in [BigQuery](http://console.cloud.google.com/bigquery)

---

## Manual steps [optional path]

To manually deploy the Dataflow streaming pipeline run:

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
