#!/bin/bash
mvn -f ../pom.xml clean install '-Dmaven.test.skip=true'
