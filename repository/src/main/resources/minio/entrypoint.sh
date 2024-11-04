#!/bin/sh

/usr/bin/minio server /data --console-address :9001 &

sleep 5

/bin/sh /docker-entrypoint-initdb.d/init-minio.sh

wait
