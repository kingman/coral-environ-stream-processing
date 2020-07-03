#!/usr/bin/env sh

# Copyright 2020 Google LLC
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

if [ -z "${GOOGLE_CLOUD_PROJECT}" ]; then
    echo 'The GOOGLE_CLOUD_PROJECT environment variable that points to the Google Cloud project is not defined. Terminating...'
    exit 1
fi

if [ -z "${DATAFLOW_TEMPLATE_BUCKET}" ]; then
    echo 'The DATAFLOW_TEMPLATE_BUCKET environment variable that points to the Google Cloud Storage bucket to store and stage dataflow template is not defined. Terminating...'
    exit 1
fi

docker run -it --rm --name my-maven-project -v "$(pwd)":/usr/src/mymaven -w /usr/src/mymaven maven:3.6.3-jdk-11 mvn compile exec:java -Dexec.mainClass=com.google.cloud.solutions.IoTStreamAnalytics -Dexec.args="\
--runner=DataflowRunner \
--project=${GOOGLE_CLOUD_PROJECT} \
--stagingLocation=gs://${DATAFLOW_TEMPLATE_BUCKET}/staging \
--templateLocation=gs://${DATAFLOW_TEMPLATE_BUCKET}/templates/iot-stream-processing" -Pdataflow-runner