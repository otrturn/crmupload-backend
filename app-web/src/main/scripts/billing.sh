#!/bin/bash

# Timestamp für Dateinamen (ohne Leerzeichen/Doppelpunkte)
timestamp=$(date +"%Y-%m-%d_%H-%M-%S")
file="${timestamp}-Billing.csv"

DB_HOST="localhost"
DB_PORT="5436"
DB_NAME="crmupload-appdb"
DB_USER="appuser"

echo "Exportiere nach Datei: ${file}"

psql -h "${DB_HOST}" -p "${DB_PORT}" -U "${DB_USER}" -d "${DB_NAME}" <<EOF
\set ON_ERROR_STOP on
\copy (SELECT * FROM app.export_billing()) TO '${file}' WITH (FORMAT csv, HEADER true)
EOF

echo "Fertig. Datei gespeichert als: ${file}"
