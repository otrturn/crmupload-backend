#!/usr/bin/env bash
set -euo pipefail

REMOTE_HOST="10.10.0.1"
REMOTE_USER="ralf"
REMOTE_DIR="/opt/crmupload-deploy/backup"
LOCAL_DIR="/home/ralf/backup_prod"

mkdir -p "$LOCAL_DIR"

# Nur Dateien holen, die mit "Backup_" beginnen und lokal noch nicht existieren
rsync -av --ignore-existing \
  "${REMOTE_USER}@${REMOTE_HOST}:${REMOTE_DIR}/Backup_*" \
  "${LOCAL_DIR}/"
