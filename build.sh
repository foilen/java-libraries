#!/bin/bash

set -e

RUN_PATH="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

echo ----==[ Update copyrights ]==----
cd $RUN_PATH/scripts
./javaheaderchanger.sh > /dev/null

echo ----==[ Compile ]==----
cd $RUN_PATH
./gradlew clean build

echo ----==[ Operation completed successfully ]==----
