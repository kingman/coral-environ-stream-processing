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

if [ -z "${IOT_REGISTRY_ID}" ]; then
    echo 'The IOT_REGISTRY_ID environment variable that points to the Cloud IoT Core registry is not defined. Terminating...'
    exit 1
fi

if [ -z "${GOOGLE_CLOUD_REGION}" ]; then
    echo 'The GOOGLE_CLOUD_REGION environment variable that points to the Cloud IoT Core region is not defined. Terminating...'
    exit 1
fi

if [ -z "${IOT_DEVICE_ID}" ]; then
    echo 'The IOT_DEVICE_ID environment variable that points to the Cloud IoT Core device is not defined. Terminating...'
    exit 1
fi

if [ -z "${BIGQUERY_DATASET_ID}" ]; then
    echo 'The BIGQUERY_DATASET_ID environment variable that points to the Google Cloud BigQuery dataset is not defined. Terminating...'
    exit 1
fi

if [ -z "${BIGQUERY_TABLE_ID}" ]; then
    echo 'The BIGQUERY_TABLE_ID environment variable that points to the Google Cloud BigQuery table is not defined. Terminating...'
    exit 1
fi

echo "Creating Cloud IoT Core device with id: ${IOT_DEVICE_ID} with metadata for data schemas."
gcloud iot devices create ${IOT_DEVICE_ID} \
  --project=${GOOGLE_CLOUD_PROJECT} \
  --region=${GOOGLE_CLOUD_REGION} \
  --registry=${IOT_REGISTRY_ID} \
  --metadata-from-file=metrics-schema=metrics-schema.json,table-schema=table-schema.json \
  --metadata=destination-dataset=${BIGQUERY_DATASET_ID},destination-table=${BIGQUERY_TABLE_ID}