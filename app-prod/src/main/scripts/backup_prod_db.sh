#!/usr/bin/env bash
set -euo pipefail

# ------------------------------------------------------------
# Backup Postgres from docker container (plain SQL, restore-fähig)
# anschließend TAR.GZ von .sql + .log
# ------------------------------------------------------------

CONTAINER_NAME="crmupload-db"
DB_NAME="crmupload-appdb"
DB_USER="appuser"

BACKUP_DIR="/opt/crmupload-deploy/backup"
TS="$(date +%Y%m%d_%H%M%S)"

SQL_FILE="${BACKUP_DIR}/Backup_${TS}.sql"
TMP_FILE="${BACKUP_DIR}/.Backup_${TS}.sql.tmp"
LOG_FILE="${BACKUP_DIR}/Backup_${TS}.log"
TAR_FILE="${BACKUP_DIR}/Backup_${TS}.tar.gz"

mkdir -p "$BACKUP_DIR"

echo "[$(date -Is)] Starting backup: ${SQL_FILE}" | tee -a "$LOG_FILE"

# Check: Docker verfügbar?
command -v docker >/dev/null 2>&1 || { echo "docker not found" | tee -a "$LOG_FILE"; exit 1; }

# Check: Container läuft?
if ! docker ps --format '{{.Names}}' | grep -qx "$CONTAINER_NAME"; then
  echo "Container not running: $CONTAINER_NAME" | tee -a "$LOG_FILE"
  exit 1
fi

# DB readiness check
echo "[$(date -Is)] Checking database readiness..." | tee -a "$LOG_FILE"
docker exec "$CONTAINER_NAME" pg_isready -U "$DB_USER" -d "$DB_NAME" >/dev/null

# pg_dump
echo "[$(date -Is)] Running pg_dump..." | tee -a "$LOG_FILE"
docker exec -i "$CONTAINER_NAME" pg_dump \
  -U "$DB_USER" \
  -d "$DB_NAME" \
  --format=p \
  --clean --if-exists \
  --no-owner --no-privileges \
  > "$TMP_FILE"

# Sanity check
if [[ ! -s "$TMP_FILE" ]]; then
  echo "Backup failed: output file is empty" | tee -a "$LOG_FILE"
  rm -f "$TMP_FILE"
  exit 1
fi

# Finalisieren
mv -f "$TMP_FILE" "$SQL_FILE"
chmod 600 "$SQL_FILE" || true

echo "[$(date -Is)] Backup OK: ${SQL_FILE}" | tee -a "$LOG_FILE"
echo "Size: $(du -h "$SQL_FILE" | awk '{print $1}')" | tee -a "$LOG_FILE"

# ------------------------------------------------------------
# TAR.GZ: SQL + LOG
# ------------------------------------------------------------
echo "[$(date -Is)] Creating TAR.GZ archive: ${TAR_FILE}" | tee -a "$LOG_FILE"

# -C sorgt dafür, dass im Archiv keine absoluten Pfade stehen
tar -czf "$TAR_FILE" -C "$BACKUP_DIR" \
  "$(basename "$SQL_FILE")" \
  "$(basename "$LOG_FILE")"

chmod 600 "$TAR_FILE" || true

# Originaldateien löschen
rm -f "$SQL_FILE" "$LOG_FILE"

# Info-Datei (optional)
INFO_FILE="${TAR_FILE}.info"
{
  echo "[$(date -Is)] TAR.GZ created successfully: $TAR_FILE"
  echo "TAR Size: $(du -h "$TAR_FILE" | awk '{print $1}')"
  echo "Contains:"
  tar -tzf "$TAR_FILE"
} > "$INFO_FILE"

chmod 600 "$INFO_FILE" || true
