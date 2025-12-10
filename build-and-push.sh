#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "${SCRIPT_DIR}" || exit

echo ">>> Projekt-Root: $(pwd)"

# Gemeinsame Variablen laden
source ./deploy-env.sh

echo ">>> Build using GHCR_USER=${GHCR_USER}"

# -------------------------------------------------------------------
# Sicherheitscheck: GHCR_PAT muss gesetzt sein
# -------------------------------------------------------------------
if [[ -z "${GHCR_PAT:-}" ]]; then
  echo "ERROR: GHCR_PAT ist nicht gesetzt. Bitte in der Shell oder ~/.bashrc exportieren:"
  echo "  export GHCR_PAT=ghp_xxx"
  exit 1
fi

# -------------------------------------------------------------------
# Maven-Builds
# -------------------------------------------------------------------
echo ">>> Build crmupload-web (Maven)"
mvn -pl app-web -am clean package -DskipTests

echo ">>> Build crmupload-worker (Maven)"
mvn -pl app-worker-upload -am clean package -DskipTests

# -------------------------------------------------------------------
# Docker Login bei GHCR
# -------------------------------------------------------------------
echo ">>> Docker login GHCR"
echo "${GHCR_PAT}" | docker login ghcr.io -u "${GHCR_USER}" --password-stdin

# -------------------------------------------------------------------
# Multi-Arch Build & Push (amd64 + arm64/v8)
# -------------------------------------------------------------------
echo ">>> Docker build+push web (multi-arch)..."
docker buildx build \
  --platform linux/amd64,linux/arm64/v8 \
  -t ghcr.io/${GHCR_USER}/crmupload-web:prod \
  -f app-web/Dockerfile \
  app-web/ \
  --push

echo ">>> Docker build+push worker (multi-arch)..."
docker buildx build \
  --platform linux/amd64,linux/arm64/v8 \
  -t ghcr.io/${GHCR_USER}/crmupload-worker-upload:prod \
  -f app-worker-upload/Dockerfile \
  app-worker-upload/ \
  --push

