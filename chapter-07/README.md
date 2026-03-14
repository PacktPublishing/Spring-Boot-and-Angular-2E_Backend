# 📘 Chapter 07 — Documenting APIs & Enabling Application Observability Using Spring Boot

## Chapter Overview

This chapter builds on the resilient communication layer introduced in Chapter 06 by making our microservices **understandable** and **observable**.
You will document REST APIs using **OpenAPI (Swagger)** and enable **logging, metrics, and distributed tracing** so that every request in the Bookstore system can be understood, monitored, and debugged with confidence.

---

## ✅ Before You Run This Chapter

Please confirm the required runtime dependencies before running this chapter:

- Confirm the database is started (PostgreSQL and MongoDB for this chapter).
- Confirm any infrastructure dependencies are running (for example Docker services, if used).
- Confirm any dependencies from previous chapters are running as needed for your flow.

### Sequence for Running Databases and Microservices

1. **Start Databases First**
   - Start PostgreSQL (Inventory DB):
     ```bash
     docker run -d \
       --name bookstore-postgres \
       -e POSTGRES_USER=bookstore \
       -e POSTGRES_PASSWORD=bookstore123 \
       -e POSTGRES_DB=inventory \
       -p 5432:5432 \
       postgres:17
     ```
   - Start MongoDB (User DB):
     ```bash
     docker run -d \
       --name bookstore-mongo \
       -e MONGO_INITDB_ROOT_USERNAME=bookstore \
       -e MONGO_INITDB_ROOT_PASSWORD=bookstore123 \
       -e MONGO_INITDB_DATABASE=userDB \
       -p 27017:27017 \
       mongo:8
     ```
   - Confirm both databases are running:
     ```bash
     docker ps | grep bookstore-postgres
     docker ps | grep bookstore-mongo
     ```

2. **Start Eureka Discovery Server**

   ```bash
   cd eureka-server
   ./mvnw spring-boot:run
   ```

   - Verify at: http://localhost:8761

3. **Start Inventory Microservice**

   ```bash
   cd inventory-ms
   ./mvnw spring-boot:run
   ```

4. **Start User Microservice**

   ```bash
   cd user-ms
   ./mvnw spring-boot:run
   ```

5. **Start Gateway Microservice**
   ```bash
   cd gateway-server
   ./mvnw spring-boot:run
   ```

## 📦 Chapter Source Code Availability

The final source code for this chapter is already uploaded in this directory.

Use this folder as the reference implementation for the completed chapter state.

---

## Table of Contents

