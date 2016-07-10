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

# Update copyrights
echo ----==[ Update copyrights ]==----
cd $RUN_PATH/scripts
./javaheaderchanger.sh > /dev/null

cd $RUN_PATH
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

  fi
done

cd $RUN_PATH
for i in `cat projects-order.txt`
do
  cd $RUN_PATH
  if [ -f $i/pom.xml ]; then

    cd $RUN_PATH/$i

    echo ----==[ Deploy to jcenter $i ]==----
    mvn deploy

    echo ----==[ Replace version ]==----
    cd $RUN_PATH/$i
    mv pom.xml.old pom.xml

  fi
done

echo ----==[ Create git tag ]==----
git tag -a -m $VERSION $VERSION

echo ----==[ Operation completed successfully ]==----

echo You can execute
echo git push --tags
echo to push the tag
