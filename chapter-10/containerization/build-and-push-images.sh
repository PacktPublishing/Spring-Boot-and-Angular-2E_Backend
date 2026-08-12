#!/bin/bash

set -euo pipefail

# Docker Hub namespace (override if needed: DOCKERHUB_USERNAME=yourname ./build-and-push-images.sh)
DOCKERHUB_USERNAME="${DOCKERHUB_USERNAME:-ansgohar}"
TAG="latest"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

SERVICES=(
  "eureka-server"
  "gateway-server"
  "inventory-ms"
  "user-ms"
)

echo "Using project root: ${PROJECT_ROOT}"
echo "Docker Hub namespace: ${DOCKERHUB_USERNAME}"
echo "Tag: ${TAG}"

if ! command -v docker >/dev/null 2>&1; then
  echo "Error: docker command not found. Install Docker first."
  exit 1
fi

# Verify Docker daemon is reachable
if ! docker info >/dev/null 2>&1; then
  echo "Error: Docker daemon is not running or not accessible."
  exit 1
fi

for service in "${SERVICES[@]}"; do
  service_path="${PROJECT_ROOT}/${service}"
  image_name="${DOCKERHUB_USERNAME}/${service}:${TAG}"

  if [ ! -d "${service_path}" ]; then
    echo "Error: service directory not found: ${service_path}"
    exit 1
  fi

  if [ ! -f "${service_path}/Containerfile" ]; then
    echo "Error: Containerfile not found in ${service_path}"
    exit 1
  fi

  echo ""
  echo "==> Building ${image_name}"
  docker build -f "${service_path}/Containerfile" -t "${image_name}" "${service_path}"

  echo "==> Pushing ${image_name}"
  docker push "${image_name}"
done

echo ""
echo "All images built and pushed successfully."
