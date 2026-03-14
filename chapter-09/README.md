# Chapter 09 - Real-Time Updates with Reactive Programming Using Spring WebFlux and Server-Sent Events

## Chapter Overview

This chapter extends the Bookstore microservices with real-time inventory notifications delivered through Server-Sent Events (SSE). The inventory service publishes domain events when books are created or repriced, the gateway exposes those events through a long-lived HTTP stream, and browser or CLI clients consume them without polling.

The implementation builds directly on the secured gateway from Chapter 08:

- SSE reads are public so dashboards and monitoring pages can subscribe easily.
- Book writes remain authenticated through the gateway and Keycloak-backed JWT validation.
- Observability remains intact because the gateway and services still emit trace and log data for the request path that produced each event.

## Table of Contents

- [What Are Server-Sent Events?](#what-are-server-sent-events)
- [SSE Architecture in Bookstore](#sse-architecture-in-bookstore)
- [Implementing SSE in Inventory-MS](#implementing-sse-in-inventory-ms)
- [Consuming SSE with Browser and CLI Clients](#consuming-sse-with-browser-and-cli-clients)
- [Testing and Validating SSE](#testing-and-validating-sse)
- [Detailed Test Scenarios (API and Tools)](#detailed-test-scenarios-api-and-tools)
- [Event Payload Reference](#event-payload-reference)
- [Troubleshooting Guide](#troubleshooting-guide)
- [Security and Observability for SSE](#security-and-observability-for-sse)
- [Companion Assets](#companion-assets)
- [References](#references)
- [Summary](#summary)

## What Are Server-Sent Events?

Server-Sent Events provide a simple one-way streaming channel from server to client over standard HTTP.

- The server responds with the media type `text/event-stream`.
- The browser keeps the connection open and listens for named events.
- The client automatically retries if the connection is interrupted.

SSE is a strong fit when the server needs to push notifications, but the client does not need a full duplex protocol such as WebSockets.

Typical use cases include:

- Live notification panels
- Admin dashboards
- Status streams
- Inventory and pricing updates

## SSE Architecture in Bookstore

The Bookstore implementation routes all notification traffic through the gateway and keeps the event production logic inside the inventory service.

```text
Browser / CLI Client
        |
        | EventSource / curl -N
        v
API Gateway :8080
        |
        | /packt/inventory/api/notifications/**
        v
Inventory Service :8081
        |
        | NotificationController
        v
NotificationService (Reactor sink)
        |
        | events emitted by BookService
        v
Connected subscribers
```

Only two business event types are emitted in this chapter:

- `NEW_BOOK`
- `PRICE_CHANGE`

## Implementing SSE in Inventory-MS

The inventory microservice uses Spring WebFlux and Project Reactor to broadcast events.

Core implementation decisions:

1. Reactive event broadcasting: `NotificationService` uses a Reactor `Sinks.Many` to fan out events to multiple listeners.
2. Named SSE events: `NotificationController` emits named SSE frames so clients can subscribe to `NEW_BOOK` and `PRICE_CHANGE` independently.
3. Heartbeat comments: the stream emits keepalive comments so intermediaries are less likely to time out an otherwise idle connection.
4. Gateway-first access: clients subscribe through the gateway path at `/packt/inventory/api/notifications/**`, not directly to the service port.
5. Separate gateway route: the notification route is defined before the general inventory route to avoid circuit breaker behavior interfering with long-lived SSE connections.

The exposed notification endpoints are:

```text
GET /packt/inventory/api/notifications/stream
GET /packt/inventory/api/notifications/stream/filtered?eventType=NEW_BOOK
GET /packt/inventory/api/notifications/status
```

## Consuming SSE with Browser and CLI Clients

This chapter includes three client-side validation tools:

- `test-sse-debug.html`: raw event inspector for troubleshooting and payload validation
- `test-sse.html`: reader-friendly event dashboard for book and price notifications
- `test-sse-curl.sh`: terminal subscriber with optional event filtering

All three clients read from the public gateway stream. None of them send authenticated write requests themselves. That separation is intentional:

- Subscription is unauthenticated.
- Book creation and updates require a Bearer token.

This makes the validation flow very clear: keep one client subscribed, then trigger authenticated writes from Postman or curl.

## Testing and Validating SSE

This section is the recommended chapter workflow for proving that the stream, routing, security, and event publication all work together.

### Validation Goals

By the end of the test, you should have verified all of the following:

1. The notification status endpoint is reachable through the gateway.
2. At least one client can subscribe successfully to the SSE stream.
3. A book creation request emits a `NEW_BOOK` event.
4. A price patch request emits a `PRICE_CHANGE` event.
5. The filtered SSE endpoint only emits the requested event type.

### Step 1 - Start the Chapter Services

Make sure the four chapter services are running:

1. Eureka Server
2. Inventory Microservice
3. User Microservice
4. Gateway Server

The gateway entry point used throughout the rest of this chapter is:

```text
http://localhost:8080
```

### Step 2 - Check the Notification Service Health

Before opening a stream, confirm the gateway can reach the notification endpoint:

```bash
curl http://localhost:8080/packt/inventory/api/notifications/status
```

Expected shape:

```json
{
  "status": "UP",
  "activeSubscribers": 0,
  "message": "Notification service is operating normally"
}
```

The exact subscriber count will vary. Once a browser page or terminal client connects, the count should increase.

### Step 3 - Open an SSE Client

Use one of the included chapter tools.

#### Option A - Raw Debug Console

Open `test-sse-debug.html`, confirm the gateway URL, and click Connect.

Use this page when you want to validate:

- Raw JSON payloads
- Event IDs
- Reconnect behavior
- Subscriber count from the status endpoint

#### Option B - Visual Dashboard

Open `test-sse.html` and click Connect Stream.

Use this page when you want to validate:

- Event totals
- `NEW_BOOK` and `PRICE_CHANGE` counters
- Filtered display tabs
- Active subscriber count alongside the event list

#### Option C - Terminal Subscriber

```bash
./test-sse-curl.sh
```

To point the script to a non-default gateway URL:

```bash
SSE_GATEWAY_URL=http://localhost:8080 ./test-sse-curl.sh
```

To validate a filtered stream from the terminal:

```bash
./test-sse-curl.sh NEW_BOOK
./test-sse-curl.sh PRICE_CHANGE
```

### Step 4 - Obtain a JWT Access Token

SSE subscriptions are public, but writes still require authentication. Sign in through the gateway and capture the `accessToken` field from the response.

Example request shape:

```bash
curl -X POST http://localhost:8080/packt/user/api/users/signin \
  -H "Content-Type: application/json" \
  -d '{
    "email": "<your-email>",
    "password": "<your-password>"
  }'
```

After sign-in, export the token in your shell or Postman environment:

```bash
export ACCESS_TOKEN="<paste-access-token-here>"
```

### Step 5 - Fetch or Create an Author

The current book creation API expects `authorId`. That means the validation flow must use an existing author or create one first.

List authors:

```bash
curl http://localhost:8080/packt/inventory/api/authors \
  -H "Authorization: Bearer ${ACCESS_TOKEN}"
```

If needed, create an author:

```bash
curl -X POST http://localhost:8080/packt/inventory/api/authors \
  -H "Authorization: Bearer ${ACCESS_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Josh Long",
    "nationality": "American"
  }'
```

Save the returned author id for the next step.

### Step 6 - Validate the `NEW_BOOK` Event

With the SSE client still connected, create a book through the gateway:

```bash
curl -X POST http://localhost:8080/packt/inventory/api/books \
  -H "Authorization: Bearer ${ACCESS_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Reactive Spring WebFlux",
    "isbn": "978-1617297571",
    "authorId": 1,
    "price": 49.99,
    "genre": "Technology",
    "description": "Hands-on reactive Spring guide",
    "pageCount": 420
  }'
```

Expected results:

- The write request returns `201 Created`.
- The SSE client receives a `NEW_BOOK` event.
- The event payload includes `bookId`, `bookTitle`, `isbn`, and `eventData.authorName`.

### Step 7 - Validate the `PRICE_CHANGE` Event

Patch the same book with a different price:

```bash
curl -X PATCH http://localhost:8080/packt/inventory/api/books/<book-id> \
  -H "Authorization: Bearer ${ACCESS_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "price": 39.99
  }'
```

Expected results:

- The write request returns `200 OK`.
- The SSE client receives a `PRICE_CHANGE` event.
- The payload contains `oldPrice`, `newPrice`, `priceChange`, and `percentageChange`.

### Step 8 - Validate the Filtered Stream

The filtered endpoint should only emit the selected event type.

Browser or debug page:

- Choose `NEW_BOOK` or `PRICE_CHANGE` in the stream filter before connecting.

CLI:

```bash
./test-sse-curl.sh PRICE_CHANGE
```

Then repeat both write operations and confirm that only matching event frames appear in the filtered subscriber.

### Step 9 - Re-check Subscriber Counts

Run the status endpoint again while a client is connected:

```bash
curl http://localhost:8080/packt/inventory/api/notifications/status
```

Expected behavior:

- `activeSubscribers` increases when a stream is open.
- `activeSubscribers` drops after the browser page or terminal subscriber disconnects.

## Detailed Test Scenarios (API and Tools)

This section provides a deeper, tool-by-tool validation flow so you can prove not only that events are emitted, but that each test client behaves correctly.

### Shared Variables for API Calls

Use these shell variables to keep commands repeatable during testing:

```bash
export BASE_URL="http://localhost:8080"
export ACCESS_TOKEN="<paste-access-token-here>"
```

Optional helper values for repeated calls:

```bash
export AUTHOR_ID="1"
export BOOK_ID="1"
```

### Scenario A - CLI Subscriber + API Calls (Fastest End-to-End Check)

Terminal 1 (subscriber):

```bash
./test-sse-curl.sh
```

Terminal 2 (writes):

1. Create or confirm an author.

```bash
curl "${BASE_URL}/packt/inventory/api/authors" \
  -H "Authorization: Bearer ${ACCESS_TOKEN}"
```

```bash
curl -X POST "${BASE_URL}/packt/inventory/api/authors" \
  -H "Authorization: Bearer ${ACCESS_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{"name":"Josh Long","nationality":"American"}'
```

1. Create a book and capture `id` from the response.

```bash
curl -X POST "${BASE_URL}/packt/inventory/api/books" \
  -H "Authorization: Bearer ${ACCESS_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Reactive Spring WebFlux",
    "isbn": "978-1617297571",
    "authorId": 1,
    "price": 49.99,
    "genre": "Technology",
    "description": "Hands-on reactive Spring guide",
    "pageCount": 420
  }'
```

1. Patch price for the created book.

```bash
curl -X PATCH "${BASE_URL}/packt/inventory/api/books/${BOOK_ID}" \
  -H "Authorization: Bearer ${ACCESS_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{"price":39.99}'
```

Validate in Terminal 1:

- you receive a `NEW_BOOK` frame after create
- you receive a `PRICE_CHANGE` frame after patch
- each event contains `eventId`, `bookId`, and `eventData`

### Scenario B - Dashboard Validation with `test-sse.html`

1. Open `test-sse.html`.
2. Connect to stream using the gateway URL.
3. Trigger create and patch API calls.

Validate in dashboard:

- `Total Events` increases by 2
- `New Books` increases by 1
- `Price Changes` increases by 1
- `Active Subscribers` is greater than 0 while connected

Use this scenario to validate user-facing behavior and visual counters.

### Scenario C - Raw Protocol Validation with `test-sse-debug.html`

1. Open `test-sse-debug.html`.
2. Connect and click `Check Service Status`.
3. Trigger the same create and patch API calls.

Validate in debug log:

- named events appear as `NEW_BOOK` and `PRICE_CHANGE`
- payload structure is complete and parseable JSON
- reconnect attempts are visible if the stream is interrupted

Use this scenario when you need low-level troubleshooting, especially for malformed payloads or missing event names.

### Scenario D - Filter Contract Test

Terminal 1:

```bash
./test-sse-curl.sh NEW_BOOK
```

Terminal 2:

```bash
./test-sse-curl.sh PRICE_CHANGE
```

Terminal 3:

- run one create call and one patch call

Expected behavior:

- `NEW_BOOK` terminal receives create events only
- `PRICE_CHANGE` terminal receives patch events only
- no cross-delivery between filtered subscribers

### Scenario E - Subscriber Lifecycle Validation

1. Query status before subscribing.
2. Subscribe from one or more clients.
3. Query status again.
4. Disconnect clients.
5. Query status a final time.

Commands:

```bash
curl "${BASE_URL}/packt/inventory/api/notifications/status"
```

Expected behavior:

- subscriber count rises after connect
- subscriber count drops after disconnect

This confirms the SSE lifecycle is managed correctly and helps identify ghost connections.

### Minimum Acceptance Checklist for Chapter Completion

Mark SSE testing complete when all are true:

1. Notification status endpoint is reachable and returns `UP`.
2. At least one subscriber connects from each tool family (CLI and browser).
3. `NEW_BOOK` is emitted after successful create.
4. `PRICE_CHANGE` is emitted after successful patch.
5. Filtered subscriptions isolate event types correctly.
6. Subscriber counts rise and fall as clients connect and disconnect.

## Event Payload Reference

The payload examples below are aligned with the current event model in `inventory-ms`.

### `NEW_BOOK` Event

```json
{
  "eventType": "NEW_BOOK",
  "timestamp": "2026-02-15T10:30:00",
  "eventId": "550e8400-e29b-41d4-a716-446655440000",
  "bookId": 42,
  "bookTitle": "Reactive Spring WebFlux",
  "isbn": "978-1617297571",
  "eventData": {
    "authorName": "Josh Long",
    "genre": "Technology",
    "price": 49.99,
    "quantity": 100,
    "published": "2025-01-15",
    "description": "Hands-on reactive Spring guide",
    "pageCount": 420,
    "coverImageUrl": null
  }
}
```

`eventData` fields come from `NewBookEventData` and may be null when optional values are not set.

### `PRICE_CHANGE` Event

```json
{
  "eventType": "PRICE_CHANGE",
  "timestamp": "2026-02-15T10:35:00",
  "eventId": "660f9511-f30c-52e5-b827-557766551111",
  "bookId": 42,
  "bookTitle": "Reactive Spring WebFlux",
  "isbn": "978-1617297571",
  "eventData": {
    "oldPrice": 49.99,
    "newPrice": 39.99,
    "priceChange": -10.0,
    "percentageChange": -20.0
  }
}
```

`eventData` fields come from `PriceChangeEventData`.

## Troubleshooting Guide

### Stream connects but no events arrive

- Confirm the write request succeeded and returned `200` or `201`.
- Verify the request carried `Authorization: Bearer ${ACCESS_TOKEN}`.
- Use `test-sse-debug.html` to inspect raw frames and confirm events are not filtered out by the UI.

### Create-book fails with validation errors

- Use `authorId`, not `authorName`.
- Verify required fields (`title`, `isbn`, `authorId`, `price`) are present.
- Create or fetch an author first if the id is unknown.

### `401 Unauthorized` on writes

- This is expected if sign-in was skipped or the token expired.
- SSE subscriptions are public, but book and author writes are authenticated.

### Browser reconnect loops

- Confirm the gateway and inventory service are both running.
- Verify `GET /packt/inventory/api/notifications/status` returns `UP`.
- Check gateway timeout and CORS settings if this appears only in browser clients.

### `test-sse-curl.sh` exits with code 22

- Exit code `22` comes from curl when HTTP status is `>= 400` while `--fail-with-body` is enabled.
- Check the gateway URL in `SSE_GATEWAY_URL` and verify the notification endpoint path.
- Confirm the gateway route for `/packt/inventory/api/notifications/**` is active.

## Security and Observability for SSE

The security model is deliberately split:

- `GET /packt/inventory/api/notifications/**` is public.
- Write operations under `/packt/inventory/api/**` still require authentication.

That separation lets you validate SSE with a simple browser page while still protecting inventory mutations.

Observability remains important for SSE because the most common failures are integration failures:

- Bad routing
- Premature connection timeouts
- Missing event publication after successful writes
- Authentication failures on the write path

Use the notification status endpoint, service logs, and gateway traces together when diagnosing issues.

## Companion Assets

This chapter ships with the following validation aids:

- `test-sse-debug.html`
- `test-sse.html`
- `test-sse-curl.sh`
- `SSE-IMPLEMENTATION.md`

`SSE-IMPLEMENTATION.md` remains available as a compact lab companion, while this README now contains the full end-to-end chapter flow.

## References

- [Spring WebFlux Documentation](https://docs.spring.io/spring-framework/reference/web/webflux.html)
- [Project Reactor Reference](https://projectreactor.io/docs/core/release/reference/)
- [SSE Specification](https://html.spec.whatwg.org/multipage/server-sent-events.html)
- [MDN EventSource API](https://developer.mozilla.org/en-US/docs/Web/API/EventSource)

## Summary

In this chapter, you added a reactive notification stream to the Bookstore inventory service and validated it end to end through the gateway.

You now have:

- Real-time `NEW_BOOK` and `PRICE_CHANGE` notifications
- A public SSE read path through the gateway
- Authenticated write operations that emit those events
- Browser and CLI tools for stream validation

The next logical step is to consume the stream from a richer frontend or operational dashboard and use the same validation flow to prove those clients stay in sync with the backend.
