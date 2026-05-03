#!/usr/bin/env sh

##############################################################################
# Gradle start up script for UN*X
# Generado para Gradle 8.6 — FileExplorer project
#
# NOTA: Este script requiere gradle/wrapper/gradle-wrapper.jar para funcionar
# desde la terminal local. Si no tienes el jar, genera el wrapper con:
#   gradle wrapper --gradle-version 8.6
# (requiere tener Gradle instalado localmente)
#
# En GitHub Actions, el workflow usa "gradle" directamente sin necesitar este archivo.
##############################################################################

# Attempt to set APP_HOME
PRG="$0"
while [ -h "$PRG" ] ; do
    ls=$(ls -ld "$PRG")
    link=$(expr "$ls" : '.*-> \(.*\)$')
    if expr "$link" : '/.*' > /dev/null; then
        PRG="$link"
    else
        PRG=$(dirname "$PRG")/"$link"
    fi
done
SAVED="$(pwd)"
cd "$(dirname "$PRG")/" >/dev/null
APP_HOME="$(pwd -P)"
cd "$SAVED" >/dev/null

APP_NAME="Gradle"
APP_BASE_NAME=$(basename "$0")

DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'

MAX_FD="maximum"
warn () { echo "$*"; }
die () { echo; echo "$*"; echo; exit 1; }

# OS specific support (must be 'true' or 'false').
cygwin=false
msys=false
darwin=false
nonstop=false
case "$(uname)" in
  CYGWIN* ) cygwin=true ;;
  Darwin* ) darwin=true ;;
  MINGW*  ) msys=true   ;;
  NONSTOP*) nonstop=true;;
esac

CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

if [ ! -f "$CLASSPATH" ]; then
    echo ""
    echo "ERROR: gradle-wrapper.jar no encontrado en gradle/wrapper/"
    echo ""
    echo "Para generarlo localmente, ejecuta una sola vez:"
    echo "  gradle wrapper --gradle-version 8.6"
    echo ""
    echo "Alternativamente, usa GitHub Actions para compilar (no necesita el jar)."
    echo ""
    exit 1
fi

# Determine the Java command to use to start the JVM.
if [ -n "$JAVA_HOME" ]; then
    if [ -x "$JAVA_HOME/jre/sh/java" ]; then
        JAVACMD="$JAVA_HOME/jre/sh/java"
    else
        JAVACMD="$JAVA_HOME/bin/java"
    fi
    if [ ! -x "$JAVACMD" ]; then
        die "ERROR: JAVA_HOME está definido como \"$JAVA_HOME\", pero no se encontró el ejecutable."
    fi
else
    JAVACMD="java"
    java -version >/dev/null 2>&1 || die "ERROR: JAVA_HOME no está definido y 'java' no está en PATH."
fi

# Collect all arguments for the java command.
set -- \
    -classpath "$CLASSPATH" \
    org.gradle.wrapper.GradleWrapperMain \
    "$@"

exec "$JAVACMD" $DEFAULT_JVM_OPTS $JAVA_OPTS $GRADLE_OPTS "$@"