- [📘 Chapter 07 — Documenting APIs \& Enabling Application Observability Using Spring Boot](#-chapter-07--documenting-apis--enabling-application-observability-using-spring-boot)
  - [Chapter Overview](#chapter-overview)
  - [✅ Before You Run This Chapter](#-before-you-run-this-chapter)
    - [Sequence for Running Databases and Microservices](#sequence-for-running-databases-and-microservices)
  - [📦 Chapter Source Code Availability](#-chapter-source-code-availability)
  - [Table of Contents](#table-of-contents)
- [Understanding Documentation \& Observability](#understanding-documentation--observability)
- [Documenting APIs with OpenAPI](#documenting-apis-with-openapi)
- [Implementing OpenAPI with Springdoc](#implementing-openapi-with-springdoc)
  - [Step 1 — Add Dependency](#step-1--add-dependency)
  - [Step 2 — Access Documentation](#step-2--access-documentation)
- [OpenAPI Annotations in Practice](#openapi-annotations-in-practice)
  - [@Operation](#operation)
  - [@ApiResponses](#apiresponses)
  - [@Parameter](#parameter)
  - [@Tag](#tag)
- [API Documentation Best Practices](#api-documentation-best-practices)
- [Observability Fundamentals](#observability-fundamentals)
- [Logging with SLF4J \& Logback](#logging-with-slf4j--logback)
  - [Correlated Logging](#correlated-logging)
- [Distributed Tracing with Micrometer \& Zipkin](#distributed-tracing-with-micrometer--zipkin)
  - [Dependencies](#dependencies)
  - [Configuration](#configuration)
  - [Start Zipkin for Distributed Tracing](#start-zipkin-for-distributed-tracing)
- [Observability Best Practices \& Pitfalls](#observability-best-practices--pitfalls)
  - [Common Pitfalls](#common-pitfalls)
  - [Best Practices](#best-practices)
- [Summary](#summary)
- [Resources \& References](#resources--references)
  - [Official Documentation](#official-documentation)
  - [Useful Tools \& Services](#useful-tools--services)

---

# Understanding Documentation & Observability

In microservices architectures, **working APIs are not enough**.  
They must be:

- Discoverable and easy to consume (documentation)
- Transparent and traceable in production (observability)

Documentation tells consumers **what should happen**.  
Observability tells operators **what actually happened**.

Together, they form the foundation of operational excellence.

---

# Documenting APIs with OpenAPI

OpenAPI is the industry standard for describing REST APIs.

It defines:
| Feature | Description |
|-------|-------------|
| Paths | Available endpoints |
| Methods | HTTP verbs |
| Parameters | Path, query, headers |
| Schemas | Request/response models |
| Errors | Failure scenarios |
| Security | Authentication methods |

In Spring Boot 4.0.3, the recommended tool is **springdoc-openapi**.

---

# Implementing OpenAPI with Springdoc

## Step 1 — Add Dependency

```xml
<dependency>
  <groupId>org.springdoc</groupId>
  <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
  <version>2.3.0</version>
</dependency>
```

## Step 2 — Access Documentation

```
/v3/api-docs
/swagger-ui.html
```

Example:

```
http://localhost:8081/inventory/swagger-ui.html
```

---

# OpenAPI Annotations in Practice

## @Operation

```java
@Operation(
  summary = "Retrieve all books",
  description = "Returns a paginated and sortable list of books"
)
```

## @ApiResponses

```java
@ApiResponses({
  @ApiResponse(responseCode = "200", description = "Success"),
  @ApiResponse(responseCode = "400", description = "Invalid request"),
  @ApiResponse(responseCode = "500", description = "Server error")
})
```

## @Parameter

```java
@Parameter(
  name = "page",
  description = "Page number (0-based)",
  example = "0"
)
```

## @Tag

```java
@Tag(
  name = "Book Inventory",
  description = "Inventory management APIs"
)
```

These annotations make APIs **self-describing**, **interactive**, and **tool-friendly**.

---

# API Documentation Best Practices

| Practice                      | Benefit                |
| ----------------------------- | ---------------------- |
| Use @Operation & @ApiResponse | Clear intent           |
| Document error responses      | Safer clients          |
| Version endpoints             | Backward compatibility |
| Group endpoints with @Tag     | Better UX              |
| Keep docs near code           | Prevent drift          |

---

# Observability Fundamentals

Observability answers the question:
**Can we understand system behavior from its outputs?**

It consists of three pillars:

| Pillar  | Purpose                 |
| ------- | ----------------------- |
| Logs    | Discrete events         |
| Metrics | Aggregated measurements |
| Traces  | End-to-end request flow |

---

# Logging with SLF4J & Logback

Spring Boot uses SLF4J with Logback by default.

```java
log.info("Fetching book with id {}", id);
```

## Correlated Logging

```yaml
logging:
  pattern:
    correlation: "[${spring.application.name:},%X{traceId:-},%X{spanId:-}]"
```

This enriches every log entry with trace context.

---

# Distributed Tracing with Micrometer & Zipkin

## Dependencies

```xml
<dependency>
  <groupId>io.micrometer</groupId>
  <artifactId>micrometer-tracing-bridge-brave</artifactId>
</dependency>

<dependency>
  <groupId>io.zipkin.reporter2</groupId>
  <artifactId>zipkin-reporter-brave</artifactId>
</dependency>
```

## Configuration

```yaml
management:
  tracing:
    sampling:
      probability: 1.0
  zipkin:
    tracing:
      endpoint: http://localhost:9411/api/v2/spans

spring:
  main:
    allow-bean-definition-overriding: true
  profiles:
    active: dev
  java:
    version: 25
```

## Start Zipkin for Distributed Tracing

To enable distributed tracing and view trace data, start the Zipkin server:

```bash
docker run -d -p 9411:9411 openzipkin/zipkin
```

Once running, access the Zipkin UI at:
http://localhost:9411

---

# Observability Best Practices & Pitfalls

## Common Pitfalls

| Issue              | Impact                  |
| ------------------ | ----------------------- |
| Over-logging       | Noise & performance hit |
| Missing trace IDs  | Hard debugging          |
| Logging secrets    | Security risk           |
| Only infra metrics | No business insight     |

## Best Practices

| Area     | Recommendation               |
| -------- | ---------------------------- |
| Logs     | Consistent log levels        |
| Traces   | Always propagate traceId     |
| Metrics  | Track domain KPIs            |
| Security | Lock down actuator & Swagger |

---

# Summary

In this chapter, you implemented:

✔ OpenAPI documentation with Springdoc  
✔ Interactive Swagger UI for all services  
✔ Structured, correlated logging  
✔ Distributed tracing with Micrometer & Zipkin  
✔ Production-grade observability foundations

Your Bookstore microservices are now **documented, observable, and diagnosable**.  
In the next chapter, we will secure these APIs and observability endpoints using **Spring Security and Keycloak**.

---

# Resources & References

## Official Documentation

- [OpenAPI Specification](https://spec.openapis.org/oas/v3.1.0) — Complete OpenAPI 3.1.0 specification
- [Springdoc OpenAPI](https://springdoc.org/) — Official Springdoc documentation
- [Spring Boot Actuator](https://spring.io/guides/gs/actuator-service/) — Production-ready features guide
- [Micrometer Tracing](https://micrometer.io/docs/tracing) — Micrometer distributed tracing documentation

## Useful Tools & Services

- [Swagger Editor](https://editor.swagger.io/) — Interactive OpenAPI editor
- [Zipkin](https://zipkin.io/) — Distributed tracing system
- [Spring Boot Actuator Endpoints](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html) — Complete endpoint reference

---
