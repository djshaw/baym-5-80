EXE=powermonitoring
default: $(EXE)

SOURCES=main.cpp
OBJECTS=$(SOURCES:.cpp=.o)
-include $(OBJECTS:.o=.d)

LIB_DIR=lib.$(shell uname -m)
LIBS=-lmodbus -lprometheus-cpp-pull -lprometheus-cpp-core -latomic -L$(LIB_DIR) -pthread

CXXFLAGS = -g \
	   -Iinclude/ \
	   -Wall \
	   -Wextra \
	   -pedantic-errors \
    	   -Wl,--allow-shlib-undefined \
           -Wno-unused-parameter \
	   -Werror \
	   -MP \
	   -MD


%.o: %.c Makefile
	$(CXX) $(CXXFLAGS) $<

$(EXE): $(OBJECTS) Makefile
	$(CXX) $(OBJECTS) $(LIBS) -o $(EXE)
	@# TODO: use built in makefile variables for providing libraries and things

.PHONY: dependencies
dependencies:
	@# cmake for building prometheus
	@# maven for building default-jdk-headless, jboss, socat for the java
	@# virtual-baym integration test
	apt install -y autoconf \
		       cmake \
		       default-jdk-headless \
		       gcc \
		       libtool \
		       maven \
		       snapd \
		       socat \
		       subversion \
		       vim
	snapd install yq

.PHONY: run
run: $(EXE)
	LD_LIBRARY_PATH=$(LIB_DIR)/ ./$(EXE) /dev/ttyUSB0

.PHONY: clean
clean:
	rm -f *.o *.d $(EXE)

.PHONY: debug
debug: $(EXE)
	@# TODO: put /dev/ttyUSB0 in a Makefile variable
	LD_LIBRARY_PATH=$(LIB_DIR)/ gdb --args ./$(EXE) /dev/ttyUSB0

.PHONY: tags
tags:
	find . /usr/include/linux -\( -name "*.c*" -or -name "*.h*" -\) -and -type f -and -print | ctags --extras=+f -a -L -

.PHONY: lint
lint:
	@# One file per invocation?
	clang-format-12 --dry-run -Wclang-format-violations -Werror -style=file *.c*

