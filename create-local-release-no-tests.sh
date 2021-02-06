#!/bin/bash

set -e

# Set environment
export LANG="C.UTF-8"
export VERSION=$1

RUN_PATH="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd $RUN_PATH

if [ -z "$VERSION" ]; then
    export VERSION=$(git rev-parse --abbrev-ref HEAD)-SNAPSHOT
fi

./step-clean.sh
./step-compile-no-tests.sh
