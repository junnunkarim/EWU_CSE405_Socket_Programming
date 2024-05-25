# Define the compiler and the flags
JAVAC = javac
JAVA = java
SRC_DIR = src
CLASS_DIR = bin

# define compiler flags
CFLAGS = -g

# default target: compile all
all: build

# clean up .class files
clean:
	rm -rf $(CLASS_DIR)/*.class

# compile all java files
build:
	@mkdir -p $(CLASS_DIR)
	$(JAVAC) $(CFLAGS) -d $(CLASS_DIR) $(SRC_DIR)/*.java

# Run the server
run-server:
	$(JAVA) -cp $(CLASS_DIR) Server

# Run the client
run-client:
	$(JAVA) -cp $(CLASS_DIR) Client

# Define all targets
.PHONY: all clean build run-server run-client
