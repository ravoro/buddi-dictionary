# BudDi Dictionary

[![Build Status](https://travis-ci.org/ravoro/buddi-dictionary.svg?branch=master)](https://travis-ci.org/ravoro/buddi-dictionary)
[![Coverage Status](https://coveralls.io/repos/github/ravoro/buddi-dictionary/badge.svg?branch=master)](https://coveralls.io/github/ravoro/buddi-dictionary?branch=master)
[![BCH compliance](https://bettercodehub.com/edge/badge/ravoro/buddi-dictionary?branch=master)](https://bettercodehub.com/)


> ***BudDi** - your dictionary buddy :)*


BudDi is a personalizable online dictionary allowing you to:

- View and compare standard defitions from various reliable sources.
- Provide and view your own custom definitions.
- Seek terms and view definitions in multiple languages.

... all in one place!


## Tech
The service is powered by Scala/Play.


## Requirements
- `sbt`


## Run Locally
- Set up database access in `conf/application.conf`
- Run the dev server: `sbt run`


# Tests
- Run the test suite: `./scripts/test.sh`
- Run the integration test suite: `./scripts/test_integration.sh`
- Run the coverage report: `./scripts/test_coverage.sh`
