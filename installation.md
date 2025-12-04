# Verbinden auf Prod

ssh 168.119.63.209

# VPN

`
cat server_private.key
`
GNcUSKOsaowVxObvP0l6YsTOGOxFOyC1EonvClVhulU=

`
cat server_public.key
`
CzNlQVoGX2frHqkeZNCUMQkA2lvHLiOrG0m5JBY8zU0=

`
cat client_private.key
`
kBv3eytHAcYMTQvjO3m3SR/0XGhLDA6R/jtY1jvZx24=

`
cat client_public.key
`
GNcR7Eu4ZKXsgAUvW8MFTiri14J8svQGnFt31FWrOFo=

## Wireguard starten

`
sudo wg-quick up wg-client
`

oder

`
sudo wg-quick up ./wg-client.conf
`

## Wireguard starten

`
sudo wg-quick down wg-client
`

oder

`
sudo wg-quick down ./wg-client.conf
`

## /etc
`
sudo cp wg-client.conf /etc/wireguard/wg-client.conf
sudo chmod 600 /etc/wireguard/wg-client.conf
`

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