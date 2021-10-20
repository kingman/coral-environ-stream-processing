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

# Check if the necessary dependencies are available
if ! command -v gsutil >/dev/null 2>&1; then
    echo "gsutil command is not available, but it's needed. Terminating..."
    return
fi

if [ -z "${TF_SERVICE_ACCOUNT_NAME}" ]; then
    echo 'The TF_SERVICE_ACCOUNT_NAME environment variable that points to the Google Cloud service account that Terraform will use is not defined. Terminating...'
    return
fi

if [ -z "${TF_STATE_PROJECT}" ]; then
    echo 'The TF_STATE_PROJECT environment variable that points to the Google Cloud project to store the Terraform state is not defined. Terminating...'
    return
fi

if [ -z "${TF_STATE_BUCKET}" ]; then
    echo 'The TF_STATE_BUCKET environment variable that points to the Google Cloud Storage bucket to store the Terraform state is not defined. Terminating...'
    return
fi

if [ -z "${GOOGLE_CLOUD_PROJECT}" ]; then
    echo 'The GOOGLE_CLOUD_PROJECT environment variable that points to the default Google Cloud project that Terraform will provision the resources in is not defined. Terminating...'
    return
fi

if [ -z "${GOOGLE_CLOUD_REGION}" ]; then
    echo 'The GOOGLE_CLOUD_REGION environment variable that points to the default Google Cloud region that Terraform will provision the resources in is not defined. Terminating...'
    return
fi

if [ -z "${GOOGLE_CLOUD_ZONE}" ]; then
    echo 'The GOOGLE_CLOUD_ZONE environment variable that points to the default Google Cloud zone that Terraform will provision the resources in is not defined. Terminating...'
    return
fi

if [ -z "${IOT_REGISTRY_ID}" ]; then
    echo 'The IOT_REGISTRY_ID environment variable that states the IoT Core registry id that Terraform will use is not defined. Terminating...'
    return
fi

if [ -z "${IOT_DEVICE_ID}" ]; then
    echo 'The IOT_DEVICE_ID environment variable that states the IoT Core device id that Terraform will use is not defined. Terminating...'
    return
fi

if [ -z "${GOOGLE_APPLICATION_CREDENTIALS}" ]; then
    echo 'The GOOGLE_APPLICATION_CREDENTIALS environment variable that points to the default Google Cloud application credentials that Terraform will use is not defined. Terminating...'
    return
fi

if [ -z "${DATAFLOW_TEMPLATE_BUCKET}" ]; then
    echo 'The DATAFLOW_TEMPLATE_BUCKET environment variable that points to the Google Cloud Storage bucket to store and stage dataflow template is not defined. Terminating...'
    return
fi

if [ -z "${BIGQUERY_DATASET_ID}" ]; then
    echo 'The BIGQUERY_DATASET_ID environment variable that points to the Google Cloud BigQuery dataset is not defined. Terminating...'
    return
fi

if [ -z "${BIGQUERY_METRICS_TABLE_ID}" ]; then
    echo 'The BIGQUERY_METRICS_TABLE_ID environment variable that points to the Google Cloud BigQuery table is not defined. Terminating...'
    return
fi

if [ -z "${BIGQUERY_UNKNOWN_MESSAGE_TABLE_ID}" ]; then
    echo 'The BIGQUERY_UNKNOWN_MESSAGE_TABLE_ID environment variable that points to the Google Cloud BigQuery table is not defined. Terminating...'
    return
fi

echo "Setting the default Google Cloud project to ${TF_STATE_PROJECT}"
gcloud config set project "${TF_STATE_PROJECT}"

echo "Creating the service account for Terraform"
if gcloud iam service-accounts describe "${TF_SERVICE_ACCOUNT_NAME}"@"${TF_STATE_PROJECT}".iam.gserviceaccount.com >/dev/null 2>&1; then
    echo "The ${TF_SERVICE_ACCOUNT_NAME} service account already exists."
else
    gcloud iam service-accounts create "${TF_SERVICE_ACCOUNT_NAME}" \
        --display-name "Terraform admin account"
fi

echo "Granting the service account permission to view the Admin Project"
gcloud projects add-iam-policy-binding "${TF_STATE_PROJECT}" \
    --member serviceAccount:"${TF_SERVICE_ACCOUNT_NAME}"@"${TF_STATE_PROJECT}".iam.gserviceaccount.com \
    --role roles/viewer

echo "Granting the service account permission to manage Cloud Storage"
gcloud projects add-iam-policy-binding "${TF_STATE_PROJECT}" \
    --member serviceAccount:"${TF_SERVICE_ACCOUNT_NAME}"@"${TF_STATE_PROJECT}".iam.gserviceaccount.com \
    --role roles/storage.admin

echo "Enable the Cloud Resource Manager API with"
gcloud services enable cloudresourcemanager.googleapis.com

echo "Creating a new Google Cloud Storage bucket to store the Terraform state in ${TF_STATE_PROJECT} project, bucket: ${TF_STATE_BUCKET}"
if gsutil ls -b gs://"${TF_STATE_BUCKET}" >/dev/null 2>&1; then
    echo "The ${TF_STATE_BUCKET} Google Cloud Storage bucket already exists."
else
    gsutil mb -p "${TF_STATE_PROJECT}" gs://"${TF_STATE_BUCKET}"
    gsutil versioning set on gs://"${TF_STATE_BUCKET}"
fi

echo "Creating a new Google Cloud Storage bucket to store the dataflow template in ${TF_STATE_PROJECT} project, bucket: ${DATAFLOW_TEMPLATE_BUCKET}"
if gsutil ls -b gs://"${DATAFLOW_TEMPLATE_BUCKET}" >/dev/null 2>&1; then
    echo "The ${DATAFLOW_TEMPLATE_BUCKET} Google Cloud Storage bucket already exists."
