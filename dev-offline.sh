#!/bin/bash

# prima di eseguire questo script, assicurati di aver scaricato 
#le dipendenze con mvn spring-boot:run -Dmaven.repo.local=.m2

mvn clean compile -Dmaven.repo.local=.m2

IMAGE_NAME="vaadin-dev-offline"
CONTAINER_NAME="vaadin-dev"

echo "==> Build immagine Docker..."
docker build -f Dockerfile.dev-offline -t "$IMAGE_NAME" .

if [ $? -ne 0 ]; then
  echo "ERRORE: build fallita."
  exit 1
fi


# Crea la cartella bundles sul host se non esiste ancora
mkdir -p "$(pwd)/src/main/bundles"

echo "==> Avvio container..."
docker run --rm \
  --name "$CONTAINER_NAME" \
  -p 8080:8080 \
  -v "$(pwd)/src/main/bundles":/app/src/main/bundles \
  "$IMAGE_NAME"
