#!/bin/bash

set -e
set -o pipefail
set -x

rm -rf libmodbus-*/

# TODO: modbus.h includes modbus-rtu.h and modbus-tcp.h, each include modbus.h
#       I would think that there should be no circular dependencies
# TODO: libmodbus doesn't have an include/ directory

VERSION=3.1.10
tar xvzf libmodbus-$VERSION.tar.gz
pushd libmodbus-$VERSION
./autogen.sh
./configure CPPGLAGS=-DDEBUG CFLAGS="-g -O0"
make -j $( nproc )
popd

LIB_DIR=lib.$( uname -m )
mkdir -p $LIB_DIR

cp ./libmodbus-$VERSION/src/.libs/*.so* $LIB_DIR/

# TODO: parameterize the include directory on cpu architecture
mkdir -p include
# I can't skip modbus-tcp.h even though I don't need it: modbus.h includes it, always
# TODO: Perhaps copy everything that doesn't have the `-private.h` suffix?
cp ./libmodbus-$VERSION/src/modbus{,-tcp,-rtu,-version}.h include/

# TODO: put this in a trap, add a commandline parameter to not delete it
#rm -rf libmodbus-$VERSION/

