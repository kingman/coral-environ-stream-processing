# FogLAMP installation on Coral Dev Board
## Prerequisite
The Dev Board set up and connected to internet by follwing the steps 1-5 of the [Getting Started guide](https://coral.ai/docs/dev-board/get-started).

## FogLAMP installation
1. Connect to the board's shell via MDT, run the following command in your dev computer shell:

        mdt shell

1. Download the installation script, run the following commands in the dev board's shell:

        cd $HOME
        wget https://raw.githubusercontent.com/kingman/coral-environ-stream-processing/master/foglamp/install.sh

1. Run the installation script in the dev board's shell:

        . install.sh
    * press **enter** for the "Configuring Kerberos authentication" prompt.

1. Wait the dev board to reboot and verify that the FogLAMP is running by browsing from your dev computer to: **http://[board-IP]**. This should open the FogLAMP web UI.

## Add public key to the FogLAMP device in Google Cloud IoT Core
1. Setting environment variables that store your Google Cloud project id and region, run the following command in your dev computer shell:

        export GOOGLE_CLOUD_PROJECT=[PROJECT_ID]
        export GOOGLE_CLOUD_REGION=[REGION]

1. Download the public key from the dev board, run the following command in your dev computer shell:

        mdt pull /usr/local/foglamp/data/etc/certs/pem/rsa_public.pem .
1. Add public key to the FogLAMP device in Google Cloud IoT Core, run the following command in your dev computer shell:

        gcloud iot devices credentials create \
        --project=${GOOGLE_CLOUD_PROJECT} \
        --region=${GOOGLE_CLOUD_REGION} \
        --registry=device-registry \
        --device=enviro-plugin \
        --path=rsa_public.pem \
        --type=rsa-pem

## Configure the Google Cloud IoT Core north plugin on FogLAMP
1. Connect to the board's shell via MDT, run the following command in your dev computer shell:

        mdt shell

1. Download the plugin configuration script, run the following commands in the dev board's shell:

        cd $HOME
        wget https://raw.githubusercontent.com/kingman/coral-environ-stream-processing/master/foglamp/plugin-configure.sh

1. Setting environment variables that store your Google Cloud project id and region, run the following command in your dev computer shell:

        export GOOGLE_CLOUD_PROJECT=[PROJECT_ID]
        export GOOGLE_CLOUD_REGION=[REGION]

1. Run the plugin configuration script in the dev board's shell:

        . plugin-configure.sh