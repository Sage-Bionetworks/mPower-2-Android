#!/bin/bash
set -e

gcloud config set project mpower2-e03b9
gcloud --quiet components update
gcloud --quiet components install beta
gcloud auth activate-service-account mpower2-e03b9@appspot.gserviceaccount.com --key-file ../android-certificates/gcloud_service_account_106c1870.json
echo "y" | gcloud firebase test android run --app ./app/build/outputs/apk/internal/debug/app-internal-debug.apk --test ./app/build/outputs/apk/androidTest/internal/debug/app-internal-debug-androidTest.apk
