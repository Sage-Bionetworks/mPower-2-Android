#!/bin/bash
set -e

gcloud config set project mpower2-e03b9
gcloud --quiet components update
gcloud --quiet components install beta
gcloud auth activate-service-account travis@mpower2-215017.iam.gserviceaccount.com --key-file ../android-certificates/gcloud_service_account_6f4ab81e.json