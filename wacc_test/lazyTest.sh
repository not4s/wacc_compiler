#!/usr/bin/bash
# Ultimate mega script which runs make
# then depending on the flags runs all milestone tests

cd ..
make clean
make
cd wacc_test

case "$1" in
  ""|"-bf"|"-fb")
    ./testAll.sh
    ./testOutputAll.sh;;
  "-f")
    ./testAll.sh;;
  "-b")
    ./testOutputAll.sh;;
esac