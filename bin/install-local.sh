#!/bin/bash

projects[i++]="com.github.athingx.athing.aliyun:athing-aliyun-thing"
projects[i++]="com.github.athingx.athing.aliyun:athing-aliyun-platform"

mvn clean install \
  -f ../pom.xml \
  -pl "$(printf "%s," "${projects[@]}")" -am
