# Bookstore Backend Docker Compose User Guide

## Overview

This guide explains how to run the adjacent [docker-compose.yml](docker-compose.yml) file in this folder.

By default, the compose file downloads pre-built backend images from the author Docker Hub namespace:

- `ansgohar/eureka-server`
- `ansgohar/gateway-server`
- `ansgohar/inventory-service`
- `ansgohar/user-service`

The full stack includes infrastructure and backend services:

- PostgreSQL
- MongoDB
- Zipkin
- Keycloak
- Eureka Server
- Inventory Service
- User Service
- Gateway Server

## Prerequisites

- Docker Desktop (or Docker Engine + Compose plugin with `docker compose`)
- At least 6 GB RAM available to Docker
- Internet access to pull required images

Optional:

- Docker Hub login (only needed if your environment enforces auth/rate limits)
- Local source code if you want to rebuild images instead of pulling them

## What This Compose File Does

When you run compose from this folder, Docker will:

1. Pull infrastructure images (`postgres`, `mongo`, `zipkin`, `keycloak`).
2. Pull backend service images from author repositories (`ansgohar/*`).
3. Start all services with health checks and dependency ordering.
4. Attach everything to one shared bridge network.
5. Persist database state in named volumes.

## Services and Ports

| Service | Container Name | Port |
|---|---|---|
| Gateway | `bookstore-gateway-server` | `8080` |
| Eureka | `bookstore-eureka-server` | `8761` |
| Inventory | `bookstore-inventory-service` | `8081` |
| User | `bookstore-user-service` | `8082` |
| Keycloak | `bookstore-keycloak` | `8090` |
| PostgreSQL | `bookstore-postgres` | `5432` |
| MongoDB | `bookstore-mongo` | `27017` |
| Zipkin | `bookstore-zipkin` | `9411` |

## Quick Start (Pull Author Images and Run)

From repository root:

```bash
cd containerization
docker compose pull
docker compose up -d
docker compose ps
```

Open:

- Gateway: http://localhost:8080
- Eureka: http://localhost:8761
- Zipkin: http://localhost:9411
- Keycloak: http://localhost:8090

The `docker compose pull` command is the key step that downloads images from the author repository references in [docker-compose.yml](docker-compose.yml).

## Image Tag Selection

The compose file uses this pattern for backend services:

- `ansgohar/<service-name>:${IMAGE_TAG:-latest}`

Examples:

```bash
# Use latest tag (default)
docker compose pull

# Use an explicit tag
IMAGE_TAG=v0.0.1 docker compose pull
IMAGE_TAG=v0.0.1 docker compose up -d
```

The MongoDB image tag is overridable the same way, defaulting to `8.0`:

- `mongo:${MONGO_IMAGE_TAG:-8.0}`

```bash
# Required on Linux kernels 6.19-7.0.13, including Docker Desktop 4.87+.
# See "MongoDB exits immediately on startup" under Troubleshooting.
MONGO_IMAGE_TAG=7.0 docker compose up -d
```

## Build and Start Options

### Option A: Use author-published images (recommended)

```bash
docker compose pull
docker compose up -d
```

### Option B: Rebuild service images locally

Use this only if you changed source code and want local builds instead of pulling author images.

```bash
# From repository root, build each service image as needed
cd chapter-10/eureka-server && docker build -t ansgohar/eureka-server:latest -f Containerfile .
cd ../gateway-server && docker build -t ansgohar/gateway-server:latest -f Containerfile .
cd ../inventory-ms && docker build -t ansgohar/inventory-service:latest -f Containerfile .
cd ../user-ms && docker build -t ansgohar/user-service:latest -f Containerfile .

# Then run compose
cd ../../containerization
docker compose up -d
```

### Use a different image tag

`docker-compose.yml` supports `IMAGE_TAG` for author images.

```bash
IMAGE_TAG=v0.0.1 docker compose up -d
```

## Useful Runtime Commands

```bash
# Check service state and health
docker compose ps

# Tail logs for all services
docker compose logs -f --tail=200

# Tail logs for one service
docker compose logs -f --tail=200 gateway-server

# Restart one service
docker compose restart inventory-service

# Stop stack
docker compose down

# Stop and remove volumes (destructive for DB data)
docker compose down -v
```

## Troubleshooting

### 1) Port already in use

Symptom:

- Compose fails with port binding error for `8080`, `8761`, `8090`, `5432`, `27017`, or `9411`.

Checks:

```bash
lsof -nP -iTCP:8080 -sTCP:LISTEN
lsof -nP -iTCP:5432 -sTCP:LISTEN
```

Fix:

- Stop conflicting local service or container.
- Or change host-side port mappings in `docker-compose.yml`.

