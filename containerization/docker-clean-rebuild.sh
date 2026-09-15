#!/usr/bin/env bash
#
# Clean rebuild of the bookstore stack: postgres, mongodb, zipkin, keycloak,
# eureka-server, inventory-service, user-service, gateway-server.
#
# Usage:
#   ./docker-clean-rebuild.sh              # recreate containers, keep database volumes
#   ./docker-clean-rebuild.sh --wipe-data  # also delete the postgres/mongo volumes
#   ./docker-clean-rebuild.sh --no-pull    # reuse local images instead of re-pulling
#
set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
COMPOSE_FILE="$SCRIPT_DIR/docker-compose.yml"

WIPE_DATA=0
PULL=1
for arg in "$@"; do
  case "$arg" in
    --wipe-data) WIPE_DATA=1 ;;
    --no-pull) PULL=0 ;;
    -h | --help)
      sed -n '3,9p' "$0"
      exit 0
      ;;
    *)
      echo "[ERROR] Unknown option: $arg (try --help)" >&2
      exit 2
      ;;
  esac
done

[ -f "$COMPOSE_FILE" ] || {
  echo "[ERROR] Compose file not found: $COMPOSE_FILE" >&2
  exit 1
}

# Prefer the Compose v2 plugin; fall back to the standalone v1 binary. Only v2
# understands "up --wait", so the health gate below is conditional on it.
SUPPORTS_WAIT=0
if docker compose version >/dev/null 2>&1; then
  compose() { docker compose -f "$COMPOSE_FILE" "$@"; }
  SUPPORTS_WAIT=1
elif command -v docker-compose >/dev/null 2>&1; then
  compose() { docker-compose -f "$COMPOSE_FILE" "$@"; }
  echo "[WARN] Using legacy docker-compose v1; skipping the health gate."
else
  echo "[ERROR] Neither 'docker compose' nor 'docker-compose' is available." >&2
  exit 1
fi

docker info >/dev/null 2>&1 || {
  echo "[ERROR] Cannot reach the Docker daemon. Is Docker running?" >&2
  exit 1
}

# "compose down" is scoped to this Compose project, so it removes exactly this
# stack's containers and network. The previous version grepped global Docker
# state for names like 'user' and 'mongo', which could delete unrelated
# containers, images and volumes belonging to other projects on the machine.
echo "[INFO] Tearing down the stack..."
if [ "$WIPE_DATA" -eq 1 ]; then
  echo "[WARN] --wipe-data: the postgres and mongo volumes will be deleted."
  compose down --volumes --remove-orphans
else
  compose down --remove-orphans
fi

# Every service uses a prebuilt "image:" reference and none declares "build:",
# so "pull" is what refreshes them. ("compose build" only warns "No services to
# build" here, which is why the old --no-cache rebuild had no effect.)
if [ "$PULL" -eq 1 ]; then
  echo "[INFO] Pulling the latest images..."
  compose pull
fi

echo "[INFO] Starting the stack..."
UP_ARGS=(up -d --force-recreate)
# --wait blocks until every service with a healthcheck reports healthy and exits
# non-zero if one never does, so a failing healthcheck surfaces here rather than
# as a confusing "dependency failed to start" on an unrelated service.
[ "$SUPPORTS_WAIT" -eq 1 ] && UP_ARGS+=(--wait)

if compose "${UP_ARGS[@]}"; then
  echo "[SUCCESS] Stack is up."
  compose ps
else
  status=$?
  echo "[ERROR] The stack did not come up cleanly (exit $status)." >&2
  compose ps >&2
  echo "[HINT] Check an unhealthy service with:" >&2
  echo "         docker compose -f \"$COMPOSE_FILE\" logs <service>" >&2
  exit "$status"
fi
