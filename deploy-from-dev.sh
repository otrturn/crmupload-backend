#!/usr/bin/env bash
set -e

# -------------------------------------------------------------------
# !!! NUR ZUR ERINNERUNG !!!
# -------------------------------------------------------------------

# Verzeichnis, in dem das Skript liegt (→ Projekt-Root)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "${SCRIPT_DIR}" || exit

echo ">>> Projekt-Root: $(pwd)"

GHCR_USER="${GHCR_USER:-otrturn}"          # nutzt Env oder fällt auf 'otrturn' zurück
SERVER="ralf@crmupload.de"
REMOTE_DIR="/opt/crmupload-deploy"

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
mvn -pl app-worker -am clean package -DskipTests

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
  -t ghcr.io/${GHCR_USER}/crmupload-worker:prod \
  -f app-worker/Dockerfile \
  app-worker/ \
  --push

# -------------------------------------------------------------------
# Deployment-Dateien auf den Server kopieren
# -------------------------------------------------------------------
echo ">>> Rsync deployment files..."
rsync -avz ./db-init/                         "${SERVER}:${REMOTE_DIR}/db-init/"
rsync -avz ./app-prod/docker-compose.prod.yml "${SERVER}:${REMOTE_DIR}/docker-compose.prod.yml"
rsync -avz ./app-prod/src/main/scripts/deploy-on-server.sh \
           "${SERVER}:${REMOTE_DIR}/deploy.sh"

# -------------------------------------------------------------------
# Remote-Deploy ausführen
# -------------------------------------------------------------------
echo ">>> Remote deploy..."
ssh "${SERVER}" "cd ${REMOTE_DIR} && chmod +x deploy.sh && ./deploy.sh"

echo ">>> Deployment completed successfully!"
