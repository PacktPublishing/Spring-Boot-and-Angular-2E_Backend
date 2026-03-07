#!/usr/bin/env bash
set -euo pipefail

WORKSPACE_ROOT="$(cd "$(dirname "$0")" && pwd)"

KNOWN_MAIN_CLASSES=(
  "EurekaServerApplication"
  "GatewayServerApplication"
  "InventoryMsApplication"
  "UserMsApplication"
)

echo "Stopping Java apps running from: $WORKSPACE_ROOT"

# Find candidate Java processes and match by:
# 1) command line contains workspace path, OR
# 2) command line contains one of known app main classes, OR
# 3) process current working directory is under workspace root.
PIDS=""

while IFS=$'\t' read -r pid cmdline; do
  [[ -z "$pid" || -z "$cmdline" ]] && continue

  is_match=false

  if [[ "$cmdline" == *"$WORKSPACE_ROOT"* ]]; then
    is_match=true
  else
    for class_name in "${KNOWN_MAIN_CLASSES[@]}"; do
      if [[ "$cmdline" == *"$class_name"* ]]; then
        is_match=true
        break
      fi
    done
  fi

  if [[ "$is_match" == false ]]; then
    cwd="$(lsof -a -d cwd -p "$pid" -Fn 2>/dev/null | awk '/^n/ { sub(/^n/, "", $0); print; exit }')"
    if [[ -n "$cwd" && "$cwd" == "$WORKSPACE_ROOT"* ]]; then
      is_match=true
    fi
  fi

  if [[ "$is_match" == true ]]; then
    PIDS+="$pid"$'\n'
  fi
done < <(
  ps -axo pid=,command= | awk '
    {
      pid = $1
      sub(/^[0-9]+[[:space:]]+/, "", $0)
      if ($0 ~ /(^|[[:space:]])([^[:space:]]*\/)?java([[:space:]]|$)/) {
        print pid "\t" $0
      }
    }
  '
)

PIDS="$(printf "%s" "$PIDS" | awk 'NF' | sort -u)"

if [[ -z "$PIDS" ]]; then
  echo "No matching workspace Java processes found."
  exit 0
fi

echo "Found process IDs:"
printf '%s\n' "$PIDS"

echo "Sending SIGTERM..."
while IFS= read -r pid; do
  [[ -z "$pid" ]] && continue
  kill -TERM "$pid" 2>/dev/null || true
done <<< "$PIDS"

# Wait up to 10 seconds for graceful shutdown.
for _ in {1..10}; do
  sleep 1
  remaining=""
  while IFS= read -r pid; do
    [[ -z "$pid" ]] && continue
    if kill -0 "$pid" 2>/dev/null; then
      remaining+="$pid"$'\n'
    fi
  done <<< "$PIDS"

  if [[ -z "$remaining" ]]; then
    echo "All workspace applications stopped gracefully."
    exit 0
  fi
done

echo "Some processes are still running. Sending SIGKILL..."
while IFS= read -r pid; do
  [[ -z "$pid" ]] && continue
  kill -KILL "$pid" 2>/dev/null || true
done <<< "$remaining"

echo "Done."
