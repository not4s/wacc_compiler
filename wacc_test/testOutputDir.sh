#!/usr/bin/env bash

shopt -s globstar

failed=0
processed=0

for f in $2
do
    ./testOutputOne "$1" "$f"

    if [[ $? != 0 ]]
    then
        ((failed++))
    fi

    ((processed++))
done

echo "Failed $failed out of $processed tests."

if [[ failed -eq 0 ]]
then
    exit 0
else
    exit 1
fi