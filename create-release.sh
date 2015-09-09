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
RUN_PATH="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Update copyrights
echo ----==[ Update copyrights ]==----
cd $RUN_PATH/scripts
./javaheaderchanger.sh > /dev/null

cd $RUN_PATH
rm -f *.zip
for i in `cat projects-order.txt`
do
  cd $RUN_PATH
  if [ -f $i/pom.xml ]; then
  
    cd $RUN_PATH/$i

    echo ----==[ Update version ]==----
    cp pom.xml pom.xml.old
    sed "s/master-SNAPSHOT/$VERSION/g" pom.xml.old > pom.xml
  
    echo ----==[ Compile $i ]==----
    mvn clean install
  
    echo ----==[ Zip $i ]==----
    cd $RUN_PATH/$i/target
    mkdir -p com/foilen/$i/$VERSION/
    mv *.jar com/foilen/$i/$VERSION/
    cp ../pom.xml com/foilen/$i/$VERSION/$i-$VERSION.pom
    zip -r $RUN_PATH/$i.zip com
    
    echo ----==[ Replace version ]==----
    cd $RUN_PATH/$i
    mv pom.xml.old pom.xml
    
  fi
done

echo ----==[ Operation completed successfully ]==----
