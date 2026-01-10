# Verbinden auf Prod

ssh 168.119.63.209

# Installation auf dem Prod-Server

## Docker Netzwerk

`
docker network create crmuploadnet
`

## Registry

ghcr.io/<github-user>/crmupload-web:prod
ghcr.io/<github-user>/crmupload-worker:prod

## Docker Images bauen

### App-Web

`
docker build -t ghcr.io/<github-user>/crmupload-web:prod -f Dockerfile.web .
docker push ghcr.io/<github-user>/crmupload-web:prod
`

### App-Worker

`
docker build -t ghcr.io/<github-user>/crmupload-worker:prod -f Dockerfile.worker .
docker push ghcr.io/<github-user>/crmupload-worker:prod
`

## Dateien synchronisieren

`
cd /home/ralf/IdeaProjects/crmupload/ || exit
rsync -avz ./db-init/ ralf@server:/opt/crmupload-deploy/db-init/
rsync -avz ./app-prod/docker-compose.prod.yml ralf@server:/opt/crmupload-deploy/docker-compose.prod.yml
rsync -avz ./app-prod/src/main/scripts/deploy-on-server.sh ralf@server:/opt/crmupload-deploy/deploy.sh
`

## Vorbereitung auf dem Prod-Server

`
sudo usermod -aG docker ralf
`

`
sudo mkdir -p /opt/crmupload-deploy
sudo mkdir -p /opt/crmupload-deploy/app-db-data
sudo mkdir -p /opt/crmupload-deploy/tmp
sudo chown -R ralf:ralf /opt/crmupload-deploy
`

## Docker Deployment auf dem Prod-Server ausführen

`
ssh ralf@server "cd /opt/crmupload-deploy && ./deploy.sh"
`

# Postgres

## Auf dem Prod-Server

`
sudo ufw allow from 10.10.0.0/24 to any port 5436
`

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

# Docker

```
docker ps --format "table {{.ID}}\t{{.Image}}\t{{.RunningFor}}\t{{.Status}}\t{{.Names}}"
```

# Copy to server

```
scp ./backup_prod_db.sh ralf@10.10.0.1:/opt/crmupload-deploy/backup
```
