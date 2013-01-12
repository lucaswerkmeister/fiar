fiar
====

Repository for the Five in a Row minigame.

Building
===
If a new version shall be uploaded to the Praktomat, the preferred way of doing so is to run make in /Five in a Row.
make will automatically
* commit changes in the src/ and test/ folders, as well as changes to README.txt
* tag the latest commit as the praktomat solution
* create the zip files
* increase the version numer
* commit the increased version number.

This makes it very easy to reobtain the zip files that were uploaded to the Praktomat:
Just switch/checkout to the according tag (creating a new branch in the process) and run make again.
Since you are in a new branch, make's commits won't break anything.
