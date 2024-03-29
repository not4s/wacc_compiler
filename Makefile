# Sample Makefile for the WACC Compiler lab: edit this to build your own comiler

# Useful locations

ANTLR_DIR	 := antlr_config
SOURCE_DIR	 := src/main/kotlin
ANTLR_SOURCE_DIR := $(SOURCE_DIR)/antlr
OUTPUT_DIR	 := bin
COMPILATION_DIR	:=	wacc_test/compiled_code

PYTHON_SOURCE_DIR	 := ide
PYTHON_ANTLR_SOURCE_DIR := $(PYTHON_SOURCE_DIR)/antlr

# Project tools

ANTLR	:= antlrBuild
MKDIR	:= mkdir -p
JAVAC	:= javac
RM	:= rm -rf

# Configure project Java flags

FLAGS := -d $(OUTPUT_DIR) -cp bin:lib/antlr-4.9.3-complete.jar
JFLAGS	:= -sourcepath $(SOURCE_DIR) $(FLAGS)


# The make rules:

# run the antlr build script then attempts to compile all .java files within src/antlr
all:
	cd $(ANTLR_DIR) && ./$(ANTLR)
	$(MKDIR) $(OUTPUT_DIR)
	$(JAVAC) $(JFLAGS) $(ANTLR_SOURCE_DIR)/*.java
	mvn package

# clean up all of the compiled files
clean:
	$(RM) $(OUTPUT_DIR) $(SOURCE_DIR)/antlr $(COMPILATION_DIR)
	$(RM) *.s
	mvn clean
	$(RM) $(PYTHON_ANTLR_SOURCE_DIR)
	cd $(ANTLR_DIR) && ./namedeclash --clean

.PHONY: all clean
