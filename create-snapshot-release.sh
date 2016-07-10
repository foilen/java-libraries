#!/bin/bash

set -e

VERSION=master-SNAPSHOT
RUN_PATH="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Update copyrights
echo ----==[ Update copyrights ]==----
cd $RUN_PATH/scripts
./javaheaderchanger.sh > /dev/null

# Go through projects
cd $RUN_PATH
rm -f *.zip
for i in `cat projects-order.txt`
do
  cd $RUN_PATH
  if [ -f $i/pom.xml ]; then
  
    cd $RUN_PATH/$i
  
    echo ----==[ Compile $i ]==----
    mvn clean install
  
    echo ----==[ Zip $i ]==----
    cd $RUN_PATH/$i/target
    mkdir -p com/foilen/$i/$VERSION/
    mv *.jar com/foilen/$i/$VERSION/
    cp ../pom.xml com/foilen/$i/$VERSION/$i-$VERSION.pom
    zip -r $RUN_PATH/$i.zip com
  fi
done

echo ----==[ Operation completed successfully ]==----
