#!/bin/bash

adb shell monkey -p com.android.gallery3d --throttle 100 -s 160 --monitor-native-crashes -v 99999
