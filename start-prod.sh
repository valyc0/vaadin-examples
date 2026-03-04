#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
JAR_PATH="$SCRIPT_DIR/target/my-app-0.0.1-SNAPSHOT.jar"

if [ ! -f "$JAR_PATH" ]; then
    echo "ERRORE: JAR non trovato in $JAR_PATH"
    echo "Esegui prima ./build-prod.sh"
    exit 1
fi

echo "==> Avvio my-app in modalità produzione..."
echo "    JAR: $JAR_PATH"
echo "    URL: http://localhost:8080"
echo ""

java -jar "$JAR_PATH"
