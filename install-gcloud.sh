#!/bin/sh
set -e

# instructions from https://cloud.google.com/sdk/docs/downloads-apt-get
echo "Installing the Google Cloud SDK..."
export CLOUD_SDK_REPO="cloud-sdk-$(lsb_release -c -s)"
echo "deb http://packages.cloud.google.com/apt $CLOUD_SDK_REPO main" | sudo tee -a /etc/apt/sources.list.d/google-cloud-sdk.list
curl https://packages.cloud.google.com/apt/doc/apt-key.gpg | sudo apt-key add -
sudo apt-get update -y && sudo apt-get install -y --allow-unauthenticated google-cloud-sdk

