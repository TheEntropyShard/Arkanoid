#!/bin/bash

cd src
javac *.java
jar cfe ../Arkanoid.jar Arkanoid -C ../src/ *.class -C ../resources .
rm *.class
cd ../