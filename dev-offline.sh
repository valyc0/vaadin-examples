#!/bin/bash
# PREREQUISITO: esegui prima mvn spring-boot:run -Dmaven.repo.local=.m2
# per scaricare tutte le dipendenze e generare .vaadin-cache/node via ~/.vaadin

set -e

IMAGE_NAME="vaadin-dev-offline"
CONTAINER_NAME="vaadin-dev"

# 1) Copia Node.js da ~/.vaadin (scaricato da Vaadin al primo run) in .vaadin-cache/
echo "==> Copio .vaadin-cache da ~/.vaadin ..."
mkdir -p .vaadin-cache
rsync -a "$HOME/.vaadin/node" .vaadin-cache/

# 2) Build immagine Docker
echo "==> Build immagine Docker..."
docker build -f Dockerfile.dev-offline -t "$IMAGE_NAME" .

# 3) Avvio container senza internet
mkdir -p "$(pwd)/src/main/bundles"
echo "==> Avvio container (senza internet)..."
docker run --rm \
  --name "$CONTAINER_NAME" \
  -p 8080:8080 \
  --dns 0.0.0.0 \
  -v "$(pwd)/src/main/bundles":/app/src/main/bundles \
  "$IMAGE_NAME"
