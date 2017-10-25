#!/bin/bash
# Run the entire test suite or a subset via argument.
#
# Examples:
#     * Execute all tests:               `./test.sh`
#     * Execute all tests in a package:  `./test.sh controllers.*`
#     * Execute specific test spec:      `./test.sh controllers.WordControllerSpec`

if [ ! -d app ]; then
    echo 'script needs to be run from project base dir'
    exit
fi

EXEC_TESTS="test"
if [ "$#" -ne 0 ]; then
    EXEC_TESTS="test-only $@"
fi

sbt "${EXEC_TESTS}"
