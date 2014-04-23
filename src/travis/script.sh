#!/usr/bin/env bash

cd `dirname $0`/../..

HOME=`pwd`
BIN=$HOME/bin
SRC=$HOME/src
TEST=$SRC/test
PYTHON=$SRC/client/python

function open_test_database {
    PORT=$1
    $BIN/zerograph open $PORT
}

function drop_test_database {
    PORT=$1
    $BIN/zerograph drop $PORT
}

function test_server {
    echo "######################"
    echo "### TESTING SERVER ###"
    echo "######################"
    cd $HOME
    ./gradlew assemble
    ./gradlew check
    STATUS=$?
    echo ""
    return $STATUS
}

function test_python_client {
    echo "#############################"
    echo "### TESTING PYTHON CLIENT ###"
    echo "#############################"
    cd $PYTHON
    open_test_database 47471
    nosetests test
    STATUS=$?
    drop_test_database 47471
    echo ""
    return $STATUS
}

test_server
STATUS=$?
if [ $STATUS != 0 ]
then
    exit $STATUS
fi

test_python_client
STATUS=$?
if [ $STATUS != 0 ]
then
    exit $STATUS
fi
