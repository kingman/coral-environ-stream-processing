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

DYNAMIC_SCHEMA_PROCESSOR_ROOT=${HOME}/cloud-iot-stream-processing-dynamic-schema
LAB_SOURCE_RAW_MASTER_URL=https://raw.githubusercontent.com/kingman/coral-environ-stream-processing/master

FILE_PATH=data-configs/input-data-schema.json
wget -O ${DYNAMIC_SCHEMA_PROCESSOR_ROOT}/${FILE_PATH} ${LAB_SOURCE_RAW_MASTER_URL}/${FILE_PATH}

FILE_PATH=data-configs/foglamp-sinusoid-table-schema.json
wget -O ${DYNAMIC_SCHEMA_PROCESSOR_ROOT}/${FILE_PATH} ${LAB_SOURCE_RAW_MASTER_URL}/${FILE_PATH}

FILE_PATH=data-configs/foglamp-sinusoid-schema-mapping.json
wget -O ${DYNAMIC_SCHEMA_PROCESSOR_ROOT}/${FILE_PATH} ${LAB_SOURCE_RAW_MASTER_URL}/${FILE_PATH}

wget -O ${DYNAMIC_SCHEMA_PROCESSOR_ROOT}/scripts/set-env.sh ${LAB_SOURCE_RAW_MASTER_URL}/processor-update/set-env.sh

wget -O ${DYNAMIC_SCHEMA_PROCESSOR_ROOT}/scripts/generate-tf-backend.sh ${LAB_SOURCE_RAW_MASTER_URL}/processor-update/generate-tf-backend.sh