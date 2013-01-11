SRC = $(shell find src -name *.java)
TEST = $(shell find test -name *.java)
VERSION = $(shell cat version)
all: src.zip test.zip
	git tag -a Solution_$(VERSION) -m "Praktomat Solution $(VERSION)."
	echo $$(($(VERSION)+1))>version

test.zip: $(TEST)
	cd test && zip -r ../Solution$(VERSION)_test.zip $(TEST:test/%=%)

src.zip: $(SRC) README.txt
	cd src && zip -r ../Solution$(VERSION)_src.zip $(SRC:src/%=%)
	zip Solution$(VERSION)_src.zip README.txt
