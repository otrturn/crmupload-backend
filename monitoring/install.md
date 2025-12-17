# Verzeichnisse

```
mkdir -p ./monitoring
mkdir -p ./monitoring/grafana/data
mkdir -p ./monitoring/loki/data
mkdir -p ./monitoring/promtail/data
sudo chmod -R 777 monitoring/loki/data
sudo chmod -R 777 monitoring/promtail/data
sudo chmod -R 777 monitoring/grafana/data
```

# Betrieb

```
docker compose -f docker.compose.monitoring.dev.yml down
docker compose -f docker.compose.monitoring.dev.yml up -d
```
