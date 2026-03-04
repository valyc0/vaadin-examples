#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

IMAGE_NAME="my-app"
IMAGE_TAG="0.0.1-SNAPSHOT"

echo "==> Build JAR di produzione..."
mvn clean package -Pproduction -DskipTests

echo ""
echo "==> Creazione immagine Docker: ${IMAGE_NAME}:${IMAGE_TAG} (via Jib)..."
mvn jib:dockerBuild -Pproduction -DskipTests

REGISTRY="localhost:5000"

echo ""
echo "==> Push immagini su registry ${REGISTRY}..."
docker tag "${IMAGE_NAME}:latest" "${REGISTRY}/${IMAGE_NAME}:latest"
docker tag "${IMAGE_NAME}:${IMAGE_TAG}" "${REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}"
docker push "${REGISTRY}/${IMAGE_NAME}:latest"
docker push "${REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}"

echo ""
echo "==> COMPLETATO"
echo "    Locale:   ${IMAGE_NAME}:latest"
echo "    Registry: ${REGISTRY}/${IMAGE_NAME}:latest"
echo "    Registry: ${REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}"
echo ""
echo "    Per avviare il container:"
echo "    docker run -p 8080:8080 ${IMAGE_NAME}:latest"
