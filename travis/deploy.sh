#!/bin/bash
set -e

bundle exec fastlane internal alias:"$KEY_ALIAS" storepass:"$KEYSTORE_PASSWORD" keypass:"$KEY_PASSWORD" signed_apk_path:"app/build/outputs/apk/app-internal-release.apk"

