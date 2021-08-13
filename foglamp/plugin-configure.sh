#!/usr/bin/env sh

# Copyright 2021 Google LLC
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

FOGLAMP_BASE_URL='http://localhost:8081/foglamp'

GCP_PLUGIN_INSTALLED=$(curl -s "${FOGLAMP_BASE_URL}/plugins/installed?type=north" | jq -cr '.plugins[] | select(.name == "GCP")')
if [[ ! ${GCP_PLUGIN_INSTALLED} ]]; then
    curl -sX POST "${FOGLAMP_BASE_URL}/plugins" \
    -H "Content-Type: application/json" \
    --data-binary @- <<DATA
{
  "format": "repository",
  "name": "foglamp-north-gcp",
  "version": ""
}
DATA
fi

if [ -z "${GOOGLE_CLOUD_PROJECT}" ]; then
    echo 'The GOOGLE_CLOUD_PROJECT environment variable is not defined. The variable points to the Google Cloud project the FogLAMP connects to. Terminating...'
    exit 1
fi

if [ -z "${GOOGLE_CLOUD_REGION}" ]; then
    echo 'The GOOGLE_CLOUD_REGION environment variable is not defined. The variable points to the region of Google Cloud IoT registry. Terminating...'
    exit 1
fi

if [ -z "${IOT_CORE_REGISTRY_ID}" ]; then
    IOT_CORE_REGISTRY_ID=device-registry
fi

if [ -z "${IOT_CORE_DEVICE_ID}" ]; then
    IOT_CORE_DEVICE_ID=enviro-plugin
fi

GCP_CONNECTOR_NAME='Cloud IoT Core Connector'
GCP_CONNECTOR_INSTALLED=$(curl -s "${FOGLAMP_BASE_URL}/north" | jq -cr ".[] | select(.name == \"${GCP_CONNECTOR_NAME}\")")
if [[ ! ${GCP_CONNECTOR_INSTALLED} ]]; then
    curl -sX POST "${FOGLAMP_BASE_URL}/scheduled/task" \
    -H "Content-Type: application/json" \
    --data-binary @- <<DATA
{
    "name":"${GCP_CONNECTOR_NAME}",
    "plugin":"GCP",
    "type":"north",
    "schedule_repeat":30,
    "schedule_type":"3",
    "schedule_enabled":false,
    "config":
        {
            "project_id":{"value":"${GOOGLE_CLOUD_PROJECT}"},
            "region":{"value":"${GOOGLE_CLOUD_REGION}"},
            "registry_id":{"value":"${IOT_CORE_REGISTRY_ID}"},
            "device_id":{"value":"${IOT_CORE_DEVICE_ID}"},
            "key":{"value":"rsa_private"}
        }
}
DATA
fi