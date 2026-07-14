# Docker

## Quick Start

```sh
docker compose up -d
```

Starts PostgreSQL, Redis, RabbitMQ, Node-RED, Prometheus, Grafana, Loki, and exporters.

## Rebuilding Node-RED

After changing `services/node-red/Dockerfile`, `node-red-flows.json`, `entrypoint.sh`, `settings.js`, or files under `services/node-red/ui/`:

```sh
docker compose up -d --build node-red
```

To rebuild from scratch (no cache):

```sh
docker compose build --no-cache node-red
docker compose up -d node-red
```

## Deploying Flow Changes (without rebuild)

If only `node-red-flows.json` changed and the Node-RED image doesn't need rebuilding:

```sh
docker cp services/node-red/node-red-flows.json node-red:/default-flows.json
docker cp services/node-red/node-red-flows.json node-red:/data/flows.json
docker restart node-red
```

## Updating the UI


```sh
docker cp services/node-red/ui/index.html node-red:/data/ui/index.html
```

## Spring Boot

```sh
docker compose --profile spring up -d --build app
```

## Rebuilding Everything

```sh
docker compose --profile spring down
docker compose --profile spring up -d --build
```

## Logs

```sh
docker logs node-red -f
docker logs advisor-booking-system-app-1 -f
```
