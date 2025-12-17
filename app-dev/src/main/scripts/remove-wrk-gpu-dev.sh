#!/bin/bash
set -e

# Alle Container stoppen, die auf dem Image "crmupload-worker-duplicate-check-gpu-dev" basieren
echo "🛑 Stoppe Container mit Image 'crmupload-worker-duplicate-check-gpu:dev'..."
docker ps -a --filter ancestor=crmupload-worker-duplicate-check-gpu:dev --format "{{.ID}}" | while read cid; do
	if [ -n "$cid" ]; then
		echo "→ Stoppe Container $cid"
		docker stop "$cid" >/dev/null 2>&1 || true
		docker rm "$cid" >/dev/null 2>&1 || true
	fi
done

# Alle Images löschen, die "crmupload-worker-duplicate-check-gpu-dev" enthalten

echo "🧹 Entferne Images mit Namen 'crmupload-worker-duplicate-check-gpu-dev'..."
docker images --format "{{.Repository}}:{{.Tag}} {{.ID}}" | grep '^crmupload-worker-duplicate-check-gpu:dev'  | while read repo id; do
	if [ -n "$id" ]; then
		echo "→ Entferne Image $id ($repo)"
		docker rmi -f "$id"
	fi
done

echo "✅ Fertig: alle 'crmupload-worker-duplicate-check-gpu'-Images und -Container wurden entfernt."

