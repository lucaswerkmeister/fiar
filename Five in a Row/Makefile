SRC := $(shell find src -name *.java)
TEST := $(shell find test -name *.java)
VERSION := $(shell cat version)
CHANGES := $(shell git diff HEAD src test README.txt)
MESSAGE := Praktomat Solution $(VERSION).
all: src.zip test.zip
	git config user.name "Lucas Werkmeister"
	git config user.email "mail@lucaswerkmeister.de"
ifneq ($(strip $(CHANGES)),)
	git add $(SRC) $(TEST) README.txt
	git commit -m "Committing uncommitted changes for $(MESSAGE)"
endif
	git tag -a Solution_$(VERSION) -m "$(MESSAGE)"
	echo $$(($(VERSION)+1))>version
	git commit -a -m "Increased version number."

test.zip: $(TEST)
	cd test && zip -r ../Solution$(VERSION)_test.zip $(TEST:test/%=%)

src.zip: $(SRC) README.txt
	cd src && zip -r ../Solution$(VERSION)_src.zip $(SRC:src/%=%)
	zip Solution$(VERSION)_src.zip README.txt
