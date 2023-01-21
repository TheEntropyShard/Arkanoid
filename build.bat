@echo off

cd src
javac *.java
jar cfe ..\Arkanoid.jar Arkanoid -C ..\src\ *.class -C ..\resources .
del *.class
cd ..\