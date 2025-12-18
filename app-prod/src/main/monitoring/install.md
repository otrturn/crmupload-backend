# Verzeichnisse

```
mkdir -p ./monitoring
mkdir -p ./monitoring/grafana/data
mkdir -p ./monitoring/loki/data
mkdir -p ./monitoring/grafana/provisioning/datasources
sudo chmod -R 777 ./monitoring/loki/data
sudo chmod -R 777 ./monitoring/grafana/data
sudo chmod -R 777 ./monitoring/grafana/provisioning/datasources

mkdir -p ./monitoring/promtail/positions
touch ./monitoring/promtail/positions/positions.yaml
chmod 644 ./monitoring/promtail/positions/positions.yaml
```

# Betrieb

```
docker compose -f docker.compose.monitoring.dev.yml down
docker compose -f docker.compose.monitoring.dev.yml up -d
```

# Prod

```
docker network create monitoring
```

# Reset
```
docker compose -f docker.compose.monitoring.dev.yml down

rm -rf monitoring/loki/data
rm -rf monitoring/promtail/positions
rm -rf monitoring/grafana/data

# !!! -> Verzeichnisse anlegen !!!

docker compose -f docker.compose.monitoring.dev.yml up -d
```

# Monitoring config kopieren
```
rsync -av --progress \
--exclude='data/' \
--exclude='positions/' \
monitoring/ \
ralf@10.10.0.1:/opt/crmupload-deploy/monitoring
```
