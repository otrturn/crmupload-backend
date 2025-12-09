#!/bin/bash

# Timestamp für Dateinamen (ohne Leerzeichen/Doppelpunkte)
timestamp=$(date +"%Y-%m-%d_%H-%M-%S")
file="${timestamp}-Billing.csv"

DB_HOST="localhost"
DB_PORT="5436"
DB_NAME="crmupload-appdb"
DB_USER="appuser"

echo "Exportiere nach Datei: ${file}"

query="
WITH updated AS (
    UPDATE app.customer_billing b
    SET submitted_to_billing = now()
    WHERE b.submitted_to_billing IS NULL
      AND b.product = 'crm-upload'
      AND b.status = 'new-subscription'
    RETURNING
        b.customer_id,
        b.billing_meta,
        b.start_of_subscription
)
SELECT
    c.firstname,
    c.lastname,
    c.company_name,
    c.email_address,
    c.phone_number,
    c.adrline1,
    c.adrline2,
    c.postalcode,
    c.city,
    c.country,
    updated.billing_meta->>'crm_customer_id' AS crm_customer_id,
    updated.billing_meta->>'source_system'   AS source_system,
    updated.billing_meta->>'crm_system'      AS crm_system,
    updated.billing_meta->>'crm_url'         AS crm_url,
    to_char(updated.start_of_subscription, 'DD.MM.YYYY') AS start_of_subscription
FROM updated
JOIN app.customer c ON c.customer_id = updated.customer_id
ORDER BY updated.customer_id ASC
"

psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" \
     -c "\copy ($query) TO '$file' WITH (FORMAT csv, HEADER true)"

echo "Fertig. Datei gespeichert als: ${file}"
