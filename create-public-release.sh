#!/bin/bash

set -e

RUN_PATH="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd $RUN_PATH

# Check params
if [ $# -ne 1 ]
    then
        echo Usage: $0 version;
        echo E.g: $0 0.1.0
        echo Version is MAJOR.MINOR.BUGFIX
        echo Latest version:
        git describe --abbrev=0
        exit 1;
fi

# Set environment
export LANG="C.UTF-8"
export VERSION=$1

./step-clean-compile.sh
./step-upload-maven-central.sh
./step-git-tag.sh

echo ----[ Operation completed successfully ]----

echo
echo You can see the publishing progress on https://central.sonatype.com/publishing/deployments
echo
echo Then, you can see published items on
echo https://repo1.maven.org/maven2/com/foilen/jl-smalltools-main/
echo You can send the tag: git push --tags
