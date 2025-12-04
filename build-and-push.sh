#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "${SCRIPT_DIR}" || exit

# Gemeinsame Variablen laden
source ./deploy-env.sh

echo ">>> Build using GHCR_USER=${GHCR_USER}"

# Maven build + Docker buildx wie zuvor ...
