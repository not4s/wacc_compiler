#!/usr/bin/env bash

# Run our ./compile, get output and error code
cd ..
actualStd=$(./compile wacc_test/$1)
actualErr=$?
cd wacc_test

# Workaround to rename root folder "example_wacc" to "results"
oldDir=$(dirname $1)
dir=${oldDir/sample_programs/results}
dircache=${oldDir/sample_programs/reference_cache}
name=$(basename $1 .wacc)

a=`cat $1`
b=`cat "$dircache/$name.ref"`

# Check if the output of this file has been pre-cached.
if test -f "$dircache/$name.ref"
then
  # Make sure file contains actual wacc source code (ie. wacc has not been modified)
  if [[ "$b" == *"$a"* ]]
  then
    expectedStd=$b
  else
    expectedStd=$(./refCompile -s $1)
  fi
else
  # Run reference compiler if not
  expectedStd=$(./refCompile -s $1)
fi
expectedErr="0"

# This regex captures the exit code. Must be a separate variable to allow whitespace capture.
regex="Exit code ([0-9]+) returned"
if [[ $expectedStd =~ $regex ]]
then
    # Save captured error code to expectedErr
    expectedErr="${BASH_REMATCH[1]}"
fi

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
