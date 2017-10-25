#!/bin/bash
# Run code coverage.

if [ ! -d app ]; then
    echo 'script needs to be run from project base dir'
    exit
fi

sbt clean coverage test coverageReport

HTML=$(ls $(pwd)/target/scala-*/scoverage-report/index.html)
echo -e "\nHTML coverage report: $HTML"
