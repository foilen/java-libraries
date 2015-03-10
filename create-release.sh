#!/bin/bash

set -e

# Check params
if [ $# -ne 1 ]
	then
		echo Usage: $0 version;
    echo E.g: $0 2015.01.01
		exit 1;
fi

VERSION=$1

# Update copyrigths
echo ----==[ Update copyrigths ]==----
pushd scripts
./javaheaderchanger.sh > /dev/null
popd

# Update version
echo ----==[ Update version ]==----
cp build.gradle build.gradle.old
sed "s/master-SNAPSHOT/$VERSION/g" build.gradle.old > build.gradle

# Compile
echo ----==[ Compile ]==----
./gradlew clean install

# Zip
for i in `ls`
do
  if [ -d $i/build/libs ]; then
    echo ----==[ Zip $i ]==----
    rm $i.zip
    pushd $i/build/libs
    mkdir -p com/foilen/$i/$VERSION/
    mv *.jar com/foilen/$i/$VERSION/
    mv ../poms/pom-default.xml com/foilen/$i/$VERSION/$i-$VERSION.pom
		zip -r ../../../$i.zip com
    popd
	fi
done

# Replace version
echo ----==[ Replace version ]==----
mv build.gradle.old build.gradle

echo ----==[ Operation completed successfully ]==----
