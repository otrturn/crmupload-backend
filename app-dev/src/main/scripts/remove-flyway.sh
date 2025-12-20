#!/bin/bash
set -e

# Alle Container stoppen, die auf dem Image "crmupload-flyway" basieren
echo "🛑 Stoppe Container mit Image 'crmupload-web'..."
docker ps -a --filter ancestor=crmupload-web --format "{{.ID}}" | while read cid; do
	if [ -n "$cid" ]; then
		echo "→ Stoppe Container $cid"
		docker stop "$cid" >/dev/null 2>&1 || true
		docker rm "$cid" >/dev/null 2>&1 || true
	fi
done

# Alle Images löschen, die "crmupload-flyway" enthalten
echo "🧹 Entferne Images mit Namen 'crmupload-web'..."
docker images --format "{{.Repository}}:{{.Tag}} {{.ID}}" | grep 'crmupload-flyway:' | while read repo id; do
	if [ -n "$id" ]; then
		echo "→ Entferne Image $id ($repo)"
		docker rmi -f "$id"
	fi
done

echo "✅ Fertig: alle 'crmupload-web'-Images und -Container wurden entfernt."

