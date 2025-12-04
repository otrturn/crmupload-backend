#!/usr/bin/env bash
set -e

COMPOSE_FILE="docker-compose.prod.yml"

echo ">>> Pull latest images..."
docker compose -f ${COMPOSE_FILE} pull

echo ">>> Start/Update containers..."
docker compose -f ${COMPOSE_FILE} up -d

echo ">>> Remove unused images..."
docker image prune -f

echo ">>> Done."
