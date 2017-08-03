#!/bin/bash
sudo modprobe bcm2835-v4l2
java -classpath bin::$NXJ_HOME/lib/pc/*:$NXJ_HOME/lib/pc/3rdparty/*:/home/pi/opencv-3.1.0/build2/bin/opencv-310.jar:$SLF4J_HOME/* -Djava.library.path=/home/pi/opencv-3.1.0/build2/lib -Djava.util.logging.config.file=./logging.properties edu.lejos.RemoteScan