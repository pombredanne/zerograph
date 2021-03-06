#!/usr/bin/env bash

cd `dirname $0`/../..

HOME=`pwd`
BIN=$HOME/bin
SRC=$HOME/src
TEST=$SRC/test
PYTHON=$SRC/client/python

sudo apt-get install -y build-essential python-nose python3-nose

cd $PYTHON
sudo pip install -r $PYTHON/requirements.txt
