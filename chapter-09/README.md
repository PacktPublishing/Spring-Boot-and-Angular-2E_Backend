# 📘 Chapter 09 — Real-Time Updates with Server-Sent Events (SSE)

## Chapter Overview
This chapter extends the Bookstore microservices by adding **real-time event streaming** using **Server-Sent Events (SSE)**. You will learn how to push inventory updates instantly to clients, integrate SSE endpoints, and test event delivery using browser, Postman, and command-line tools. The chapter builds on the secure, observable, and documented microservices from previous chapters.

---

## Table of Contents
- [What Are Server-Sent Events?](#what-are-server-sent-events)
- [SSE Architecture in Bookstore](#sse-architecture-in-bookstore)
- [Implementing SSE in Inventory-MS](#implementing-sse-in-inventory-ms)
- [Consuming SSE: Browser, Postman, CLI](#consuming-sse-browser-postman-cli)
- [Testing SSE Endpoints](#testing-sse-endpoints)
- [Security & Observability for SSE](#security--observability-for-sse)
- [Summary](#summary)

---

# What Are Server-Sent Events?

SSE is a web technology for **one-way, real-time streaming** from server to client over HTTP. Unlike WebSockets, SSE is simple, reliable, and ideal for event notifications.

- **Use Cases:** Inventory updates, notifications, live data feeds
- **Protocol:** HTTP/1.1, text/event-stream
- **Client Support:** Native in browsers via `EventSource`

---

# SSE Architecture in Bookstore

- **Inventory-MS:** Publishes book events (add/update/delete)
- **Gateway Server:** Routes SSE endpoints securely
- **User-MS:** Can subscribe to inventory events
- **Eureka Server:** Service discovery for SSE endpoints

---

# Implementing SSE in Inventory-MS

- Expose `/inventory/api/books/stream` endpoint
- Use Spring's `SseEmitter` for event streaming
- Publish events on book creation/update
- Integrate with existing REST controllers

---

# Consuming SSE: Browser, Postman, CLI

- **Browser:** Open `test-sse-debug.html` or `test-sse.html` and click "Connect"
- **Postman:** Use collection `Packt- Java and Angular Second Ed.postman_collection.json` to trigger events
- **CLI:** Run `./test-sse-curl.sh` to subscribe and see events in terminal

---

# Testing SSE Endpoints

- Use browser debug page for step-by-step event validation
- Use Postman to trigger book events
- Use CLI for headless testing
- Troubleshoot with logs and event payloads

---

# Security & Observability for SSE

- SSE endpoints are secured via Spring Security & JWT (see Chapter 08)
- All events are traceable and logged
- Distributed tracing and metrics enabled via Micrometer

---

# Summary

Chapter 09 enables **real-time inventory updates** in the Bookstore system using SSE. You can now push events to clients instantly, improving user experience and system responsiveness. All microservices remain secure, observable, and discoverable.


## Updated Source Code Notes


## Quick Start


## License
See LICENSE for details.

---

# Server-Sent Events (SSE) Implementation for Book Inventory

## Quick Start

**To see SSE in action (STEP-BY-STEP):**

### Option 1: Use Debug Page (RECOMMENDED)

1. **Open test-sse-debug.html** in your browser → Click "Connect"
	 - You'll see "✅ Connected" immediately
	 - The log shows "Listening for events..."

2. **Keep that page open**, then open Postman:
	 - Import: `Packt- Java and Angular Second Ed.postman_collection.json`
	 - Find: `01-Inventory-MS → books → POST /inventory/api/books`
	 - **Send the request**

3. **Watch test-sse-debug.html** - event appears within 1 second! 🎉
	 - Shows full raw event data
	 - Easier to debug than fancy UI

### Option 2: Use Command Line

1. **Terminal 1** - Subscribe to events:
```bash
./test-sse-curl.sh
# Shows: "Waiting for events..."
```

2. **Postman** - Create a book (POST request)

3. **Terminal 1** - See the event appear instantly!

### Option 3: Use test-sse.html (Full UI)

Same steps as debug page, but with prettier interface.

---

**✅ If you don't see events:**
- Check browser console (F12) for JavaScript errors
- Verify Postman request succeeded (should return book with ID)
- Try test-sse-debug.html - shows raw event data
- Check service status: `curl http://localhost:8080/packt/inventory/api/notifications/status`

---

## Overview

This implements real-time notifications for book inventory events using Server-Sent Events (SSE) with Spring WebFlux reactive programming.

**Event Types:**
- `NEW_BOOK` - When books are created
- `PRICE_CHANGE` - When prices are updated

**⚠️ Authentication Note:**
- **SSE subscription (read)** - Public, no authentication required
- **Book operations (write)** - Requires JWT authentication from Keycloak
- For testing, use Postman or authenticated frontend to create/update books while watching events in test-sse.html

## What Changed

### 1. Inventory Microservice (`inventory-ms`)

**Dependencies Added** (`pom.xml`):
```xml
<dependency>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
<dependency>
		<groupId>io.projectreactor</groupId>
		<artifactId>reactor-core</artifactId>
</dependency>
```

**New Classes:**
- `event/BookEvent.java` - Base event structure with EventType enum
- `event/NewBookEventData.java` - Data for new book events  
- `event/PriceChangeEventData.java` - Data for price change events
- `service/NotificationService.java` - Manages SSE broadcasting with Reactor Sinks
- `controller/NotificationController.java` - Exposes SSE endpoints

**Modified:**
- `service/BookService.java` - Publishes events when books are created/updated

**SSE Endpoints:**
```
GET /api/notifications/stream           - All events
GET /api/notifications/stream/filtered   - Filtered by type
GET /api/notifications/status            - Service health
```

### 2. Gateway Server (`gateway-server`)

**Route Configuration** (`GatewayRouteConfig.java`):
- Added notification route **BEFORE** general inventory route (critical for SSE)
- No circuit breaker on SSE route (long-lived connections)
- 1-hour timeout for SSE connections

**Security** (`SecurityConfig.java`):
- Enabled CORS for SSE endpoints (`.cors(cors -> {})`)
- Configured `allowedOriginPatterns` for local development
- Public access to notification endpoints (`.permitAll()`)

**Application Config** (`application.yml`):
- Extended `response-timeout` from 10s to 3600s (1 hour)

## Architecture

```
Client (Browser/App)
		↓
EventSource Connection
		↓
API Gateway (port 8080)
		↓
Inventory Service (port 8081)
		↓
NotificationController
		↓
NotificationService (Reactor Sink)
		↓ (broadcasts to all)
SSE Subscribers
```

### Key Technical Decisions

1. **Project Reactor Sinks** - Thread-safe pub-sub pattern with backpressure
2. **Separate SSE Route** - Must be before general routes to avoid circuit breaker interference
3. **Extended Timeout** - SSE connections are long-lived, need 1+ hour timeouts
4. **Keepalive Heartbeat** - 15-second heartbeat prevents connection timeouts
5. **CORS Configuration** - Supports local file:// testing with `allowedOriginPatterns`

## Testing

### Step 1: Verify SSE Connection
```bash
# Check service is running and see active subscribers
curl http://localhost:8080/packt/inventory/api/notifications/status
```

Expected response:
```json
{
	"status": "UP",
	"activeSubscribers": 27,
	"message": "Notification service is operating normally"
}
```

✅ If you see `activeSubscribers > 0`, your test-sse.html IS connected and working!

### Step 2: Subscribe to Events

**Using Browser (RECOMMENDED):**
1. Open `test-sse.html` in your browser
2. Click "🔌 Connect to SSE Stream"
3. Status should show "Connected - Listening for events..."
4. Leave this tab open

**Using cURL (alternative):**
```bash
curl -N -H "Accept: text/event-stream" \
	http://localhost:8080/packt/inventory/api/notifications/stream
```

### Step 3: Trigger Events

**⚠️ Important:** Book creation/updates require JWT authentication. Use your **existing Postman collection**.

#### Using Your Postman Collection:

1. **Import Collection:**
	 - Open: `Packt- Java and Angular Second Ed.postman_collection.json`
	 - This collection already has authentication configured!

2. **Create Book** (watch test-sse.html for NEW_BOOK event):
	 - Use: `01-Inventory-MS → books → POST /inventory/api/books`
	 - Body example:
	 ```json
	 {
		 "title": "Reactive Spring WebFlux",
		 "isbn": "978-TEST-001",
		 "price": 49.99,
		 "quantity": 100,
		 "authorName": "Josh Long",
		 "genre": "Technology"
	 }
	 ```
	 - Send the request
	 - **Watch test-sse.html** - you'll see a NEW_BOOK event appear immediately! 🎉

3. **Update Price** (watch test-sse.html for PRICE_CHANGE event):
	 - Use: `01-Inventory-MS → books → PATCH /inventory/api/books/{id}`
	 - Body:
	 ```json
	 {
		 "price": 39.99
	 }
	 ```
	 - Send the request
	 - **Watch test-sse.html** - you'll see a PRICE_CHANGE event appear! 💰

**Check status:**
```bash
curl http://localhost:8080/packt/inventory/api/notifications/status
```

## Event Format

### NEW_BOOK Event
```json
{
	"eventType": "NEW_BOOK",
	"timestamp": "2026-02-15T10:30:00",
	"eventId": "550e8400-e29b-41d4-a716-446655440000",
	"bookId": 42,
	"bookTitle": "Reactive Spring",
	"isbn": "978-1234567890",
	"eventData": {
		"authorName": "Josh Long",
		"genre": "Technology",
		"price": 49.99,
		"quantity": 100
	}
}
```

### PRICE_CHANGE Event
```json
{
	"eventType": "PRICE_CHANGE",
	"timestamp": "2026-02-15T10:35:00",
	"eventId": "660f9511-f30c-52e5-b827-557766551111",
	"bookId": 42,
	"bookTitle": "Reactive Spring",
	"isbn": "978-1234567890",
	"eventData": {
		"oldPrice": 49.99,
		"newPrice": 39.99,
		"priceChange": -10.00,
		"percentageChange": -20.0
	}
}
```

## Client Integration Examples

### JavaScript (Browser)
```javascript
const eventSource = new EventSource(
	'http://localhost:8080/packt/inventory/api/notifications/stream'
);

eventSource.addEventListener('NEW_BOOK', (event) => {
	const book = JSON.parse(event.data);
	console.log('New book:', book.bookTitle);
});

eventSource.addEventListener('PRICE_CHANGE', (event) => {
	const change = JSON.parse(event.data);
	console.log('Price changed:', change.bookTitle);
});

eventSource.onerror = (error) => {
	console.error('SSE error:', error);
};
```

### Angular
```typescript
@Injectable({ providedIn: 'root' })
export class NotificationService {
	subscribeToNotifications(): Observable<BookEvent> {
		return new Observable(observer => {
			const eventSource = new EventSource(
				'http://localhost:8080/packt/inventory/api/notifications/stream'
			);
      
			eventSource.onmessage = (event) => {
				observer.next(JSON.parse(event.data));
			};
      
			eventSource.onerror = (error) => observer.error(error);
      
			return () => eventSource.close();
		});
	}
}
```

## Production Considerations

1. **Authentication** - Add JWT token validation for SSE endpoints
2. **Rate Limiting** - Limit connections per user/IP
3. **Load Balancing** - Use sticky sessions or Redis Pub/Sub for multi-instance
4. **Monitoring** - Track active subscriber count and event emission rates
5. **Buffer Size** - Adjust Sink buffer (currently 256) based on load

## Troubleshooting

**HTML shows connected but no events appear:**
- ✅ Connection works (check status endpoint for active subscribers)
- ❌ No events generated because book operations require authentication
- **Solution:** Use Postman with JWT token to create/update books, or use the Angular frontend with authentication

**401 Unauthorized when creating books:**
- This is **expected** - the API requires authentication for write operations
- SSE notifications are public (read-only)
- Use Postman or authenticated frontend to trigger events

**No events received:**
- Check service logs for errors
- Verify SSE connection is active (`status` endpoint shows subscribers)
- Ensure operations happen AFTER connecting

**Connection drops:**
- Check gateway timeout settings (needs > 1 hour)
- Verify network stability
- Check for proxy/firewall issues

**CORS errors:**
- Verify `.cors(cors -> {})` is enabled in SecurityConfig
- Check `allowedOriginPatterns` includes your origin
- For file:// protocol, use `null` origin pattern

## Files Modified

- `inventory-ms/pom.xml`
- `inventory-ms/src/main/java/com/packt/bookstore/inventory/event/*.java` (3 files)
- `inventory-ms/src/main/java/com/packt/bookstore/inventory/service/NotificationService.java`
- `inventory-ms/src/main/java/com/packt/bookstore/inventory/controller/NotificationController.java`
- `inventory-ms/src/main/java/com/packt/bookstore/inventory/service/BookService.java`
- `gateway-server/src/main/java/com/packt/bookstore/gateway_server/config/GatewayRouteConfig.java`
- `gateway-server/src/main/java/com/packt/bookstore/gateway_server/config/SecurityConfig.java`
- `gateway-server/src/main/resources/application.yml`

## References

- [Spring WebFlux Documentation](https://docs.spring.io/spring-framework/reference/web/webflux.html)
- [Project Reactor](https://projectreactor.io/docs/core/release/reference/)
- [SSE Specification](https://html.spec.whatwg.org/multipage/server-sent-events.html)
- [MDN EventSource API](https://developer.mozilla.org/en-US/docs/Web/API/EventSource)

---
## License
See LICENSE for details.
