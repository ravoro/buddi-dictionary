#!/bin/bash
# Run local dev server

if [ ! -d app ]; then
    echo 'script needs to be run from project base dir'
    exit
fi

sbt -Dconfig.resource=custom.conf run
