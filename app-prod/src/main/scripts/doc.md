# Backup

```
scp ./backup_prod_db.sh ralf@10.10.0.1:/opt/crmupload-deploy/backup
```

# VPN
```
sudo systemctl enable wg-quick@wg-client
sudo systemctl start wg-quick@wg-client
systemctl status wg-quick@wg-client
```

# PostgreSQL Konfiguration
## pg_hba.conf
```
host  crmupload-appdb  appuser  10.10.0.2/32  scram-sha-256
```

Dann auf dem Prod-Rechner

```
docker exec -it crmupload-db \
  psql -U appuser -d crmupload-appdb \
  -c "SELECT pg_reload_conf();"
```

Dann auf dem Dev-Rechner

```
psql "host=10.10.0.1 port=5436 dbname=crmupload-appdb user=appuser"
```