else
    gsutil mb -p "${GOOGLE_CLOUD_PROJECT}" gs://"${DATAFLOW_TEMPLATE_BUCKET}"
fi

TERRAFORM_BACKEND_DESCRIPTOR_PATH=terraform/backend.tf
echo "Generating the descriptor to hold backend data in ${TERRAFORM_BACKEND_DESCRIPTOR_PATH}"
if [ -f "${TERRAFORM_BACKEND_DESCRIPTOR_PATH}" ]; then
    echo "The ${TERRAFORM_BACKEND_DESCRIPTOR_PATH} file already exists."
else
    tee "${TERRAFORM_BACKEND_DESCRIPTOR_PATH}" <<EOF
terraform {
    backend "gcs" {
        bucket  = "${TF_STATE_BUCKET}"
        prefix  = "terraform/state"
    }
}
EOF
fi

TERRAFORM_VARIABLE_FILE_PATH=terraform/terraform.tfvars
echo "Generate the terraform variables in ${TERRAFORM_VARIABLE_FILE_PATH}"
if [ -f "${TERRAFORM_VARIABLE_FILE_PATH}" ]; then
    echo "The ${TERRAFORM_VARIABLE_FILE_PATH} file already exists."
else
    if [[ ${GOOGLE_CLOUD_REGION} == *"asia-"* ]]; then
        GOOGLE_BIGQUERY_REGION="asia-east1"
    elif [[ ${GOOGLE_CLOUD_REGION} == *"europe-"* ]]; then
        GOOGLE_BIGQUERY_REGION="europe-west3"
    else
        GOOGLE_BIGQUERY_REGION="us-west2"
    fi
    CONFIGS_DIR_RELATIVE_PATH="../data-configs/"
    UNKNOWN_MESSAGE_TYPE_ID="unknown-message"
    EDGEX_MESSAGE_TYPE_ID="edgex"
    FOGLAMP_SINUSOID_MESSAGE_TYPE_ID="foglamp-sinusoid"
    tee "${TERRAFORM_VARIABLE_FILE_PATH}" <<EOF
google_project_id="${GOOGLE_CLOUD_PROJECT}"
google_default_region="${GOOGLE_CLOUD_REGION}"
google_default_zone="${GOOGLE_CLOUD_ZONE}"
google_iot_registry_id="${IOT_REGISTRY_ID}"
google_iot_device_id="${IOT_DEVICE_ID}"
google_bigquery_default_region="${GOOGLE_BIGQUERY_REGION}"
google_bigquery_dataset_id="${BIGQUERY_DATASET_ID}"
google_dataflow_default_bucket="${DATAFLOW_TEMPLATE_BUCKET}"
input_data_schemas_path="${CONFIGS_DIR_RELATIVE_PATH}input-data-schema.json"
data_type_configuration = [
  {
    id = "${UNKNOWN_MESSAGE_TYPE_ID}"
    schema_key = "table-schema-${UNKNOWN_MESSAGE_TYPE_ID}"
    dataset_key = "destination-dataset-${UNKNOWN_MESSAGE_TYPE_ID}"
    table_key = "destination-table-${UNKNOWN_MESSAGE_TYPE_ID}"
    schema_map_key = ""
    schema_path = "${CONFIGS_DIR_RELATIVE_PATH}${UNKNOWN_MESSAGE_TYPE_ID}-table-schema.json"
    destination_table = "${BIGQUERY_UNKNOWN_MESSAGE_TABLE_ID}"
    destination_dataset = "${BIGQUERY_DATASET_ID}"
    schema_map_path = ""
  },
  {
    id = "${EDGEX_MESSAGE_TYPE_ID}"
    schema_key = "table-schema-${EDGEX_MESSAGE_TYPE_ID}"
    dataset_key = "destination-dataset-${EDGEX_MESSAGE_TYPE_ID}"
    table_key = "destination-table-${EDGEX_MESSAGE_TYPE_ID}"
    schema_map_key = "schema-map-${EDGEX_MESSAGE_TYPE_ID}"
    schema_path = "${CONFIGS_DIR_RELATIVE_PATH}${EDGEX_MESSAGE_TYPE_ID}-table-schema.json"
    destination_table = "${BIGQUERY_METRICS_TABLE_ID}"
    destination_dataset = "${BIGQUERY_DATASET_ID}"
    schema_map_path = "${CONFIGS_DIR_RELATIVE_PATH}${EDGEX_MESSAGE_TYPE_ID}-schema-mapping.json"
  },
  {
    id = "${FOGLAMP_SINUSOID_MESSAGE_TYPE_ID}"
    schema_key = "table-schema-${FOGLAMP_SINUSOID_MESSAGE_TYPE_ID}"
    dataset_key = "destination-dataset-${FOGLAMP_SINUSOID_MESSAGE_TYPE_ID}"
    table_key = "destination-table-${FOGLAMP_SINUSOID_MESSAGE_TYPE_ID}"
    schema_map_key = "schema-map-${FOGLAMP_SINUSOID_MESSAGE_TYPE_ID}"
    schema_path = "${CONFIGS_DIR_RELATIVE_PATH}${FOGLAMP_SINUSOID_MESSAGE_TYPE_ID}-table-schema.json"
    destination_table = "${BIGQUERY_FOGLAMP_SINUSOID_TABLE_ID}"
    destination_dataset = "${BIGQUERY_DATASET_ID}"
    schema_map_path = "${CONFIGS_DIR_RELATIVE_PATH}${FOGLAMP_SINUSOID_MESSAGE_TYPE_ID}-schema-mapping.json"
  }
]
EOF
fi
