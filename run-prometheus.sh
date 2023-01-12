#!/bin/bash

set -x
set -e
set -o pipefail

SOCAT_PID=0
JAVA_PID=0
POWERMONITORING_PID=0
function cleanup()
{
    docker stop node-exporter || true
    docker stop prometheus || true

    if [[ $POWERMONITORING_PID != 0 ]] ; then
        kill $POWERMONITORING_PID || true
        wait $POWERMONITORING_PID || true
    fi
    if [[ $JAVA_PID != 0 ]] ; then
        kill $JAVA_PID || true
        wait $JAVA_PID || true
    fi
    if [[ $SOCAT_PID != 0 ]] ; then
        kill $SOCAT_PID || true
        wait $SOCAT_PID || true
    fi
}
trap cleanup EXIT

case "$( uname -m )" in
    x86_64)
        socat PTY,raw,echo=0,cs8,link=COM8 PTY,raw,echo=0,cs8,link=COM9 &
        # TODO: find something to wait on, rather than sleep on
        sleep 1
        SOCAT_PID=$!

        java -cp test/integration Main < COM8 > COM8 &
        JAVA_PID=$!

        LD_LIBRARY_PATH=lib.$( uname -m ) ./powermonitoring COM9 &
        POWERMONITORING_PID=$!
        # TODO: find something to wait on, rather than sleep on
        sleep 1
        ;;

    arm7l)
        # TODO: start the power monitor
        ;;
esac

case "$( uname -m )" in
    x86_64)
        docker rm node-exporter || true
        docker run --detach \
                   --name node-exporter \
                   --net="host" \
                   --pid="host" \
                   --volume "/:/host:ro,rslave" \
                   prom/node-exporter:latest \
                   --path.rootfs=/host

        docker rm prometheus || true
        docker run --rm \
                   --name prometheus \
                   --net=host \
                   --volume $( dirname $( realpath $0 ) )/prometheus/prometheus.yml:/etc/prometheus/prometheus.yml:rw \
                   prom/prometheus
        ;;

    arm7l)
        # TODO: run grafana on the raspberry pi
        ;;
esac

