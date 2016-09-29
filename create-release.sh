#!/bin/bash

set -e

# Check params
if [ $# -ne 1 ]
	then
		echo Usage: $0 version;
    echo E.g: $0 0.1.0
		echo Version is MAJOR.MINOR.BUGFIX
		exit 1;
fi

VERSION=$1
RUN_PATH="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

echo ----==[ Update copyrights ]==----
cd $RUN_PATH/scripts
./javaheaderchanger.sh > /dev/null

echo ----==[ Update version ]==----
cd $RUN_PATH
cp gradle.properties gradle.properties.old
sed "s/master-SNAPSHOT/$VERSION/g" gradle.properties.old > gradle.properties

echo ----==[ Compile and deploy to jcenter ]==----
./gradlew bintrayUpload

echo ----==[ Replace version ]==----
mv gradle.properties.old gradle.properties

echo ----==[ Create git tag ]==----
git tag -a -m $VERSION $VERSION

echo ----==[ Operation completed successfully ]==----

echo You can execute
echo git push --tags
echo to push the tag

echo
echo You can publish https://bintray.com/foilen/maven
