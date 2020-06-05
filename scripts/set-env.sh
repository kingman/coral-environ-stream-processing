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

export TF_SERVICE_ACCOUNT_NAME=tf-service-account
export TF_STATE_PROJECT=${DEVSHELL_PROJECT_ID}
export TF_STATE_BUCKET=tf-state-bucket-${TF_STATE_PROJECT}
export GOOGLE_CLOUD_PROJECT=${DEVSHELL_PROJECT_ID}
export GOOGLE_CLOUD_REGION=europe-west1
export IOT_REGISTRY_ID=device-registry
export IOT_DEVICE_ID=enviro-plugin
export DATAFLOW_TEMPLATE_BUCKET=stream-processing-${GOOGLE_CLOUD_PROJECT}
export TF_VAR_google_project_id=${GOOGLE_CLOUD_PROJECT}
export TF_VAR_google_default_region=${GOOGLE_CLOUD_REGION}
export TF_VAR_google_default_zone=europe-west1-b
export TF_VAR_google_iot_registry_id=${IOT_REGISTRY_ID}
export TF_VAR_google_bigquery_default_zone=europe-west2
export TF_VAR_google_dataflow_default_bucket=${DATAFLOW_TEMPLATE_BUCKET}
export TF_VAR_stream_processing_window_size=40
export TF_VAR_stream_processing_window_frequency=15

