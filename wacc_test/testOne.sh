#!/usr/bin/env bash

# Run our ./compile, get output and error code
cd ..
actualStd=$(./compile $1)
actualErr=$?
cd wacc_test

# Run reference compiler
expectedStd=$(./refCompile -s $1)
expectedErr="0"

# This regex captures the exit code. Must be a separate variable to allow whitespace capture.
regex="Exit code ([0-9]+) returned"
if [[ $expectedStd =~ $regex ]]
then
    # Save captured error code to expectedErr
    expectedErr="${BASH_REMATCH[1]}"
fi

# Workaround to rename root folder "example_wacc" to "results"
oldDir=$(dirname $1)
dir=${oldDir/sample_programs/results}
name=$(basename $1 .wacc)

# Make directory and any intermediates, save test output to it
mkdir -p $dir
rm -f "$dir/$name"
echo "\
ඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞ
                    OUR COMPILER OUTPUT (code: $actualErr)
ඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞ

$actualStd

ඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞ
                    REFERENCE COMPILER OUTPUT (code: $expectedErr)
ඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞඞ

$expectedStd" >> "$dir/$name"

# Pretty print result, exit with error code if test failed.
red="\033[0;31m"
green="\033[0;32m"
clear="\033[0m"
if [[ $actualErr == $expectedErr ]]
then
    echo -e "${green}pass $1${clear}"
    exit 0
else
    echo -e "${red}FAIL $1${clear} - expected: $expectedErr, got: $actualErr"
    exit 1
fi
