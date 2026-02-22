# 📘 Chapter 02 — Getting Started with Microservices Using Spring Boot

## Chapter Overview

This chapter establishes the baseline for the Bookstore backend.
You define the microservices mindset, set up your toolchain, and bootstrap the first Spring Boot service with a clean layered structure.

---

## ✅ Before You Run This Chapter

Please confirm the required runtime dependencies before running this chapter:

- Confirm the database is started (if this chapter uses one).
- Confirm any infrastructure dependencies are running (for example Docker services, if used).
- Confirm any dependencies from previous chapters are running as needed for your flow.

---

## 📦 Chapter Source Code Availability

The final source code for this chapter is available in this directory.

Reference implementation path:

```text
chapter-02/inventory-ms/
```

Main bootstrap class:

```text
chapter-02/inventory-ms/src/main/java/com/packt/bookstore/inventory/InventoryMsApplication.java
```

---

## Table of Contents

- [Microservices Fundamentals](#microservices-fundamentals)
- [Design Patterns You Will Reuse](#design-patterns-you-will-reuse)
- [Bookstore Architecture Baseline](#bookstore-architecture-baseline)
- [Environment Setup](#environment-setup)
- [First Spring Boot Service](#first-spring-boot-service)
- [Summary](#summary)

---

## Microservices Fundamentals

Microservices organize the backend into small, independently deployable services aligned to business capabilities.

Core principles used across the book:

- Single responsibility per service
- Independent deployment and scaling
- API-first communication
- Database per service
- Operational visibility from day one

They communicate through lightweight protocols, typically REST or messaging, and own their data stores.

### Key Characteristics

- **Loose coupling** — Services are independent and can be deployed separately
- **High cohesion** — Each service focuses on a single business capability
- **Technology-agnostic** — Teams can choose the best tools for each service
- **Decentralized governance** — Teams own their services end-to-end
- **Built-in fault tolerance** — Services handle failures gracefully

---

## Microservice Design Patterns

### Decomposition Patterns

- **Decompose by business capability** — Organize services around business functions
- **Decompose by subdomain** — Align services with domain-driven design subdomains

### Integration Patterns

- **API Gateway** — Single entry point for all client requests
- **Backend for Frontend (BFF)** — Separate backends for different client types
- **Service Mesh** — Infrastructure layer for service-to-service communication
- **Message-based communication** — Asynchronous communication via message brokers

### Data Management Patterns

- **Database per service** — Each service owns its data store
- **Event sourcing** — Store state changes as sequence of events
- **CQRS** — Separate read and write models
- **Saga Pattern** — Manage distributed transactions across services

### Reliability Patterns

- **Circuit breaker** — Prevent cascading failures
- **Retry with backoff** — Retry failed requests with increasing delays
- **Bulkhead isolation** — Isolate resources to prevent system-wide failures
- **Timeouts and fallbacks** — Set timeouts and provide fallback responses

### Observability Patterns

- **Centralized logging** — Aggregate logs from all services
- **Distributed tracing** — Track requests across service boundaries
- **Metrics collection** — Monitor service health and performance
- **Health checks** — Expose service health status

---

## Design Patterns You Will Reuse

This chapter introduces patterns that continue in later chapters:

| Pattern | Why it matters |
| ------- | -------------- |
| API Gateway | Single client entry point |
| Database per Service | Strong service ownership |
| Circuit Breaker | Failure isolation |
| Centralized Observability | Faster debugging |
| Service Discovery | Dynamic routing in distributed systems |

---

## Bookstore Architecture Baseline

Initial service landscape:

- Inventory service (catalog and stock)
- User service (accounts and profile)

Planned platform additions in later chapters:

- Gateway, service discovery, security, observability, and containerized deployment.

---

## Environment Setup

Install and verify:

- Java 25
- Spring Boot 4.0.3
- Maven
- Git
- Docker Desktop
- VS Code + Java/Spring extensions
- Postman API Client

For complete step-by-step setup on macOS, Linux, and Windows, see:

- [Environment Setup Guide](./ENVIRONMENT_SETUP.md)

```bash
java -version
mvn -version
git --version
docker --version
docker compose version
```

Project configuration used in this chapter:

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>4.0.3</version>
</parent>

<properties>
    <java.version>25</java.version>
</properties>
```

Clone repositories:

```bash
git clone https://github.com/PacktPublishing/Spring-Boot-and-Angular-2E_Backend
git clone https://github.com/PacktPublishing/Spring-Boot-and-Angular-2E_Frontend
```

---

## First Spring Boot Service

Create a starter service from Spring Initializr:

- Project: Maven
- Language: Java
- Spring Boot: 4.0.3
- Java: 25
- Dependencies: Spring Web, DevTools, Actuator

Recommended package structure:

```text
src/main/java/com/bookstore/
├── controller/
├── service/
├── repository/
├── dto/
├── model/
└── config/
```

Chapter source code snapshot (actual bootstrap class):

```java
@SpringBootApplication
public class InventoryMsApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventoryMsApplication.class, args);
    }
}
```

Optional layered examples for the next implementation steps:

```java
@Entity
@Table(name = "books")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String author;
    private BigDecimal price;
}
```

Repository:

```java
public interface BookRepository extends JpaRepository<Book, Long> {
    List<Book> findByAuthor(String author);
}
```

Controller:

```java
@RestController
@RequestMapping("/api/books")
public class BookController {
    @GetMapping
    public ResponseEntity<List<Book>> getAllBooks() {
        return ResponseEntity.ok(List.of());
    }
}
```

---

## 🛠️ Troubleshooting & Environment Setup

- [Troubleshooting & Environment Setup Guide](../ENVIRONMENT_SETUP.md)

---

## Summary

In this chapter, you completed:

- Microservices foundations and patterns
- Local development setup
- First Spring Boot service bootstrap
- Layered project structure for future chapters

This baseline is used directly in Chapter 03 and then expanded into persistence, APIs, discovery, observability, and security.

---

## Resources & References

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Spring Initializr](https://start.spring.io)
- [Microservices.io](https://microservices.io)
- [Spring Cloud](https://spring.io/projects/spring-cloud)

