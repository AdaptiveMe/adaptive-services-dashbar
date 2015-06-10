#!/bin/bash

HOME_DIR=`pwd`

command -v compass >/dev/null 2>&1 || { echo "I require compass but it's not installed. Install with ´sudo gem install compass --pre´  Aborting." >&2; exit 1; }
cd $HOME_DIR/deps

#git submodule update --recursive --remote
if [ $? -ne 0 ]; then
    echo "ERROR: Unable to update submodules."
    exit 1
fi

cd $HOME_DIR/deps/che-core
mvn clean install
if [ $? -ne 0 ]; then
    echo "ERROR: che-core failed to build."
    exit 1
fi

cd $HOME_DIR/deps/che
mvn clean install
if [ $? -ne 0 ]; then
    echo "ERROR: che failed to build."
    exit 1
fi

cd $HOME_DIR/deps/user-dashboard
mvn clean install
if [ $? -ne 0 ]; then
    echo "ERROR: user-dashboard failed to build."
    exit 1
fi

cd $HOME_DEPS/deps/cli
mvn clean install
if [ $? -ne 0 ]; then
    echo "ERROR: cli failed to build."
    exit 1
fi

cd $HOME_DEPS
echo "ALL DONE."

