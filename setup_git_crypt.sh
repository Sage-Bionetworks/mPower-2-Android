#!/bin/bash
set -e

# install git-crypt and create a sim link
pushd /tmp
wget https://github.com/AGWA/git-crypt/archive/ccdcc76f8e1a639847a8accd801f5a284194e43f.zip -O git-crypt.zip
unzip git-crypt.zip
cd git-crypt-ccdcc76f8e1a639847a8accd801f5a284194e43f
make
sudo install git-crypt /usr/local/bin
popd

