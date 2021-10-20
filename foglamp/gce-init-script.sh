#!/bin/bash
apt-get update
apt-get -y install automake python3-dev libpython3.8-dev jq git

FOGLAMP_SOURCE_LIST_FILE=/etc/apt/sources.list.d/foglamp.list
if [ ! -f "${FOGLAMP_SOURCE_LIST_FILE}" ]; then
    echo "deb [arch=x86_64] http://archives.dianomic.com/foglamp/latest/ubuntu2004/x86_64/ ./" |  tee -a ${FOGLAMP_SOURCE_LIST_FILE}
fi

FOGLAMP_KEY_ADDED=$(apt-key list 2> /dev/null | grep foglamp-key)
if [[ ! ${FOGLAMP_KEY_ADDED} ]]; then
    wget -O - http://archives.dianomic.com/KEY.gpg | apt-key add -
fi

apt-get update
DEBIAN_FRONTEND=noninteractive apt-get -y install foglamp foglamp-gui foglamp-north-gcp

FOGLAMP_BASE_URL='http://localhost:8081/foglamp'

GCP_PLUGIN_INSTALLED=$(curl -s "${FOGLAMP_BASE_URL}/plugins/installed?type=north" | jq -cr '.plugins[] | select(.name == "GCP")')
until [ -n "${GCP_PLUGIN_INSTALLED}" ]; do
    GCP_PLUGIN_INSTALLED=$(curl -s "${FOGLAMP_BASE_URL}/plugins/installed?type=north" | jq -cr '.plugins[] | select(.name == "GCP")')
    sleep 3
done

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

until [ -n "${GCP_CONNECTOR_INSTALLED}" ]; do
    GCP_CONNECTOR_INSTALLED=$(curl -s "${FOGLAMP_BASE_URL}/north" | jq -cr ".[] | select(.name == \"${GCP_CONNECTOR_NAME}\")")
    sleep 3
done

FOGLAMP_CERT_DIR=/usr/local/foglamp/data/etc/certs/pem
GCP_ROOT_CERT_PATH="${FOGLAMP_CERT_DIR}/roots.pem"
if [ ! -f "${GCP_ROOT_CERT_PATH}" ]; then
    wget https://pki.goog/roots.pem -P ${FOGLAMP_CERT_DIR}
fi

PRIVATE_KEY_FILE=rsa_private.pem
PUBLIC_KEY_FILE=rsa_public.pem
GCP_IOT_DEVICE_PRIVATE_KEY_PATH="${FOGLAMP_CERT_DIR}/${PRIVATE_KEY_FILE}"
GCP_IOT_DEVICE_PUBLIC_KEY_PATH="${FOGLAMP_CERT_DIR}/${PUBLIC_KEY_FILE}"
if [ ! -f "${GCP_IOT_DEVICE_PRIVATE_KEY_PATH}" ]; then
    openssl genpkey -algorithm RSA -out ${PRIVATE_KEY_FILE} -pkeyopt rsa_keygen_bits:2048 \
    && openssl rsa -in ${PRIVATE_KEY_FILE} -pubout -out ${PUBLIC_KEY_FILE} \
    && mv ${PRIVATE_KEY_FILE} ${GCP_IOT_DEVICE_PRIVATE_KEY_PATH} \
    && mv ${PUBLIC_KEY_FILE} ${GCP_IOT_DEVICE_PUBLIC_KEY_PATH}
fi
