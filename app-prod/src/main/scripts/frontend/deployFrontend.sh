#!/usr/bin/env bash
set -e

# === Konfiguration ===
SERVER_USER="ralf"
SERVER_HOST="168.119.63.209"              # oder www.crmupload.de
TARGET_DIR="/opt/websites/crmupload"
DIST_DIR="/home/ralf/IdeaProjects/crmupload-frontend/dist/crmupload-frontend"     # z.B. dist/crmupload-frontend

echo ">>> 2) Inhalte des Zielordners auf dem Server löschen..."
ssh ${SERVER_USER}@${SERVER_HOST} "rm -rf ${TARGET_DIR}/*"

echo ">>> 3) Neue Build-Dateien übertragen..."
rsync -avz ${DIST_DIR}/ ${SERVER_USER}@${SERVER_HOST}:${TARGET_DIR}/

echo ">>> Deployment erfolgreich abgeschlossen."
