#!/bin/bash
set -e

# unlock the repo and throw away the key
openssl aes-256-cbc -K $encrypted_cafca427fc2f_key -iv $encrypted_cafca427fc2f_iv -in git-crypt-android-certificates.key.enc -out git-crypt-android-certificates.key -d
git clone https://github.com/Sage-Bionetworks/android-certificates ../android-certificates
pushd ../android-certificates
git-crypt unlock $TRAVIS_BUILD_DIR/git-crypt-android-certificates.key
rm -rf git-crypt-android-certificates.key
popd