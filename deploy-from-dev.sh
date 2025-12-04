#!/usr/bin/env bash
set -e

# Verzeichnis, in dem das Skript liegt (→ Projekt-Root)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "${SCRIPT_DIR}" || exit

echo ">>> Projekt-Root: $(pwd)"

GHCR_USER="${GHCR_USER:-otrturn}"          # nutzt Env oder fällt auf 'otrturn' zurück
SERVER="ralf@crmupload.de"
REMOTE_DIR="/opt/crmupload-deploy"

echo ">>> Build crmupload-web (Maven)"
mvn -pl app-web -am clean package -DskipTests

echo ">>> Build crmupload-worker (Maven)"
mvn -pl app-worker -am clean package -DskipTests

echo ">>> Docker build web..."
docker build -t ghcr.io/${GHCR_USER}/crmupload-web:prod -f app-web/Dockerfile.web app-web/

echo ">>> Docker build worker..."
docker build -t ghcr.io/${GHCR_USER}/crmupload-worker:prod -f app-worker/Dockerfile.worker app-worker/

echo ">>> Docker login GHCR"
echo "${GHCR_PAT}" | docker login ghcr.io -u "${GHCR_USER}" --password-stdin

echo ">>> Docker push web..."
docker push ghcr.io/${GHCR_USER}/crmupload-web:prod

echo ">>> Docker push worker..."
docker push ghcr.io/${GHCR_USER}/crmupload-worker:prod

echo ">>> Rsync deployment files..."
rsync -avz ./db-init/                         "${SERVER}:${REMOTE_DIR}/db-init/"
rsync -avz ./app-prod/docker-compose.prod.yml "${SERVER}:${REMOTE_DIR}/docker-compose.prod.yml"
rsync -avz ./app-prod/src/main/scripts/deploy-on-server.sh \
           "${SERVER}:${REMOTE_DIR}/deploy.sh"

echo ">>> Remote deploy..."
ssh "${SERVER}" "cd ${REMOTE_DIR} && chmod +x deploy.sh && ./deploy.sh"

echo ">>> Deployment completed successfully!"
