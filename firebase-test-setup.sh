#!/bin/bash
set -e

gcloud config set project mpower2-e03b9
gcloud auth activate-service-account mpower2-e03b9@appspot.gserviceaccount.com --key-file ../android-certificates/gcloud_service_account_106c1870.json