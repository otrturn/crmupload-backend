#!/bin/bash

# Timestamp für Dateinamen (ohne Leerzeichen/Doppelpunkte)
timestamp=$(date +"%Y-%m-%d_%H-%M-%S")
file="${timestamp}-Billing.csv"

DB_HOST="localhost"
DB_PORT="5436"
DB_NAME="crmupload-appdb"
DB_USER="appuser"

echo "Exportiere nach Datei: ${file}"

# WICHTIG: Ausgabe von psql wird in die Datei ${file} umgeleitet
psql -h "${DB_HOST}" -p "${DB_PORT}" -U "${DB_USER}" -d "${DB_NAME}" > "${file}" <<'EOF'
COPY (
    WITH updated_consumer AS (
        UPDATE app.consumer c
        SET submitted_to_billing = now()
        WHERE c.submitted_to_billing IS NULL
          AND EXISTS (
              SELECT 1
              FROM app.consumer_upload cu
              WHERE cu.consumer_id = c.consumer_id
                AND cu.status = 'done'
          )
        RETURNING
            c.consumer_id,
            c.firstname,
            c.lastname,
            c.company_name,
            c.email_address,
            c.phone_number,
            c.adrline1,
            c.adrline2,
            c.postalcode,
            c.city,
            c.country
    )
    SELECT
        uc.firstname,
        uc.lastname,
        uc.company_name,
        uc.email_address,
        uc.phone_number,
        uc.adrline1,
        uc.adrline2,
        uc.postalcode,
        uc.city,
        uc.country,
        cu.source_system,
        cu.crm_system
    FROM updated_consumer uc
    JOIN app.consumer_upload cu
      ON cu.consumer_id = uc.consumer_id
    WHERE cu.status = 'done'
) TO STDOUT WITH (FORMAT csv, HEADER true);
EOF

echo "Fertig. Datei gespeichert als: ${file}"
