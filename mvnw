#!/bin/sh
# ----------------------------------------------------------------------------
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.
# ----------------------------------------------------------------------------
set -e

SCRIPT_DIR=$(dirname "$(readlink -f "$0" 2>/dev/null || realpath "$0" 2>/dev/null || echo "$0")")
MAVEN_WRAPPER_PROPERTIES="$SCRIPT_DIR/.mvn/wrapper/maven-wrapper.properties"
WRAPPER_JAR="$HOME/.m2/wrapper/maven-wrapper.jar"

if [ ! -e "$WRAPPER_JAR" ]; then
    mkdir -p "$(dirname "$WRAPPER_JAR")"
    WRAPPER_URL=$(grep wrapperUrl "$MAVEN_WRAPPER_PROPERTIES" | cut -d= -f2-)
    curl -o "$WRAPPER_JAR" -fsSL "$WRAPPER_URL" || wget -O "$WRAPPER_JAR" "$WRAPPER_URL"
fi

exec java -classpath "$WRAPPER_JAR" org.apache.maven.wrapper.MavenWrapperMain "$@" \
    "-Dmaven.multiModuleProjectDirectory=$SCRIPT_DIR"
