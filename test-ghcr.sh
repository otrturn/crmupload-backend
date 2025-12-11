#!/usr/bin/env bash
set -e

echo ">>> GHCR Test – Docker + Token prüfen"

# -------------------------------------------------------
# 1) Prüfen, ob docker installiert ist
# -------------------------------------------------------
if ! command -v docker >/dev/null 2>&1; then
  echo "ERROR: 'docker' wurde nicht gefunden. Bitte Docker installieren und PATH prüfen."
  exit 1
fi

# -------------------------------------------------------
# 2) GitHub-User ermitteln
#    - Entweder aus GHCR_USER
#    - oder als erstes Skriptargument
# -------------------------------------------------------
GHCR_USER="${GHCR_USER:-$1}"

if [ -z "$GHCR_USER" ]; then
  echo "ERROR: Kein GitHub-User angegeben."
  echo "Setze entweder die Umgebungsvariable GHCR_USER oder rufe das Skript so auf:"
  echo "  ./test-ghcr.sh dein-github-user"
  exit 1
fi

echo ">>> Verwende GitHub-User: $GHCR_USER"

# -------------------------------------------------------
# 3) Prüfen, ob GHCR_PAT gesetzt ist
# -------------------------------------------------------
if [ -z "$GHCR_PAT" ]; then
  echo "ERROR: Umgebungsvariable GHCR_PAT ist nicht gesetzt."
  echo
  echo "Bitte setze sie z.B. in ~/.bashrc:"
  echo '  export GHCR_PAT="ghp_dein_token_hier"'
  echo
  echo "Danach neues Terminal öffnen oder 'source ~/.bashrc' ausführen."
  exit 1
fi

echo ">>> GHCR_PAT ist gesetzt (Inhalt wird NICHT angezeigt)."

# -------------------------------------------------------
# 4) Login bei ghcr.io testen
# -------------------------------------------------------
echo ">>> Versuche Docker-Login bei ghcr.io ..."
if echo "$GHCR_PAT" | docker login ghcr.io -u "$GHCR_USER" --password-stdin; then
  echo ">>> Login Succeeded – GHCR-Token und User funktionieren."
else
  echo "ERROR: Docker-Login bei ghcr.io ist fehlgeschlagen."
  echo "Mögliche Ursachen:"
  echo "  - falscher GHCR_USER (muss exakt dein GitHub-Login sein)"
  echo "  - Token hat nicht die Scopes 'read:packages'/'write:packages'"
  echo "  - Token abgelaufen oder falsch kopiert"
  exit 1
fi

echo ">>> GHCR Test erfolgreich abgeschlossen."
