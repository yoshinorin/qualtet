#!/bin/sh -xe

export QUALTET_BUILD_COMMIT_HASH=$(head -n 1 qualtet.build_hash)
exec java $JAVA_OPTS -jar /usr/opt/qualtet/qualtet-assembly.jar
