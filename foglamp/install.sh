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

MENDEL_VERSION_PATH=/etc/mendel_version
if [ -f "${MENDEL_VERSION_PATH}" ]; then
    echo "## Install Foglamp on Coral Dev Board ##"

    sudo apt-get -y install automake python-dev python3-dev libpython2.7-dev libpython3.7-dev jq git

    FOGLAMP_SOURCE_LIST_FILE=/etc/apt/sources.list.d/foglamp.list

    if [ ! -f "${FOGLAMP_SOURCE_LIST_FILE}" ]; then
        echo "## Create apt repository sources file for FogLAMP"
        echo "deb [arch=arm64] http://archives.dianomic.com/foglamp/nightly/mendel/aarch64/ ./" | sudo tee -a ${FOGLAMP_SOURCE_LIST_FILE}
    fi

    FOGLAMP_KEY_ADDED=$(apt-key list 2> /dev/null | grep foglamp-key)
    if [[ ! ${FOGLAMP_KEY_ADDED} ]]; then
        echo "## Add FogLAMP repository GPG key"
        wget -O - http://archives.dianomic.com/KEY.gpg | sudo apt-key add -
    fi

    sudo apt-get update

    sudo apt-get -y install foglamp foglamp-gui

    NGINX_FIX_SCRIPT=/usr/local/bin/fix-nginx.sh
    if [ ! -f "${NGINX_FIX_SCRIPT}" ]; then
        echo "## Create nginx fix script"
        sudo tee -a ${NGINX_FIX_SCRIPT} <<EOF
#!/bin/sh

_NGINX_LOGS_DIR_PATH=/var/log/nginx
echo "Creating the nginx logs directory: ..."
mkdir -p "\${_NGINX_LOGS_DIR_PATH}"
EOF
        sudo chmod a+x ${NGINX_FIX_SCRIPT}
    fi
    
    NGINX_FIX_FILE=/lib/systemd/system/fix-nginx.service
    if [ ! -f "${NGINX_FIX_FILE}" ]; then
        echo "## Create nginx fix service configuration"
        sudo tee -a ${NGINX_FIX_FILE} <<EOF
[Unit]
Before=nginx.target
Wants=nginx.target

[Service]
ExecStart=${NGINX_FIX_SCRIPT}

[Install]
WantedBy=multi-user.target
EOF
        sudo chmod 644 ${NGINX_FIX_FILE}
    fi

    NGINX_FIX_SERVICE_NAME=fix-nginx.service
    if ! systemctl is-enabled --quiet ${NGINX_FIX_SERVICE_NAME}; then
        echo "## Enable the nginx fix service"
        sudo systemctl daemon-reload
        sudo systemctl enable ${NGINX_FIX_SERVICE_NAME}
    fi    
fi

FOGLAMP_CERT_DIR=/usr/local/foglamp/data/etc/certs/pem
GCP_ROOT_CERT_PATH="${FOGLAMP_CERT_DIR}/roots.pem"
if [ ! -f "${GCP_ROOT_CERT_PATH}" ]; then
    echo "## Download Google Cloud root cert"
    wget https://pki.goog/roots.pem -P ${FOGLAMP_CERT_DIR}
fi

PRIVATE_KEY_FILE=rsa_private.pem
PUBLIC_KEY_FILE=rsa_public.pem
GCP_IOT_DEVICE_PRIVATE_KEY_PATH="${FOGLAMP_CERT_DIR}/${PRIVATE_KEY_FILE}"
GCP_IOT_DEVICE_PUBLIC_KEY_PATH="${FOGLAMP_CERT_DIR}/${PUBLIC_KEY_FILE}"
if [ ! -f "${GCP_IOT_DEVICE_PRIVATE_KEY_PATH}" ]; then
    echo "## Generate Cloud IoT Core device keypair"
    openssl genpkey -algorithm RSA -out ${PRIVATE_KEY_FILE} -pkeyopt rsa_keygen_bits:2048 \
    && openssl rsa -in ${PRIVATE_KEY_FILE} -pubout -out ${PUBLIC_KEY_FILE} \
    && mv ${PRIVATE_KEY_FILE} ${GCP_IOT_DEVICE_PRIVATE_KEY_PATH} \
    && mv ${PUBLIC_KEY_FILE} ${GCP_IOT_DEVICE_PUBLIC_KEY_PATH}
fi

sudo reboot
