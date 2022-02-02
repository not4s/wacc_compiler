#!/usr/bin/env bash

# Run our ./compile, get output and error code
cd ..
./grun "antlr.Basic program -ps temp.ps wacc_test/$1"

# Workaround to rename root folder "example_wacc" to "results"
oldDir=$(dirname $1)
dir=${oldDir/sample_programs/results}
name=$(basename $1 .wacc)

convert "-density" "300" "temp.ps" "temp.jpg"

# Make directory and any intermediates, save test output to it
mkdir -p "wacc_test/$dir"
rm -f "$dir/$name"
cp "temp.jpg" "wacc_test/$dir/$name.jpg"
