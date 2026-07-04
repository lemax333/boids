#!/bin/bash

rm -rf build
mkdir -p build

javac -sourcepath ./src/ -d ./build ./src/com/boids/Main.java