### 2) Keycloak realm import file missing

Symptom:

- Keycloak fails to start because `./Keycloak/bookstore-realm.json` cannot be mounted.

Fix:

- Confirm the file exists at `containerization/Keycloak/bookstore-realm.json`.
- If unavailable, remove the volume mount temporarily and start Keycloak without import.

### 3) Service stays unhealthy or restarts

Symptom:

- One or more services remain `unhealthy` or restart repeatedly.

Checks:

```bash
docker compose ps
docker compose logs --tail=200 eureka-server
docker compose logs --tail=200 inventory-service
docker compose logs --tail=200 user-service
docker compose logs --tail=200 gateway-server
```

Fix:

- Wait for dependencies (Keycloak/Eureka/databases) to become healthy.
- Restart dependent services once dependencies are stable:

```bash
docker compose restart inventory-service user-service gateway-server
```

### 4) Cannot pull author service images

Symptom:

- `pull access denied` for `ansgohar/...` images.

Fix options:

1. Login to Docker Hub and retry.

```bash
docker login
docker compose pull
```

2. Build local images with matching names and tags, then run compose.

3. Verify the tag exists. If not sure, retry with `latest`.

```bash
IMAGE_TAG=latest docker compose pull
```

### 5) Gateway starts but APIs fail

Symptom:

- Gateway responds but inventory/user endpoints fail.

Checks:

```bash
docker compose logs --tail=200 gateway-server
docker compose logs --tail=200 eureka-server
docker compose logs --tail=200 inventory-service
docker compose logs --tail=200 user-service
```

Fix:

- Ensure services are registered in Eureka.
- Ensure database containers are healthy.
- Restart gateway after dependent services are healthy:

```bash
docker compose restart gateway-server
```

### 6) MongoDB authentication errors

Symptom:

- `user-service` cannot authenticate to MongoDB.

Fix:

- Keep the URI aligned with compose env:

`mongodb://bookstore:bookstore123@mongodb:27017/userDB?authSource=admin`

- If credentials changed, recreate stack with fresh data volumes:

```bash
docker compose down -v
docker compose up -d
```

### 7) MongoDB exits immediately on startup (Linux kernel 6.19+)

Symptom:

- `bookstore-mongo` exits with code 1 straight away, and every service that
  depends on it fails with `dependency failed to start: container bookstore-mongo exited (1)`.
- `docker compose logs mongodb` shows:

```
MongoDB cannot start: Linux kernel versions 6.19 and newer has a known
incompatibility with this version of MongoDB.
See https://jira.mongodb.org/browse/SERVER-121912 for more information.
```

Cause:

- MongoDB 8.x aborts on startup on Linux kernels 6.19 through 7.0.13, because
  its bundled TCMalloc violates the kernel's `rseq` ABI. This is a hard check on
  the reported kernel version, so it fails before any configuration is read.
- **Docker Desktop 4.87+ ships VM kernel 7.0.12**, which falls inside that
  range. Earlier releases shipped 6.12.x, below the threshold, which is why the
  same compose file worked before upgrading Docker Desktop.
- Checking your kernel: `docker info --format '{{.KernelVersion}}'`.

Fix:

- Run MongoDB 7.0, which predates the affected TCMalloc:

```bash
MONGO_IMAGE_TAG=7.0 docker compose up -d
```

- Or persist it for your environment by adding this to a `.env` file next to
  `docker-compose.yml`:

```
MONGO_IMAGE_TAG=7.0
```

MongoDB 7.0 covers everything the bookstore `user-service` uses (plain Spring
Data MongoDB documents), so no application change is needed.

Notes:

- Upgrading Docker Desktop does **not** currently help: MongoDB only lifts the
  check for kernel 7.0.14 and above, and no Docker Desktop release ships that
  yet. Revert to the default `8.0` once one does.
- Neither `GLIBC_TUNABLES=glibc.pthread.rseq=0` nor a newer 8.x patch release
  bypasses it — the check runs before startup regardless.
- If you had already started a MongoDB 8.x volume, wipe it before downgrading:
  `docker compose down -v` (destroys local DB data).

### 8) Reset environment completely

Use this when state is inconsistent and a clean restart is needed.

```bash
docker compose down -v
docker system prune -f
docker compose pull
docker compose up -d
```

## Notes

- Container-to-container communication uses service names (`postgres`, `mongodb`, `eureka-server`, `zipkin`, `keycloak`), not `localhost`.
- Health checks control startup sequencing through `depends_on` conditions.
- Database data is persisted in named volumes (`postgres_data`, `mongo_data`).

## Related Chapter Guide

- Chapter 10 overview and packaging concepts: [chapter-10/README.md](../chapter-10/README.md)
