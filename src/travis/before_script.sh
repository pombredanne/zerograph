#!/usr/bin/env bash

cd `dirname $0`/../..

HOME=`pwd`
BIN=$HOME/bin
SRC=$HOME/src
TEST=$SRC/test
PYTHON=$SRC/client/python

cd $HOME
./gradlew run &
sleep 20
