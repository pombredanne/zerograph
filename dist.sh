#!/usr/bin/env bash

VERSION="1.beta"
DIST_NAME="zerograph-server-$VERSION"

cd `dirname $0`

rm -rf dist
mkdir dist

gradle install
mv build/install/zerograph dist/$DIST_NAME
rm dist/$DIST_NAME/bin/*.bat

cd dist
tar cvf $DIST_NAME.tar $DIST_NAME
xz -e $DIST_NAME.tar
