#!/usr/bin/env bash

set -euo pipefail

BASEDIR="$(cd "$(dirname "$0")" && pwd -P)"
WRAPPER_JAR="$BASEDIR/.mvn/wrapper/maven-wrapper.jar"
WRAPPER_PROPERTIES="$BASEDIR/.mvn/wrapper/maven-wrapper.properties"
WRAPPER_MAIN=org.apache.maven.wrapper.MavenWrapperMain

# Read wrapperUrl from properties file
WRAPPER_URL=""
if [[ -r "$WRAPPER_PROPERTIES" ]]; then
  while IFS='=' read -r key value; do
    case "$key" in
      wrapperUrl)
        WRAPPER_URL="${value%$'\r'}"
        ;;
    esac
  done < "$WRAPPER_PROPERTIES"
fi

if [[ -z "$WRAPPER_URL" ]]; then
  echo "ERROR: wrapperUrl not set in $WRAPPER_PROPERTIES" >&2
  exit 1
fi

if [[ ! -f "$WRAPPER_JAR" ]]; then
  mkdir -p "$(dirname "$WRAPPER_JAR")"
  if command -v curl >/dev/null 2>&1; then
    curl -fsSL -o "$WRAPPER_JAR" "$WRAPPER_URL"
  elif command -v wget >/dev/null 2>&1; then
    wget -q -O "$WRAPPER_JAR" "$WRAPPER_URL"
  else
    echo "ERROR: Neither curl nor wget is available to download the Maven wrapper." >&2
    exit 1
  fi
fi

if [[ -z "${JAVA_HOME:-}" ]]; then
  JAVA_EXE="$(command -v java || true)"
  if [[ -z "$JAVA_EXE" ]]; then
    echo "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH." >&2
    exit 1
  fi
else
  JAVA_EXE="$JAVA_HOME/bin/java"
fi

CMD=("$JAVA_EXE")
if [[ -n "${MAVEN_OPTS:-}" ]]; then
  # shellcheck disable=SC2206
  CMD+=($MAVEN_OPTS)
fi
CMD+=(-Dmaven.multiModuleProjectDirectory="$BASEDIR" -classpath "$WRAPPER_JAR" "$WRAPPER_MAIN")

exec "${CMD[@]}" "$@"
