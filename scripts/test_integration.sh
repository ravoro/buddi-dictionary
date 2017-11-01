#!/bin/bash
# Run the entire integration test suite or a subset via argument.
#
# Examples:
#     * Execute all tests:               `./scripts/test_integration.sh`
#     * Execute all tests in a package:  `./scripts/test_integration.sh repositories.*`
#     * Execute specific test spec:      `./scripts/test_integration.sh repositories.WordRepositorySpec`

if [ ! -d app ]; then
    echo 'script needs to be run from project base dir'
    exit
fi

EXEC_TESTS="it:test"
if [ "$#" -ne 0 ]; then
    EXEC_TESTS="it:test-only $@"
fi

sbt "${EXEC_TESTS}"
