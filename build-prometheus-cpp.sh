#!/bin/bash

set -e
set -o pipefail
set -x

rm -rf prometheus-*/


PROMETHEUS_CPP_VERSION=1.1.0
tar xvzf prometheus-cpp-${PROMETHEUS_CPP_VERSION}.tar.gz

CIVETWEB_VERSION=1.15
tar xvzf civetweb-$CIVETWEB_VERSION.tar.gz
rm -rf prometheus-cpp-$PROMETHEUS_CPP_VERSION/3rdparty/civetweb
mv civetweb-$CIVETWEB_VERSION prometheus-cpp-$PROMETHEUS_CPP_VERSION/3rdparty/civetweb

pushd prometheus-cpp-$PROMETHEUS_CPP_VERSION/
mkdir -p _build/
pushd _build
# If cmake is out of date:
#     $ wget ... # download cmake
#       # --DCMAKE_USE_OPENSSL=OFF may not be necessary on all systems
#       #
#     $ ./bootstrap --parallel=2 --prefix=/usr/local -- -DCMAKE_USE_OPENSSL=OFF -DBUILD_TESTING=OFF
#     $ make
#     # make install
cmake .. -DCMAKE_BUILD_TYPE=Debug \
         -DBUILD_SHARED_LIBS=OFF \
         -DENABLE_PUSH=OFF \
         -DENABLE_TESTING=OFF \
         -DENABLE_COMPRESSION=OFF \
         -DUSE_THIRDPARTY_LIBRARIES=ON
cmake --build . --parallel 2 --config Debug
popd
popd

LIB_DIR=lib.$( uname -m )
mkdir -p $LIB_DIR/
cp prometheus-cpp-$PROMETHEUS_CPP_VERSION/_build/lib/* $LIB_DIR/

# TODO: Look into whether or not the _build/{core,pull}/include stuff needs to
# go into a arch specific include/ directory
mkdir -p include/
cp -R prometheus-cpp-$PROMETHEUS_CPP_VERSION/core/include/* \
      prometheus-cpp-$PROMETHEUS_CPP_VERSION/pull/include/* \
      prometheus-cpp-$PROMETHEUS_CPP_VERSION/_build/core/include/* \
      prometheus-cpp-$PROMETHEUS_CPP_VERSION/_build/pull/include/* \
      include/

# TODO: cleanup things in a trap?

