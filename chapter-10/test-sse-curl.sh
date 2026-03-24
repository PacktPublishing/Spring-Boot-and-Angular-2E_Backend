#!/usr/bin/env bash

set -euo pipefail

GATEWAY_URL="${SSE_GATEWAY_URL:-http://localhost:8080}"
EVENT_TYPE="${1:-all}"
STATUS_URL="${GATEWAY_URL%/}/packt/inventory/api/notifications/status"

case "$EVENT_TYPE" in
  all)
    STREAM_URL="${GATEWAY_URL%/}/packt/inventory/api/notifications/stream"
    ;;
  NEW_BOOK|PRICE_CHANGE)
    STREAM_URL="${GATEWAY_URL%/}/packt/inventory/api/notifications/stream/filtered?eventType=${EVENT_TYPE}"
    ;;
  *)
    echo "Usage: ./test-sse-curl.sh [all|NEW_BOOK|PRICE_CHANGE]"
    echo "Example: ./test-sse-curl.sh PRICE_CHANGE"
    exit 1
    ;;
esac

if ! command -v curl >/dev/null 2>&1; then
  echo "curl is required to run this helper."
  exit 1
fi

cleanup() {
  echo
  echo "🔌 SSE subscription closed."
}

trap cleanup EXIT
trap 'exit 130' INT

echo "🔔 Preparing SSE validation session"
echo "================================"
echo "Gateway: ${GATEWAY_URL}"
echo "Stream : ${STREAM_URL}"
echo

if status_response="$(curl -fsS "${STATUS_URL}" 2>/dev/null)"; then
  echo "📊 Notification service status: ${status_response}"
else
  echo "⚠️  Could not read ${STATUS_URL}. The stream may still work once services are up."
fi

echo
echo "✅ Subscribe first, then trigger events from Postman or curl:"
echo "   1. Sign in at POST /packt/user/api/users/signin to get a Bearer token"
echo "   2. Fetch authors with GET /packt/inventory/api/authors or create one with POST /packt/inventory/api/authors"
echo "   3. Create a book with POST /packt/inventory/api/books using authorId"
echo "   4. Update the book price with PATCH /packt/inventory/api/books/{id}"
echo
echo "📨 Waiting for SSE frames below"
echo "================================"
echo

curl --fail-with-body --no-buffer -H "Accept: text/event-stream" "${STREAM_URL}"
