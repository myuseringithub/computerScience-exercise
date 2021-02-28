#!/bin/bash

TESTS_PATH="/u/c/s/cs537-1/tests/p3/tests"

for testdesc in $( ls "$TESTS_PATH" -v | grep ".desc" ); do

    echo -n "  $testdesc - "
    echo $(head -1 $TESTS_PATH/$testdesc)
done
