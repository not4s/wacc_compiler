#!/usr/bin/env python3
from subprocess import Popen, PIPE, STDOUT
import os, sys, shutil

NO_OF_ARGS = 1

# check the number of arguments
if len(sys.argv) != NO_OF_ARGS + 1:
    print("Error: Invalid number of arguments")
    sys.exit()

# tokenize the path given
path = sys.argv[1]
file_type = path.split('.')[-1]
test_name = ''.join(file_type).split('/')[-1]

# check if the file Exists
if not os.path.isfile(path):
    print(f"Error: Program not found at {path}")
    sys.exit()

# check the file type
if file_type != 'wacc':
    print("Error: File given is not a wacc file")
    
# build path
path = os.path.abspath(path)
file_name = os.path.splitext(os.path.basename(path))[0]
compile_dir = 'compiled_code'

# TODO: output for the .s file
output = 'TODO'

# if compilation directory does not exist, make the directory
if not os.path.exists(compile_dir):
    os.makedirs(compile_dir)

# change dir to the compilation directory
os.chdir(compile_dir)

# TODO: we can defo use the bin as the compilation directory
# but let's use the root directory for now
# I'm not sure about the conventions

# write output to .s file in compilation directory
compiled_fname = file_name + '.s'
with open(compiled_fname, 'w') as f:
    f.write(output)
    f.close()
    shutil.move(compiled_fname, compile_dir + compiled_fname)

# prints success if reached
# print("Compiled successfully")
print("")
