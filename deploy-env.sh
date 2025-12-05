#!/usr/bin/env bash

# ------------------------------------------------------------
# Gemeinsame Deployment-Konfiguration
# ------------------------------------------------------------

# GitHub Container Registry User
export GHCR_USER="otrturn"

# PAT wird weiterhin aus ~/.bashrc bezogen (nicht hier speichern!)
# export GHCR_PAT="..."   # → niemals PAT im Repo speichern!

# Produktivserver
export DEPLOY_SERVER="ralf@crmupload.de"

# Zielverzeichnis für Deployment
export REMOTE_DIR="/opt/crmupload-deploy"

# Optional: Netzwerkname für Compose
export DEPLOY_NETWORK="crmuploadnet"

# Optional: Komponierte Datei für Prod
export DEPLOY_COMPOSE_FILE="docker-compose.prod.yml"
