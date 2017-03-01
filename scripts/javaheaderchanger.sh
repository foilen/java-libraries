#!/bin/bash

RUN_PATH="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd $RUN_PATH

./javaheaderchanger.pl javaheaderchanger.txt
