#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "${SCRIPT_DIR}" || exit 1

# Gemeinsame Variablen laden
source ./deploy-env.sh

# Fallbacks (falls nicht gesetzt)
DEPLOY_PORT="${DEPLOY_PORT:-22}"
REMOTE_DIR="${REMOTE_DIR:-/opt/crmupload-deploy}"

echo ">>> Deploy to server: ${DEPLOY_SERVER}"
echo ">>> DEPLOY_PORT: ${DEPLOY_PORT}"
echo ">>> REMOTE_DIR: ${REMOTE_DIR}"

echo ">>> Uploading deployment files ..."

RSYNC_SSH="ssh -p ${DEPLOY_PORT}"

# Zum Debuggen einmal explizit zeigen, was das Ziel ist:
#echo ">>> rsync target for db-init: ${DEPLOY_SERVER}:${REMOTE_DIR}/db-init/"

#rsync -avz -e "${RSYNC_SSH}" ./db-init/ \
#  "${DEPLOY_SERVER}:${REMOTE_DIR}/db-init/"

rsync -avz -e "${RSYNC_SSH}" ./${DEPLOY_COMPOSE_FILE} \
  "${DEPLOY_SERVER}:${REMOTE_DIR}/${DEPLOY_COMPOSE_FILE}"

rsync -avz -e "${RSYNC_SSH}" ./app-prod/src/main/scripts/deploy-on-server.sh \
  "${DEPLOY_SERVER}:${REMOTE_DIR}/deploy.sh"

echo ">>> Running deploy.sh on server ..."

ssh -p "${DEPLOY_PORT}" "${DEPLOY_SERVER}" "
  cd ${REMOTE_DIR} && chmod +x deploy.sh && ./deploy.sh
"

echo ">>> Deployment completed."
