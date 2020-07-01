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
export TF_STATE_PROJECT=${GOOGLE_CLOUD_PROJECT}
export TF_STATE_BUCKET=tf-state-bucket-${TF_STATE_PROJECT}
export IOT_REGISTRY_ID=device-registry
export IOT_DEVICE_ID=enviro-plugin
export DATAFLOW_TEMPLATE_BUCKET=stream-processing-${GOOGLE_CLOUD_PROJECT}
export BIGQUERY_DATASET_ID=foglamp
export BIGQUERY_METRICS_TABLE_ID=metrics_summary
export BIGQUERY_INFERENCE_TABLE_ID=inference_result