#!/bin/bash
set -e

IMAGE_NAME="vaadin-dev-offline"
CONTAINER_NAME="vaadin-dev"

# ─────────────────────────────────────────────────────────────
# FASE 1: Preparazione offline (richiede internet, solo 1 volta)
# ─────────────────────────────────────────────────────────────

# 1a) Scarica tutte le dipendenze Maven nel .m2 locale
echo "==> Scarico dipendenze Maven in .m2/ ..."
mvn dependency:go-offline -U -Dmaven.repo.local=.m2

# 1b) Copia anche i jar non coperti da dependency:go-offline (es. test/runtime)
if [ -d "$HOME/.m2/repository" ]; then
  echo "==> Sincronizzo ~/.m2/repository → .m2/ ..."
  rsync -a --ignore-existing "$HOME/.m2/repository/" .m2/
fi

# 1c) Avvia spring-boot:run per far scaricare Node.js e generare il dev.bundle,
#     poi lo ferma automaticamente appena l'app è pronta
if [ ! -f "src/main/bundles/dev.bundle" ] || [ ! -d ".vaadin-cache/node" ]; then
  echo "==> Avvio temporaneo per scaricare Node.js e generare dev.bundle ..."
  mvn spring-boot:run -Dmaven.repo.local=.m2 &
  MVN_PID=$!
  echo "    (PID $MVN_PID) Aspetto che Vaadin finisca il setup frontend..."
  # Aspetta finché l'app risponde su :8080
  until curl -s --max-time 2 http://localhost:8080 > /dev/null 2>&1; do
    sleep 3
    # Se il processo è già morto, c'è un errore
    kill -0 $MVN_PID 2>/dev/null || { echo "ERRORE: spring-boot:run fallito"; exit 1; }
  done
  echo "==> App avviata, fermo il processo..."
  kill $MVN_PID
  wait $MVN_PID 2>/dev/null || true
else
  echo "==> Node.js e dev.bundle già presenti, skip."
fi

# ─────────────────────────────────────────────────────────────
# FASE 2: Build e avvio container offline
# ─────────────────────────────────────────────────────────────

mkdir -p "$(pwd)/src/main/bundles"

echo "==> Build immagine Docker..."
docker build -f Dockerfile.dev-offline -t "$IMAGE_NAME" .

if [ $? -ne 0 ]; then
  echo "ERRORE: build fallita."
  exit 1
fi

echo "==> Avvio container (senza internet)..."
docker run --rm \
  --name "$CONTAINER_NAME" \
  -p 8080:8080 \
  --dns 0.0.0.0 \
  -v "$(pwd)/src/main/bundles":/app/src/main/bundles \
  "$IMAGE_NAME"
