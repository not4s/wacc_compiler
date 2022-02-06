#!/usr/bin/env bash

cd ..
make clean
make
cd wacc_test
./testAll.sh