#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "==> Build produzione Vaadin: my-app"
mvn clean package -Pproduction -DskipTests

JAR_PATH="$SCRIPT_DIR/target/my-app-0.0.1-SNAPSHOT.jar"

if [ -f "$JAR_PATH" ]; then
    echo ""
    echo "==> BUILD COMPLETATO"
    echo "    JAR: $JAR_PATH"
    echo ""
    echo "    Per avviare:"
    echo "    java -jar $JAR_PATH"
else
    echo "ERRORE: JAR non trovato in $JAR_PATH" >&2
    exit 1
fi
