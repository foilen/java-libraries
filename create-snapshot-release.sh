#!/bin/bash

set -e

VERSION=master-SNAPSHOT

# Update copyrigths
echo ----==[ Update copyrigths ]==----
pushd scripts
./javaheaderchanger.sh > /dev/null
popd

# Compile
echo ----==[ Compile ]==----
./gradlew clean install

# Zip
for i in `ls`
do
  if [ -d $i/build/libs ]; then
    echo ----==[ Zip $i ]==----
    rm -f $i.zip
    pushd $i/build/libs
    mkdir -p com/foilen/$i/$VERSION/
    mv *.jar com/foilen/$i/$VERSION/
    mv ../poms/pom-default.xml com/foilen/$i/$VERSION/$i-$VERSION.pom
    zip -r ../../../$i.zip com
    popd
  fi
done

echo ----==[ Operation completed successfully ]==----
