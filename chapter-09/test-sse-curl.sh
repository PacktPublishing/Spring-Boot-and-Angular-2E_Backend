#!/bin/bash
# Test SSE Connection with cURL
# Run this and then use Postman to create/update a book

echo "🔔 Connecting to SSE Stream..."
echo "================================"
echo ""
echo "✅ Connection opens successfully if you see this"
echo "📨 Waiting for events..."
echo ""
echo "👉 Now use Postman to:"
echo "   1. Create a book (POST /packt/inventory/api/books)"
echo "   2. Or update a book price (PATCH /packt/inventory/api/books/{id})"
echo ""
echo "Events will appear below:"
echo "================================"
echo ""

curl -N -H "Accept: text/event-stream" \
  http://localhost:8080/packt/inventory/api/notifications/stream
