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

---

## Updated Source Code Notes
- Inventory-MS, Gateway Server, User-MS, and Eureka Server are updated for Java 21, Spring Boot 3.5.7, and Spring Cloud 2025.0.0
- SSE endpoints and event publishing logic are implemented in Inventory-MS
- Test scripts and debug HTML pages are included for easy validation
- Security and test profiles are aligned with previous chapters
- All changes are present in this branch and will be merged to main trunk

---

## Quick Start
- Run Inventory-MS, Gateway Server, User-MS, and Eureka Server
- Open `test-sse-debug.html` or use Postman/CLI to test SSE
- See `SSE-IMPLEMENTATION.md` for detailed step-by-step instructions

---

## License
See LICENSE for details.
