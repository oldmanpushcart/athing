#!/bin/bash
mvn -f ../pom.xml clean package '-Dmaven.test.skip=true'
