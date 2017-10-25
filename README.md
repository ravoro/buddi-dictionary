# Personal Dictionary

[![Build Status](https://travis-ci.org/ravoro/personal-dictionary.svg?branch=master)](https://travis-ci.org/ravoro/personal-dictionary)

Personalizable online dictionary allowing you to:

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
- Run the coverage report: `./scripts/test_coverage.sh`
