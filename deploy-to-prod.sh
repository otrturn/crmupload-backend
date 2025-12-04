#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "${SCRIPT_DIR}" || exit

# Gemeinsame Variablen laden
source ./deploy-env.sh

echo ">>> Deploy to server: ${DEPLOY_SERVER}"

rsync -avz ./db-init/ "${DEPLOY_SERVER}:${REMOTE_DIR}/db-init/"
rsync -avz ./app-prod/${DEPLOY_COMPOSE_FILE} "${DEPLOY_SERVER}:${REMOTE_DIR}/${DEPLOY_COMPOSE_FILE}"
rsync -avz ./app-prod/src/main/scripts/deploy-on-server.sh \
  "${DEPLOY_SERVER}:${REMOTE_DIR}/deploy.sh"

ssh "${DEPLOY_SERVER}" "cd ${REMOTE_DIR} && chmod +x deploy.sh && ./deploy.sh"
