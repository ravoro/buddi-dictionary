#!/bin/bash
# Run the entire test suite or a subset via argument.
#
# Examples:
#     * Execute all tests:               `./scripts/test.sh`
#     * Execute all tests in a package:  `./scripts/test.sh controllers.*`
#     * Execute specific test spec:      `./scripts/test.sh controllers.WordControllerSpec`

if [ ! -d app ]; then
    echo 'script needs to be run from project base dir'
    exit
fi

EXEC_TESTS="test"
if [ "$#" -ne 0 ]; then
    EXEC_TESTS="test-only $@"
fi

sbt "${EXEC_TESTS}"
